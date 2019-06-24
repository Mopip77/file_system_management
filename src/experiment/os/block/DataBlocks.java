package experiment.os.block;

import experiment.os.block.base.Block;
import experiment.os.properties.GlobalProperties;

import java.io.*;

public class DataBlocks implements Serializable {

    private static int BLOCK_SIZE = GlobalProperties.getInt("block.blockSize");
    private static String DATA_SAVE_PATH = GlobalProperties.get("savePath.dataBlockPath");

    private Block[] dataBlocks = new Block[BLOCK_SIZE];

    private static transient volatile DataBlocks dataBlocksInstance;

    private DataBlocks() { }

    public static DataBlocks getInstance() {
        if (dataBlocksInstance == null) {

            synchronized (DataBlocks.class) {
                // load data
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new FileInputStream(DATA_SAVE_PATH));
                    dataBlocksInstance = (DataBlocks) ois.readObject();
                } catch (Exception e) {
                    System.out.println("[DEBUG] 没有配置文件, 生成默认DataBlocks");
                    // 如果读取失败就默认生成空的data block
                    dataBlocksInstance = new DataBlocks();
                    dataBlocksInstance.clear();
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

        }
        return dataBlocksInstance;
    }

    public Block get(int index) {
        if (index >= BLOCK_SIZE || index < 0) {
            throw new IllegalArgumentException();
        }
        return dataBlocks[index];
    }

    public void set(int index, Block block) {
        if (index >= BLOCK_SIZE || index < 0) {
            throw new IllegalArgumentException();
        }
        dataBlocks[index] = block;
    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(DATA_SAVE_PATH));
//            oos.writeObject(dataBlocks);
            oos.writeObject(dataBlocksInstance);
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

    private void clear() {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            dataBlocks[i] = new Block() { };
        }
    }

    protected DataBlocks readResolve() {
        return dataBlocksInstance;
    }
}
