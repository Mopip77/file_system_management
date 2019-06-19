package experiment.os.system;

import experiment.os.block.SuperBlock;
import experiment.os.block.base.Block;
import experiment.os.block.DataBlocks;
import experiment.os.block.base.LeaderBlock;
import experiment.os.exception.BlockNotEnough;

import java.util.Arrays;
import java.util.stream.IntStream;

public class MemSuperBlock {

    private SuperBlock diskSuperBlock;

    static int BLOCK_SIZE = SuperBlock.BLOCK_SIZE;
    static int GROUP_SIZE = SuperBlock.GROUP_SIZE;

    private int blockSize;

    private int freeBlockSize;
    private Block freeBlock;
    private int[] freeBlockStack;

    boolean modified = false;

    /**
     * 传入disk的Super block, 如果没有配置文件或读取失败则format
     */
    public MemSuperBlock() {
        diskSuperBlock = SuperBlock.getInstance();
        if (diskSuperBlock.getFreeBlockStack() == null) {
            format();
        } else {
            blockSize = diskSuperBlock.getBlockSize();
            freeBlockSize = diskSuperBlock.getFreeBlockSize();
            freeBlockStack = Arrays.copyOf(diskSuperBlock.getFreeBlockStack(), diskSuperBlock.getFreeBlockStack().length);
            modified = false;
        }
    }

    /**
     * 格式化Super block
     */
    public void format() {
        modified = true;
        // 分配组长块
        int i;
        for (i = BLOCK_SIZE - 50; i >= 0; i -= 50) {
            DataBlocks.set(i, new LeaderBlock(
                    i == BLOCK_SIZE - 50 ? -1 : i + 50, //last leader bock
                    IntStream.range(i + 1, i + 50).toArray()
            ));
        }

        // first leader block, handle remain block
        int firstLeaderIndex = i + 50;

        // 初始化变量
        blockSize = BLOCK_SIZE;
        freeBlockSize = BLOCK_SIZE;
        freeBlock = DataBlocks.get(firstLeaderIndex - 1);
        freeBlockStack = new int[GROUP_SIZE + 1];
        freeBlockStack[0] = firstLeaderIndex + 1;
        freeBlockStack[1] = firstLeaderIndex;
        for (int j = 0; j < firstLeaderIndex; j++) {
            freeBlockStack[j + 2] = j;
        }
    }

    /**
     * 分配指定数量的Block
     * @param requireBlockCount
     * @throws BlockNotEnough
     */
    public void dispaterBlock(int requireBlockCount) throws BlockNotEnough {
        modified = true;
        if (requireBlockCount > freeBlockSize) {
            throw new BlockNotEnough();
        }

        freeBlockSize -= requireBlockCount;

        for (int i = 0; i < requireBlockCount; i++) {
            int dispacherBlockIndex;
            if (freeBlockStack[0] == 1) {
                dispacherBlockIndex = loadNewLeaderBlock();
            } else {
                dispacherBlockIndex = freeBlockStack[freeBlockStack[0]];
                freeBlockStack[0] = freeBlockStack[0] - 1;
            }
            // TODO 弹出block未接收
        }
    }

    /**
     * 加载新的Leader Block到Super Block
     * @return
     */
    private int loadNewLeaderBlock() {
        modified = true;
        int loadBlockIndex = freeBlockStack[1];

        LeaderBlock nextLeaderBlock = (LeaderBlock) DataBlocks.get(loadBlockIndex);
        int[] leaderStack = nextLeaderBlock.getStack();

        for (int i = 0; i <= leaderStack[0]; i++) {
            freeBlockStack[i] = leaderStack[i];
        }

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
            if (freeBlockStack[0] == GROUP_SIZE) {
                DataBlocks.set(blockIndex, new LeaderBlock(
                        freeBlockStack[1],
                        Arrays.copyOfRange(freeBlockStack, 2, GROUP_SIZE + 1)
                ));
                Block bbb = DataBlocks.get(blockIndex);
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
            BLOCK_SIZE = SuperBlock.BLOCK_SIZE;
            diskSuperBlock.setBlockSize(blockSize);
            diskSuperBlock.setFreeBlock(freeBlock);
            diskSuperBlock.setFreeBlockSize(freeBlockSize);
            diskSuperBlock.setFreeBlockStack(freeBlockStack);
            diskSuperBlock.save();
        }
    }
}
