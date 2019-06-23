package experiment.os.block;

import experiment.os.block.base.Block;
import experiment.os.properties.GlobalProperties;

import java.io.*;

public class SuperBlock implements Serializable {

    static String DATA_SAVE_PATH = GlobalProperties.get("savePath.dataBlockPath");

    // Initial Constant
    public static int BLOCK_SIZE = GlobalProperties.getInt("block.blockSize");
    public static int GROUP_SIZE = GlobalProperties.getInt("block.groupSize");

    private int blockSize = BLOCK_SIZE;

    private int freeBlockSize;
    private Block freeBlock;
    private int[] freeBlockStack;

    boolean modified = false;

    private static SuperBlock superBlock;

    private SuperBlock() {
        blockSize = BLOCK_SIZE;
        freeBlockSize = BLOCK_SIZE;
        freeBlockStack = null;
    }

    /**
     * 返回磁盘super block 的实例, 如果读取失败freeBlockStack为null, 用这个判断
     * @return
     */
    public static SuperBlock getInstance() {
        if (superBlock == null) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(DATA_SAVE_PATH));
                superBlock = (SuperBlock) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                // 如果读取失败就默认生成空的supber block
                superBlock = new SuperBlock();
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return superBlock;
    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(DATA_SAVE_PATH));
            oos.writeObject(superBlock);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int size) {
        blockSize = size;
    }

    public int getFreeBlockSize() {
        return freeBlockSize;
    }

    public void setFreeBlockSize(int freeBlockSize) {
        this.freeBlockSize = freeBlockSize;
    }

    public Block getFreeBlock() {
        return freeBlock;
    }

    public void setFreeBlock(Block freeBlock) {
        this.freeBlock = freeBlock;
    }

    public int[] getFreeBlockStack() {
        return freeBlockStack;
    }

    public void setFreeBlockStack(int[] freeBlockStack) {
        this.freeBlockStack = freeBlockStack;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public static int getGroupSize() {
        return GROUP_SIZE;
    }

    public static void setGroupSize(int groupSize) {
        GROUP_SIZE = groupSize;
    }
}
