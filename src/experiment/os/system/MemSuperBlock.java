package experiment.os.system;

import experiment.os.block.DataBlocks;
import experiment.os.block.SuperBlock;
import experiment.os.block.base.Block;
import experiment.os.block.base.File;
import experiment.os.block.base.LeaderBlock;
import experiment.os.exception.BlockNotEnough;

import java.sql.DatabaseMetaData;
import java.util.Arrays;

public class MemSuperBlock {

    private SuperBlock diskSuperBlock;

    private int blockSize;
    private int groupSize;

    private int freeBlockSize;
    private Block freeBlock;
    private int[] freeBlockStack;

    boolean modified = false;

    private static MemSuperBlock memSuperBlock;

    /**
     * 如果没有配置文件或读取失败则format
     */
    private MemSuperBlock() {
        diskSuperBlock = SuperBlock.getInstance();
        blockSize = SuperBlock.BLOCK_SIZE;
        groupSize = SuperBlock.GROUP_SIZE;
        freeBlockSize = diskSuperBlock.getFreeBlockSize();
        freeBlock = diskSuperBlock.getFreeBlock();
        freeBlockStack = Arrays.copyOf(diskSuperBlock.getFreeBlockStack(), diskSuperBlock.getFreeBlockStack().length);
    }

    public static MemSuperBlock getInstance() {
        if (memSuperBlock == null) {
            memSuperBlock = new MemSuperBlock();
        }
        return memSuperBlock;
    }

    /**
     * 格式化Super block
     */
    public void format() {
        modified = false; // format then reset
        SuperBlock.getInstance().format();
    }

    /**
     * 分配指定数量的Block
     * @param requireBlockCount
     * @throws BlockNotEnough
     */
    public int[] dispaterBlock(int requireBlockCount) throws BlockNotEnough {
        modified = true;
        if (requireBlockCount > freeBlockSize) {
            throw new BlockNotEnough();
        }

        freeBlockSize -= requireBlockCount;

        int[] dispatcheredBlockIndexes = new int[requireBlockCount];
        for (int i = 0; i < requireBlockCount; i++) {
            int dispatcherBlockIndex;
            if (freeBlockStack[0] == 1) {
                dispatcherBlockIndex = loadNewLeaderBlock();
            } else {
                dispatcherBlockIndex = freeBlockStack[freeBlockStack[0]];
                freeBlockStack[0] = freeBlockStack[0] - 1;
            }
            // TODO 弹出block未接收
            dispatcheredBlockIndexes[i] = dispatcherBlockIndex;
        }
        return dispatcheredBlockIndexes;
    }

    /**
     * 加载新的Leader Block到Super Block
     * @return
     */
    private int loadNewLeaderBlock() {
        modified = true;
        int loadBlockIndex = freeBlockStack[1];

        LeaderBlock nextLeaderBlock = (LeaderBlock) BlockBuffer.getInstance().get(loadBlockIndex);
        int[] leaderStack = nextLeaderBlock.getStack();

        System.arraycopy(leaderStack, 0, freeBlockStack, 0, leaderStack[0] + 1);

        return loadBlockIndex;
    }

    /**
     * 回收多个块
     * @param recallBlockIndex
     */
    public void recall(int... recallBlockIndex) {
        modified = true;

        freeBlockSize += recallBlockIndex.length;

        for (int blockIndex : recallBlockIndex) {
            if (freeBlockStack[0] == groupSize) {

                BlockBuffer.getInstance().set(blockIndex, new LeaderBlock(
                        freeBlockStack[1],
                        Arrays.copyOfRange(freeBlockStack, 2, groupSize + 1)
                ));
                freeBlockStack[0] = 1;
                freeBlockStack[1] = blockIndex;
            } else {
                freeBlockStack[0] = freeBlockStack[0] + 1;
                freeBlockStack[freeBlockStack[0]] = blockIndex;
            }
        }

    }

    /**
     * save
     */
    public void save() {
        if (modified) {
            diskSuperBlock.setFreeBlock(freeBlock);
            diskSuperBlock.setFreeBlockSize(freeBlockSize);
            diskSuperBlock.setFreeBlockStack(freeBlockStack);
            diskSuperBlock.save();
        }
    }

    public void test1() {
        DataBlocks.getInstance().set(10, new File("asdf".toCharArray(), -1));
    }

    public void test2() {
        File block = (File) DataBlocks.getInstance().get(10);
        System.out.println(block.getData());
    }
}
