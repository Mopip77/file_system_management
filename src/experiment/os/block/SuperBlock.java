package experiment.os.block;

import experiment.os.block.base.Block;
import experiment.os.block.base.LeaderBlock;
import experiment.os.properties.GlobalProperties;

import java.io.*;
import java.util.stream.IntStream;

public class SuperBlock implements Serializable {

    static String DATA_SAVE_PATH = GlobalProperties.get("savePath.superBlockPath");

    // Initial Constant
    public static int BLOCK_SIZE = GlobalProperties.getInt("block.blockSize");
    public static int GROUP_SIZE = GlobalProperties.getInt("block.groupSize");

    private int freeBlockSize;
    private Block freeBlock; // never used
    private int[] freeBlockStack;

    private static SuperBlock superBlock;

    private SuperBlock() {
        format();
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
                System.out.println("[DEBUG] 没有配置文件, 生成默认SuperBlock");
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

    public void format() {
        // 初始化变量
        freeBlockSize = BLOCK_SIZE;

        // 分配组长块
        int i;
        for (i = BLOCK_SIZE - 50; i >= 0; i -= 50) {
            DataBlocks.getInstance().set(i, new LeaderBlock(
                    i == BLOCK_SIZE - 50 ? -1 : i + 50, //last leader bock
                    IntStream.range(i + 1, i + 50).toArray()
            ));
        }

        // first leader block, handle remain block
        int firstLeaderIndex = i + 50;
        freeBlock = DataBlocks.getInstance().get(firstLeaderIndex - 1);
        freeBlockStack = new int[GROUP_SIZE + 1];
        freeBlockStack[0] = firstLeaderIndex + 1;
        freeBlockStack[1] = firstLeaderIndex;
        for (int j = 0; j < firstLeaderIndex; j++) {
            freeBlockStack[j + 2] = j;
        }
    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(DATA_SAVE_PATH));
            oos.writeObject(superBlock);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
}
