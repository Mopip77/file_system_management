package experiment.os.command_parser;

import com.sun.xml.internal.ws.api.server.EndpointAwareCodec;
import experiment.os.Server;
import experiment.os.Session;
import experiment.os.block.base.*;
import experiment.os.exception.*;
import experiment.os.myEnum.AuthorityType;
import experiment.os.myEnum.FileType;
import experiment.os.system.*;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.FilenameFilter;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class DirectoryExecutor implements Executor {
    // cd xxx
    // ls xxx
    // mkdir xxx
    // rm xxx
    // mv xxx xxx
    // cp xxx xxx
    // ln [-s] xxx xxx

    private BFD bfd = BFD.getInstance();
    private StringBuilder sb = new StringBuilder();

    @Override
    public String excute(String command, String[] args, String[] currentPath, Session session) {
        switch (command) {
            case "cd":
                return cd(args, currentPath, session);

            case "ls":
                return ls(args, currentPath, session);

            case "mkdir":
                return mkdir(args, currentPath, session);

            case "rm":
                return rm(args, currentPath, session);

            case "mv":
                return mv(args, currentPath, session);

            case "ln":
                return ln(args, currentPath, session);

        }
        return "";
    }

    /**
     * 过滤checkPath 中 为 targetPath 祖先目录的项
     *
     * @param checkPaths
     * @param targetPath
     * @return
     */
    private List<String[]> filterAncestorPath(List<String[]> checkPaths, String[] targetPath) {
        List<String[]> result = new ArrayList<>();
        for (String[] checkPath : checkPaths) {
            if (checkPath.length >= targetPath.length) {
                result.add(checkPath);
                continue;
            }

            boolean isAncient = true;
            for (int i = 0; i < checkPath.length; i++) {
                if (checkPath[i].equals(targetPath[i])) {
                    isAncient = false;
                    break;
                }
            }
            if (isAncient) {
                System.out.println("there is illegal command: " + StringUtils.join(checkPath, "/") + " -> " + StringUtils.join(targetPath, "/"));
            } else {
                result.add(checkPath);
            }
        }
        return result;
    }

    private String cd(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 只取第一个, 如果一个也没有则跳转家目录
        try {
            String[] resultPath = null;
            if (args.length == 0) {
                resultPath = new String[]{excutor.getName()};
            } else {
                resultPath = getCombinationPath(args[0], currentPath);
            }

            Integer[] eachIndex = bfd.getEachIndex(resultPath);
            if (!bfd.hasAuthority(eachIndex, excutor, AuthorityType.EXCUTE))
                throw new PermisionException("cd");

            if (bfd.get(eachIndex[eachIndex.length - 1]).getFileType() != FileType.DIRECTORY.getType())
                throw new NoSuchFileOrDirectory(resultPath.toString());

            session.setCurrentPath(resultPath);
        } catch (NoSuchFileOrDirectory | PermisionException p) {
            sb.append(p + "\n");
        } finally {
            return sb.toString();
        }
    }

    private String ls(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        String[] resultPath = null;
        try {
            // 只取第一个, 如果一个也没有则为当前目录
            if (args.length == 0) {
                resultPath = currentPath;
            } else {
                resultPath = getCombinationPath(args[0], currentPath);
            }

            // getIndex inode index
            Integer[] eachFolderINodeIndex;
            eachFolderINodeIndex = bfd.getEachIndex(resultPath);

            // 层次的鉴权
            boolean flag = true;
            flag &= bfd.hasAuthority(ArrayUtils.subarray(eachFolderINodeIndex, 0, eachFolderINodeIndex.length - 1), excutor, AuthorityType.EXCUTE);
            if (flag) {
                if ((bfd.get(eachFolderINodeIndex[eachFolderINodeIndex.length - 1]).getFileType() & FileType.DIRECTORY.getType()) == 0 ||
                        !bfd.hasAuthority(new Integer[]{eachFolderINodeIndex[eachFolderINodeIndex.length - 1]}, excutor, AuthorityType.READ)) {
                    throw new PermisionException(resultPath.toString());
                }

                // print folder in path
                DiskINode diskINode = bfd.get(eachFolderINodeIndex[eachFolderINodeIndex.length - 1]);
                BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(diskINode.getFirstBlock());
                Directory directory = (Directory) blockBufferItem.getBlock();

                Stream.of(directory.getDirectoryItems())
                        .limit(directory.getSize())  //限制数量
                        .map(item -> new String(item.getdName()) + "\t")
                        .forEach(sb::append);
            } else {
                throw new NoSuchFileOrDirectory(resultPath.toString());
            }
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            sb.append(noSuchFileOrDirectory);
        } finally {
            return sb.toString();
        }
    }

    private String mkdir(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 取所有的args
        try {
            if (args.length == 0) {
                throw new ProvideNotEnoughParam();
            } else {
                List<String[]> createPaths = getCombinationPaths(args, currentPath);
                Integer[] pathIndex;

                for (String[] s : createPaths) {
                    // enough
                    if (!bfd.hasFreeInode(1))
                        throw new NoFreeINode();
                    if (!MemSuperBlock.getInstance().hasFreeBlock(1))
                        throw new BlockNotEnough();


                    //TODO 要先判断目标目录下是否已经存在要创建的目录
                    // through path
                    pathIndex = bfd.getEachIndex(ArrayUtils.subarray(s, 0, s.length - 1));
                    if (!bfd.hasAuthority(pathIndex, excutor, AuthorityType.EXCUTE)) {
                        sb.append(new PermisionException("mkdir"));
                        continue;
                    }

                    // parent inode
                    DiskINode diskINode = bfd.get(pathIndex[pathIndex.length - 1]);
                    if (diskINode.getFileType() != FileType.DIRECTORY.getType() ||
                            !bfd.hasAuthority(new Integer[]{pathIndex[pathIndex.length - 1]}, excutor, AuthorityType.WRITE)) {
                        sb.append(new PermisionException("mkdir"));
                        continue;
                    }


                    BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(diskINode.getFirstBlock());
                    blockBufferItem.setModified(true);
                    Directory parentDir = (Directory) blockBufferItem.getBlock();
                    if (!parentDir.hasFreeItem(1)) {
                        sb.append(new DirectoryIsFull());
                        continue;
                    }

                    // targetPath should not exist
                    if (parentDir.find(s[s.length - 1]) != -1) {
                        sb.append(new FileAlreadyExistsException(s.toString()));
                        continue;
                    }


                    // 分配
                    // inode
                    Integer freeINodeIndex = bfd.getFreeINodeIndex();
                    int[] freeBlocks = MemSuperBlock.getInstance().dispaterBlock(1);
                    BlockBuffer.getInstance().set(freeBlocks[0], new Directory());
                    DiskINode diskINode1 = bfd.get(freeINodeIndex);
                    diskINode1.initInode(excutor.getDefaultFolderMode(), (short) freeBlocks[0], FileType.DIRECTORY, excutor.getUid(), excutor.getGid());
                    parentDir.addItem(new DirectoryItem(s[s.length - 1].toCharArray(), freeINodeIndex));
                }
            }
        } catch (ProvideNotEnoughParam | NoSuchFileOrDirectory | NoFreeINode | BlockNotEnough | DirectoryIsFull p) {
            sb.append(p);
        } finally {
            return sb.toString();
        }
    }

    private String rm(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 取所有的args, args.length == 0 或者结合后为当前路径的祖先都跳过
        if (args.length == 0)
            return "delete what?\n";

        List<String[]> createPaths = getCombinationPaths(args, currentPath);
        // 过滤祖先文件夹
        List<String[]> filterPaths = filterAncestorPath(createPaths, currentPath);

        List<ImmutablePair<String[], Integer>> deleteQueue = new LinkedList<>();

        for (String[] filterPath : filterPaths) {
            deleteQueue.add(new ImmutablePair<>(filterPath, 0));
        }

        while (deleteQueue.size() != 0) {
            try {
                ImmutablePair<String[], Integer> removeItem = deleteQueue.remove(0);
                String[] deletePath = removeItem.getLeft();

                if (removeItem.getRight() != 0) {
                    // traveled directory
                    if (removeItem.getRight() > 50) {
                        sb.append(removeItem.getLeft().toString() + "can not delete");
                        continue;
                    }
                    // traveled dir
                    Integer index = bfd.getIndex(deletePath);
                    DiskINode diskINode = bfd.get(index);
                    BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(diskINode.getFirstBlock());
                    blockBufferItem.setModified(true);
                    Directory dir = (Directory) blockBufferItem.getBlock();
                    if (dir.getSize() == 0) {
                        // delete
                        // find parent directory
                        Integer parentInodeIndex = bfd.getIndex(ArrayUtils.subarray(deletePath, 0, deletePath.length - 1));
                        DiskINode parentInode = bfd.get(parentInodeIndex);
                        BlockBufferItem parentBBI = BlockBuffer.getInstance().get(parentInode.getFirstBlock());
                        parentBBI.setModified(true);
                        Directory parentDir = (Directory) parentBBI.getBlock();
                        DirectoryItem deletedDirectoryItem = parentDir.removeItem(deletePath[deletePath.length - 1]);
                        FileNameIndex.getInstance().removeAllDescendant(deletePath);

                        // delete remove item, free block and inode
                        int deletedInodeIdex = deletedDirectoryItem.getdIno();
                        freeInodeAndBlockWithoutChecking(deletedInodeIdex);
                    } else {
                        deleteQueue.add(new ImmutablePair<String[], Integer>(deletePath, removeItem.getRight() + 1));
                    }
                } else {
                    // untraveled file or directory
                    Integer[] eachIndex = bfd.getEachIndex(deletePath);
                    if (!bfd.hasAuthority(ArrayUtils.subarray(eachIndex, 0, eachIndex.length - 1), excutor, AuthorityType.EXCUTE) ||
                            !bfd.hasAuthority(new Integer[]{eachIndex[eachIndex.length - 2]}, excutor, AuthorityType.WRITE)) {
                        throw new PermisionException("rm");
                    }

                    DiskINode handlingInode = bfd.get(eachIndex[eachIndex.length - 1]);

                    if (handlingInode.getFileType() == FileType.DIRECTORY.getType()) {
                        // untraveled directory
                        BlockBufferItem deletingBufferItem = BlockBuffer.getInstance().get(handlingInode.getFirstBlock());
                        deletingBufferItem.setModified(true);
                        Directory deletingDir = (Directory) deletingBufferItem.getBlock();

                        for (DirectoryItem item : deletingDir.getDirectoryItems()) {
                            deleteQueue.add(new ImmutablePair<String[], Integer>(
                                    ArrayUtils.add(deletePath, new String(item.getdName())), 0
                            ));
                        }
                        deleteQueue.add(new ImmutablePair<String[], Integer>(deletePath, removeItem.getRight() + 1));
                    } else {
                        // untraveled file
                        // find parent directory

                        // 1.拿到文件夹
                        DiskINode parentInode = bfd.get(eachIndex[eachIndex.length - 2]);
                        BlockBufferItem parentBBI = BlockBuffer.getInstance().get(parentInode.getFirstBlock());
                        parentBBI.setModified(true);
                        Directory parentDir = (Directory) parentBBI.getBlock();

                        // 2.用文件的inode尝试先在打开表中删除
                        if (! excutor.getUserOpenFile().delete(eachIndex[eachIndex.length - 1]))
                            throw new CustomException("其他用户正打开文件, 无法删除");

                        // 查看连接数
                        DiskINode targetDiskInode = bfd.get(eachIndex[eachIndex.length - 1]);
                        if (! targetDiskInode.decQuote()) {
                            System.out.println("文件链接数不为0, 不删除");
                            parentDir.removeItem(deletePath[deletePath.length - 1]);
                            FileNameIndex.getInstance().removeAllDescendant(deletePath);
                            continue;
                        }


                        // 3.打开表删除成功 则释放inode和block
                        DirectoryItem deletedFileItem = parentDir.removeItem(deletePath[deletePath.length - 1]);
                        FileNameIndex.getInstance().removeAllDescendant(deletePath);
                        freeInodeAndBlockWithoutChecking(deletedFileItem.getdIno());
                    }
                }
            } catch (PermisionException | NoSuchFileOrDirectory | CustomException p) {
                sb.append(p + "\n");
            }
        }
        return sb.toString();
    }

    private String mv(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        // 取所有的args 都移动到最后一个 arg, args.length == 0, 1 或者结合后为目标路径的祖先都跳过
        // 时间所限, targetpath 只能为目录
        try {
            if (args.length <= 1)
                throw new ProvideNotEnoughParam();

            List<String[]> createPaths = getCombinationPaths(ArrayUtils.subarray(args, 0, args.length - 1), currentPath);

            String[] targetPath;
            targetPath = getCombinationPath(args[args.length - 1], currentPath);
            Integer[] targetEachIndex = bfd.getEachIndex(targetPath);
            if (!bfd.hasAuthority(targetEachIndex, excutor, AuthorityType.EXCUTE) ||
                    !bfd.hasAuthority(new Integer[]{targetEachIndex[targetEachIndex.length - 1]}, excutor, AuthorityType.READ)) {
                throw new PermisionException("mv");
            }

            if (bfd.get(targetEachIndex[targetEachIndex.length - 1]).getFileType() != FileType.DIRECTORY.getType())
                throw new NoSuchFileOrDirectory(targetPath.toString());

            // target path (directory)
            DiskINode targetDiskInode = bfd.get(targetEachIndex[targetEachIndex.length - 1]);
            BlockBufferItem targetParentItem = BlockBuffer.getInstance().get(targetDiskInode.getFirstBlock());
            targetParentItem.setModified(true);
            Directory targetDir = (Directory) targetParentItem.getBlock();


//            List<String[]> filterPaths = filterAncestorPath(createPaths, targetPath);
            for (String[] createPath : createPaths) {
                if (!targetDir.hasFreeItem(1)) {
                    sb.append(new DirectoryIsFull());
                    continue;
                }

                Integer[] eachIndex = bfd.getEachIndex(createPath);

                if (!bfd.hasAuthority(ArrayUtils.subarray(eachIndex, 0, eachIndex.length - 1), excutor, AuthorityType.EXCUTE) ||
                        !bfd.hasAuthority(new Integer[]{eachIndex[eachIndex.length - 2]}, excutor, AuthorityType.READ)) {
                    sb.append(new PermisionException("mv") + "\n");
                    continue;
                }

                DiskINode parentInode = bfd.get(eachIndex[eachIndex.length - 2]);
                BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(parentInode.getFirstBlock());
                blockBufferItem.setModified(true);
                Directory parentDir = (Directory) blockBufferItem.getBlock();
                DirectoryItem removeItem = parentDir.removeItem(createPath[createPath.length - 1]);
                FileNameIndex.getInstance().removeAllDescendant(createPath);

                targetDir.addItem(removeItem);
            }

            // TODO mv path function
        } catch (ProvideNotEnoughParam p) {
            sb.append(p);
        } finally {
            return sb.toString();
        }
    }

    private String ln(String[] args, String[] currentPath, Session session) {
        User excutor = session.getUser();
        sb = new StringBuilder();
        try {
            // 只用前两个 或 三个
            if (args.length < 2) {
                throw new ProvideNotEnoughParam();
            }

            String[] sourcePath = null;
            String[] targetPath = null;
            if (args[0].equals("-s")) {
                // 符号链接
                if (args.length < 3) {
                    return "";
                }

                sourcePath = getCombinationPath(args[1], currentPath);
                targetPath = getCombinationPath(args[2], currentPath);
            } else {
                // 硬链接
                sourcePath = getCombinationPath(args[0], currentPath);
                targetPath = getCombinationPath(args[1], currentPath);
            }

            // source check
            Integer[] sourceEachIndex = bfd.getEachIndex(sourcePath);
            if (!bfd.hasAuthority(ArrayUtils.subarray(sourceEachIndex, 0, targetPath.length - 1), excutor, AuthorityType.EXCUTE) ||
                !bfd.hasAuthority(new Integer[]{sourceEachIndex[sourceEachIndex.length - 1]}, excutor, AuthorityType.READ)) {
                throw new PermisionException("ln");
            }
            if (bfd.get(sourceEachIndex[sourceEachIndex.length - 1]).getFileType() != FileType.FILE.getType())
                throw new NoSuchFileOrDirectory(targetPath.toString());

            // target check
            Integer[] targetEachIndex = bfd.getEachIndex(ArrayUtils.subarray(targetPath, 0, targetPath.length - 1));
            if (!bfd.hasAuthority(targetEachIndex, excutor, AuthorityType.EXCUTE) ||
                !bfd.hasAuthority(new Integer[]{targetEachIndex[targetEachIndex.length - 1]}, excutor, AuthorityType.WRITE)) {
                throw new PermisionException("ln");
            }

            // the last one not exist
            DiskINode parentInode = bfd.get(targetEachIndex[targetEachIndex.length - 1]);
            BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(parentInode.getFirstBlock());
            blockBufferItem.setModified(true);
            Directory parentDir = (Directory) blockBufferItem.getBlock();

            // target path should not exist
            if (!parentDir.hasFreeItem(1))
                throw new DirectoryIsFull();
            if (parentDir.find(targetPath[targetPath.length - 1]) != -1)
                throw new FileOrDirectoryAlreadyExists(targetPath.toString());

            DiskINode sourceInode = bfd.get(sourceEachIndex[sourceEachIndex.length - 1]);
            if (args[0].equals("-s")) {
                // symbol
                if (!MemSuperBlock.getInstance().hasFreeBlock(1))
                    throw new BlockNotEnough();

                // allocate
                Integer freeINodeIndex = bfd.getFreeINodeIndex();
                int[] freeBlocks = MemSuperBlock.getInstance().dispaterBlock(1);
                File newFile = new File(new char[]{}, -1);
                newFile.setData(getAbsolutePath(sourcePath).toCharArray());
                BlockBuffer.getInstance().set(freeBlocks[0], newFile);
                DiskINode diskINode1 = bfd.get(freeINodeIndex);
                diskINode1.initInode(excutor.getDefaultFileMode(), (short) freeBlocks[0], FileType.SYMBOL_LINK, excutor.getUid(), excutor.getGid());
                parentDir.addItem(new DirectoryItem(targetPath[targetPath.length - 1].toCharArray(), freeINodeIndex));
            } else {
                // hard
                parentDir.addItem(new DirectoryItem(targetPath[targetPath.length - 1].toCharArray(), sourceEachIndex[sourceEachIndex.length - 1]));
                sourceInode.incQuote();
            }
        } catch (ProvideNotEnoughParam | NoSuchFileOrDirectory | FileOrDirectoryAlreadyExists | DirectoryIsFull p) {
            sb.append(p + "\n");
        } finally {
            return sb.toString();
        }

    }

    private static String getAbsolutePath(String[] each) {
        StringBuilder sb = new StringBuilder();
        for (String s : each) {
            sb.append("/" + s);
        }
        return sb.toString();
    }
}
