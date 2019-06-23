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
    private Map<Integer, BlockBufferItem> blockBuffer = new LinkedHashMap<>(MAX_SIZE);
    private int size = 0;

    public Block get(int diskIndex) {
        if (blockBuffer.get(diskIndex) == null) {
            loadDiskBlock(diskIndex);
        }else {
            updateBlockBufferItem(diskIndex);
        }
        return blockBuffer.get(diskIndex).getBlock();
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            writeFirstBlockBack();
        }
        size = 0;
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
            size--;
        }
        blockBuffer.put(diskIndex, new BlockBufferItem(DataBlocks.get(diskIndex)));
        size++;
    }

    private void writeFirstBlockBack() {
        Integer saveBackBlockIndex = (Integer) blockBuffer.keySet().toArray()[0];
        BlockBufferItem saveBackBlock = blockBuffer.remove(saveBackBlockIndex);
        if (saveBackBlock.isModified()) {
            // write back
            DataBlocks.set(saveBackBlockIndex, SerializationUtils.clone(saveBackBlock.getBlock()));
        }
    }

    /**
     * update used buffer item prioity
     */
    private void updateBlockBufferItem(int index) {
        BlockBufferItem blockBufferItem = blockBuffer.get(index);
        blockBuffer.put(index, blockBufferItem);
    }
}
