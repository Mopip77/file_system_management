package experiment.os.command_parser;

import experiment.os.Session;
import experiment.os.block.base.DiskINode;
import experiment.os.block.base.File;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.myEnum.FileType;
import experiment.os.system.BFD;
import experiment.os.system.BlockBuffer;
import experiment.os.system.BlockBufferItem;
import experiment.os.system.MemSuperBlock;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Executor {
    String excute(String command, String[] args, String[] currentPath, Session session);

    default boolean verbosePrint() {return true;}

    default String[] getCombinationPath(String appendPath, String[] currentPath) throws NoSuchFileOrDirectory {
        String[] pathItem = appendPath.split("/");
        // 参数路径为绝对路径
        if (appendPath.startsWith("/")) {
            return ArrayUtils.subarray(pathItem, 1, pathItem.length);
        }

        List<String> resultPath = new ArrayList<>(Arrays.asList(currentPath));

        // combine path
        for (String folder : pathItem) {
            switch (folder) {
                case ".":
                    continue;
                case "..":
                    if (resultPath.size() <= 0) {
                        throw new NoSuchFileOrDirectory(StringUtils.join(resultPath, "/"));
                    }
                    resultPath.remove(resultPath.size() - 1);
                    break;
                default:
                    resultPath.add(folder);
            }
        }

        return resultPath.toArray(new String[]{});
    }

    default List<String[]> getCombinationPaths(String[] appendPaths, String[] currentPath) {
        List<String[]> result = new ArrayList<>();
        for (String appendPath : appendPaths) {
            String[] combinationPath = new String[0];
            try {
                combinationPath = getCombinationPath(appendPath, currentPath);
                result.add(combinationPath);
            } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
                System.out.println(noSuchFileOrDirectory);
            }
        }
        return result;
    }

    default void freeInodeAndBlockWithoutChecking(int diskInodeIndex) {
        BFD bfd = BFD.getInstance();
        DiskINode diskINode = bfd.get(diskInodeIndex);
        if (diskINode.getFileType() == FileType.DIRECTORY.getType()) {
            // free
            MemSuperBlock.getInstance().recall((int) diskINode.getFirstBlock());
        } else {
            // 删除文件
            // 循环
            short firstBlock = diskINode.getFirstBlock();
            short lastBlock = diskINode.getLastBlock();
            List<Integer> deleteBlockIndexes = new ArrayList<>();
            while (firstBlock != lastBlock) {
                deleteBlockIndexes.add((int) firstBlock);
                BlockBufferItem blockBufferItem = BlockBuffer.getInstance().get(firstBlock);
                File file = (File) blockBufferItem.getBlock();
                firstBlock = (short) file.getNextFileIndex();
            }
            deleteBlockIndexes.add((int) lastBlock);
            MemSuperBlock.getInstance().recall(deleteBlockIndexes.toArray(new Integer[0]));
        }
        bfd.recallFreeInode(diskInodeIndex);
    }
}
