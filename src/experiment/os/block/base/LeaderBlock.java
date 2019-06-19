package experiment.os.block.base;

import experiment.os.block.base.Block;

public class LeaderBlock extends Block {

    int GROUP_SIZE = 50;

    private int[] freeBlockStack = new int[GROUP_SIZE + 1];

    public LeaderBlock(int nextLeaderIndex, int[] emptyBlockIndexs) {
        freeBlockStack[0] = GROUP_SIZE;
        freeBlockStack[1] = nextLeaderIndex;
        for (int i = 0; i < emptyBlockIndexs.length; i++) {
            freeBlockStack[i + 2] = emptyBlockIndexs[i];
        }
    }

    public int[] getStack() {
        return freeBlockStack;
    }
}
