package experiment.os.command_parser;

import experiment.os.Session;
import experiment.os.block.base.*;
import experiment.os.exception.*;
import experiment.os.myEnum.AuthorityType;
import experiment.os.myEnum.FileType;
import experiment.os.system.*;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class FileExecutor implements Executor {
    // open xxx
    // close xxx
    // read xxx
    // write xxx

    private BFD bfd = BFD.getInstance();
    private StringBuilder sb;

    @Override
    public String excute(String command, String[] args, String[] currentPath, Session session) {
        switch (command) {
            case "touch":
                return create(args, currentPath, session);

            case "open":
                return open(args, currentPath, session);

            case "close":
                return close(args, currentPath, session);

            case "cat":
                return read(args, currentPath, session);

            case "echo":
                return write(args, currentPath, session);
            default:
                return null;

        }
    }

    private String create(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        for (String[] path : combinationPaths) {
            try {
                if (!MemSuperBlock.getInstance().hasFreeBlock(1))
                    throw new BlockNotEnough();

                if (!bfd.hasFreeInode(1))
                    throw new NoFreeINode();

                // 祖先目录
                Integer[] eachIndex = bfd.getEachIndex(ArrayUtils.subarray(path, 0, path.length - 1));
                if (!bfd.hasAuthority(eachIndex, excutor, AuthorityType.EXCUTE) ||
                        !bfd.hasAuthority(new Integer[]{eachIndex[eachIndex.length - 1]}, excutor, AuthorityType.WRITE)) {
                    sb.append(new PermisionException("create"));
                    continue;
                }

                // 父目录
                DiskINode parentDiskInode = bfd.get(eachIndex[eachIndex.length - 1]);
                if (parentDiskInode.getFileType() != FileType.DIRECTORY.getType()) {
                    sb.append(new NoSuchFileOrDirectory(path.toString()));
                    continue;
                }

                BlockBufferItem parentBlockBufferItem = BlockBuffer.getInstance().get(parentDiskInode.getFirstBlock());
                Directory parentDirectory = (Directory) parentBlockBufferItem.getBlock();

                if (parentDirectory.find(path[path.length - 1]) != -1) {
                    sb.append(new FileOrDirectoryAlreadyExists(path.toString()));
                    continue;
                } else if (!parentDirectory.hasFreeItem(1)) {
                    sb.append(new DirectoryIsFull());
                    continue;
                }

                // allocate
                Integer freeINodeIndex = bfd.getFreeINodeIndex();
                int[] freeBlocks = MemSuperBlock.getInstance().dispaterBlock(1);
                BlockBuffer.getInstance().set(freeBlocks[0], new File(new char[]{}, -1));
                DiskINode diskINode1 = bfd.get(freeINodeIndex);
                diskINode1.initInode(excutor.getDefaultFileMode(), (short) freeBlocks[0], FileType.FILE, excutor.getUid(), excutor.getGid());
                parentDirectory.addItem(new DirectoryItem(path[path.length - 1].toCharArray(), freeINodeIndex));


            } catch (NoSuchFileOrDirectory | NoFreeINode | DirectoryIsFull | BlockNotEnough p) {
                sb.append(p + "\n");
            }
        }
        return sb.toString();
    }

    private String open(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        for (String[] path : combinationPaths) {
            try {
                Integer[] eachIndex = bfd.getEachIndex(path);
                if (!bfd.hasAuthority(ArrayUtils.subarray(eachIndex, 0, eachIndex.length - 1), excutor, AuthorityType.EXCUTE) ||
                        !bfd.hasAuthority(new Integer[]{eachIndex[eachIndex.length - 1]}, excutor, AuthorityType.READ)) {
                    throw new PermisionException("open");
                }

                DiskINode openingFileDiskInode = bfd.get(eachIndex[eachIndex.length - 1]);
                if (openingFileDiskInode.getFileType() == FileType.FILE.getType()) {
                    if (!excutor.getUserOpenFile().allocate(eachIndex[eachIndex.length - 1], path))
                        sb.append("open file failure");

                    if (verbosePrint()) sb.append("open " + StringUtils.join(path, "/") + "\n");
                } else if (openingFileDiskInode.getFileType() == FileType.SYMBOL_LINK.getType()) {
                    // get read path
                    int realPathInodeIndex = getSymbolLinkRealpathDiskInode(eachIndex[eachIndex.length - 1], excutor);
                    if (realPathInodeIndex == -1) {
                        // can not find (real path is dir)
                        throw new CustomException("link file (" + StringUtils.join(path, "/") + " -> " + ") is not exist");
                    } else {
                        if (!excutor.getUserOpenFile().allocate(realPathInodeIndex, path))
                            sb.append("open file failure");

                        if (verbosePrint()) sb.append("open " + StringUtils.join(path, "/") + "\n");
                    }
                }
            } catch (PermisionException | NoSuchFileOrDirectory | CustomException p) {
                sb.append(p + "\n");
            }
        }
        return sb.toString();
    }

    private String close(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        for (String[] path : combinationPaths) {
            try {
                int hasOpenFile = excutor.getUserOpenFile().getDiskinodeByPath(path);
                if (hasOpenFile == -1)
                    throw new FileAlreadyOpen();

                excutor.getUserOpenFile().delete(hasOpenFile);

            } catch (FileAlreadyOpen p) {
                sb.append(p + "\n");
            }
        }
        return sb.toString();
    }

    private String read(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        try {
            if (args.length <= 0)
                throw new CustomException("you should open file before read!");

            // 只读第一个
            String[] path = getCombinationPath(args[0], currentPath);
            // 先用名字在系统打开表找一下
            int openFileDiskInodeIndex = excutor.getUserOpenFile().getDiskinodeByPath(path);
            if (openFileDiskInodeIndex == -1) {
                // 找不到, 可能没有或者是符号链接文件
                Integer[] eachIndex = bfd.getEachIndex(path);
                if (!bfd.hasAuthority(ArrayUtils.subarray(eachIndex, 0, eachIndex.length - 1), excutor, AuthorityType.EXCUTE) ||
                        !bfd.hasAuthority(ArrayUtils.subarray(eachIndex, eachIndex.length - 1, eachIndex.length), excutor, AuthorityType.READ))
                    throw new PermisionException("read");

                if (bfd.get(eachIndex[eachIndex.length - 1]).getFileType() != FileType.SYMBOL_LINK.getType())
                    throw new CustomException("this is not a open file");

                int realpathDiskInode = getSymbolLinkRealpathDiskInode(eachIndex[eachIndex.length - 1], excutor);
                if (realpathDiskInode == -1) {
                    throw new CustomException("link file (" + StringUtils.join(path, "/") + " -> " + ") is not exist");
                } else {
                    openFileDiskInodeIndex = realpathDiskInode;
                }
            }

            MemInode memInode = SysOpenFile.getInstance().getMemInodeByDiskInodeIndex(openFileDiskInodeIndex);
            if (memInode == null) {
                throw new CustomException("this is not a open file");
            }

            short firstBlock = memInode.getFirstBlock();
            short lastBlock = memInode.getLastBlock();

            StringBuilder text = new StringBuilder();

            do {
                BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(firstBlock);
                File file = (File) blockBufferItem.getBlock();

                if (firstBlock == lastBlock) {
                    text.append(file.getData(), 0, (int) (memInode.getSize() % File.FILE_TEXT_MAX_LENGTH));
                } else {
                    text.append(file.getData());
                    firstBlock = (short) file.getNextFileIndex();
                }

            } while (firstBlock != lastBlock);

            sb = text;
        } catch (CustomException | NoSuchFileOrDirectory | PermisionException p) {
            sb.append(p);
        } finally {
            return sb.toString();
        }

    }

    private String write(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        try {
            if (args.length <= 2)
                throw new ProvideNotEnoughParam();

            if (!args[1].equals(">") && !args[1].equals(">>"))
                throw new CustomException("写格式错误, 必须为 write text >(>) file");

            // write text >> file
            // 最后一个为打开路径
            String[] path = getCombinationPath(args[2], currentPath);
            // 先用名字在系统打开表找一下
            int openFileDiskInodeIndex = excutor.getUserOpenFile().getDiskinodeByPath(path);
            if (openFileDiskInodeIndex == -1) {
                // 找不到, 可能没有或者是符号链接文件
                Integer[] eachIndex = bfd.getEachIndex(path);
                if (!bfd.hasAuthority(ArrayUtils.subarray(eachIndex, 0, eachIndex.length - 1), excutor, AuthorityType.EXCUTE) ||
                        !bfd.hasAuthority(ArrayUtils.subarray(eachIndex, eachIndex.length - 1, eachIndex.length), excutor, AuthorityType.READ))
                    throw new PermisionException("write");

                if (bfd.get(eachIndex[eachIndex.length - 1]).getFileType() != FileType.SYMBOL_LINK.getType())
                    throw new CustomException("this is not a open file");

                int realpathDiskInode = getSymbolLinkRealpathDiskInode(eachIndex[eachIndex.length - 1], excutor);
                if (realpathDiskInode == -1) {
                    throw new CustomException("link file (" + StringUtils.join(path, "/") + " -> " + ") is not exist");
                } else {
                    openFileDiskInodeIndex = realpathDiskInode;
                }
            }

            MemInode memInode = SysOpenFile.getInstance().getMemInodeByDiskInodeIndex(openFileDiskInodeIndex);
            if (memInode == null) {
                throw new CustomException("this is not a open file");
            }

            short firstBlock = memInode.getFirstBlock();
            short lastBlock = memInode.getLastBlock();

            if (! memInode.hasAuthority(excutor, AuthorityType.WRITE))
                throw new PermisionException("write");

            // TODO modify modify
            char[] textChars = args[0].toCharArray();
            if (args[1].equals(">")) {
                // rewrite
                long formerSize = memInode.getSize();
                long formerBlockSize = (formerSize - 1) / File.FILE_TEXT_MAX_LENGTH + 1;
                int curBlockSize = (textChars.length - 1) / File.FILE_TEXT_MAX_LENGTH + 1;
                if (textChars.length >= formerSize) {
                    // more than before

                    // allocate new block
                    // maybe fail (not enough)
                    if (!MemSuperBlock.getInstance().hasFreeBlock((int) (curBlockSize - formerBlockSize))) {
                        // block not enough
                        throw new BlockNotEnough();
                    }

                    // block enough
                    int[] appendBlocks = MemSuperBlock.getInstance().dispaterBlock((int) (curBlockSize - formerBlockSize));
                    // link append block
                    for (int i = 0; i < appendBlocks.length; i++) {
                        BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(lastBlock);
                        blockBufferItem.setModified(true);
                        File lastFileBlock = (File) blockBufferItem.getBlock();

                        lastFileBlock.setNextFileIndex(appendBlocks[i]);
                        BlockBuffer.getInstance().set(appendBlocks[i], new File(new char[]{}, -1));

                        lastBlock = (short) appendBlocks[i];
                    }
                } else {
                    // less than before
                    // recall useless block
                    for (int i = 0; i < formerBlockSize - curBlockSize; i++) {
                        BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(firstBlock);
                        File file = (File) blockBufferItem.getBlock();
                        MemSuperBlock.getInstance().recall((int) firstBlock);
                        firstBlock = (short) file.getNextFileIndex();
                    }
                }

                // rewrite
                int offset = 0;
                short firstBlockBack = firstBlock;
                do {
                    BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(firstBlock);
                    blockBufferItem.setModified(true);
                    File file = (File) blockBufferItem.getBlock();

                    if (firstBlock == lastBlock) {
                        file.setData(ArrayUtils.subarray(textChars, offset, textChars.length));
                    } else {
                        file.setData(ArrayUtils.subarray(textChars, offset, offset + File.FILE_TEXT_MAX_LENGTH));
                        offset += file.FILE_TEXT_MAX_LENGTH;
                        firstBlock = (short) file.getNextFileIndex();
                    }
                } while (firstBlock != lastBlock);

                memInode.setSize(textChars.length);
                memInode.setModified(true);
                memInode.setModifyTime(System.currentTimeMillis());
                memInode.setFirstBlock(firstBlockBack);
                memInode.setLastBlock(lastBlock);
            } else {
                // append
                long formerSize = memInode.getSize();
                long formerBlockSize = (formerSize - 1) / File.FILE_TEXT_MAX_LENGTH + 1;
                int curBlockSize = (textChars.length - 1) / File.FILE_TEXT_MAX_LENGTH + 1;
                if (!MemSuperBlock.getInstance().hasFreeBlock((int) (curBlockSize - formerBlockSize))) {
                    // block not enough
                    throw new BlockNotEnough();
                }

                short firstBlockInAppend = lastBlock;

                // block enough
                int[] appendBlocks = MemSuperBlock.getInstance().dispaterBlock((int) (curBlockSize - formerBlockSize));
                // link append block
                for (int i = 0; i < appendBlocks.length; i++) {
                    BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(lastBlock);
                    blockBufferItem.setModified(true);
                    File lastFileBlock = (File) blockBufferItem.getBlock();

                    lastFileBlock.setNextFileIndex(appendBlocks[i]);
                    BlockBuffer.getInstance().set(appendBlocks[i], new File(new char[]{}, -1));

                    lastBlock = (short) appendBlocks[i];
                }

                // append write
                // 把最后一个file块和添加文本拼接
                BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(firstBlockInAppend);
                blockBufferItem.setModified(true);
                File firstBlockInAppendBlock = (File) blockBufferItem.getBlock();
                char[] appendChars = ArrayUtils.addAll(firstBlockInAppendBlock.getData(), textChars);

                int offset = 0;
                do {
                    BlockBufferItem blockBufferItem2 = BlockBuffer.getInstance().get(firstBlockInAppend);
                    blockBufferItem2.setModified(true);
                    File file = (File) blockBufferItem2.getBlock();

                    if (firstBlockInAppend == lastBlock) {
                        file.setData(ArrayUtils.subarray(appendChars, offset, appendChars.length));
                    } else {
                        file.setData(ArrayUtils.subarray(appendChars, offset, offset + File.FILE_TEXT_MAX_LENGTH));
                        offset += file.FILE_TEXT_MAX_LENGTH;
                        firstBlockInAppend = (short) file.getNextFileIndex();
                    }
                } while (firstBlockInAppend != lastBlock);

                memInode.setSize(textChars.length + memInode.getSize());
                memInode.setModified(true);
                memInode.setModifyTime(System.currentTimeMillis());
                memInode.setLastBlock(lastBlock);
            }
        } catch (CustomException | NoSuchFileOrDirectory | PermisionException p) {
            sb.append(p);
        } finally {
            return sb.toString();
        }
    }
}
