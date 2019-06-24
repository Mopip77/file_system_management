package experiment.os.system;

import experiment.os.block.DataBlocks;
import experiment.os.block.base.Block;
import experiment.os.properties.GlobalProperties;
import org.apache.commons.lang3.SerializationUtils;

import java.util.LinkedHashMap;
import java.util.Map;

class BlockBufferItem {
    private Block block;
    private boolean modified = false;

    public BlockBufferItem(Block block) {
        this.block = SerializationUtils.clone(block);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Block getBlock() {
        return block;
    }
}

public class BlockBuffer {

    private int MAX_SIZE = GlobalProperties.getInt("blockBuffer.maxSize");

    // <diskIndex, BlockItem>
    private Map<Integer, BlockBufferItem> blockBuffer = new LinkedHashMap<>(MAX_SIZE, 0.75f, true);
    private int size = 0;

    private static BlockBuffer blockBufferInstance = null;

    private BlockBuffer() {}

    public static BlockBuffer getInstance() {
        if (blockBufferInstance == null) {
            blockBufferInstance = new BlockBuffer();
        }
        return blockBufferInstance;
    }

    public Block get(int diskIndex) {
        if (blockBuffer.get(diskIndex) == null) {
            loadDiskBlock(diskIndex);
        }
        return blockBuffer.get(diskIndex).getBlock();
    }

    public void set(int diskIndex, Block block) {
        if (blockBuffer.containsKey(diskIndex)) {
            size--;
        } else if (size == MAX_SIZE) {
            writeFirstBlockBack();
        }
        BlockBufferItem blockBufferItem = new BlockBufferItem(block);
        blockBufferItem.setModified(true);
        blockBuffer.put(diskIndex, blockBufferItem);
        size++;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            writeFirstBlockBack();
        }
        size = 0;
    }

    public void save() {
        for (int i = 0; i < size; i++) {
            writeFirstBlockBack();
        }
        DataBlocks.getInstance().save();
    }

    public void setBlockModified(int diskIndex) {
        BlockBufferItem blockBufferItem = blockBuffer.get(diskIndex);
        if (blockBufferItem != null) {
            blockBufferItem.setModified(true);
        }
    }

    /**
     * load disk block if full write back, need sync skip
     * @param diskIndex
     */
    private void loadDiskBlock(int diskIndex) {
        if (size == MAX_SIZE) {
            writeFirstBlockBack();
        }
        Block diskBlock = DataBlocks.getInstance().get(diskIndex);
        // 从硬盘读入内存 需要copy一份
        blockBuffer.put(diskIndex, new BlockBufferItem(SerializationUtils.clone(diskBlock)));
        size++;
    }

    private void writeFirstBlockBack() {
        size--;
        Integer saveBackBlockIndex = (Integer) blockBuffer.keySet().toArray()[0];
        BlockBufferItem saveBackBlock = blockBuffer.remove(saveBackBlockIndex);
        if (saveBackBlock.isModified()) {
            // write back
            DataBlocks.getInstance().set(saveBackBlockIndex, saveBackBlock.getBlock());
        }
    }
}
