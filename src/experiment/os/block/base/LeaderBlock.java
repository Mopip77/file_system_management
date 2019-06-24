package experiment.os.block.base;

import experiment.os.properties.GlobalProperties;

public class LeaderBlock implements Block {

    int GROUP_SIZE = GlobalProperties.getInt("block.groupSize");

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
