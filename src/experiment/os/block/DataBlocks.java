package experiment.os.block;

import experiment.os.block.base.Block;
import experiment.os.properties.GlobalProperties;

public class DataBlocks {

    static int BLOCK_SIZE = GlobalProperties.getInt("block.blockSize");

    private static Block[] dataBlocks = new Block[BLOCK_SIZE];

    public static Block get(int index) {
        if (index >= BLOCK_SIZE || index < 0) {
            throw new IllegalArgumentException();
        }
        return dataBlocks[index];
    }

    public static void set(int index, Block block) {
        if (index >= BLOCK_SIZE || index < 0) {
            throw new IllegalArgumentException();
        }
        dataBlocks[index] = block;
    }
}
