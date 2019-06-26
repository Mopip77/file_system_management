package experiment.os.system;

import experiment.os.block.base.Block;
import org.apache.commons.lang3.SerializationUtils;

public class BlockBufferItem {
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