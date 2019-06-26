package experiment.os.system;

import com.sun.corba.se.impl.encoding.IDLJavaSerializationInputStream;
import experiment.os.block.base.DiskINode;
import org.apache.commons.lang3.RandomUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MemInodeTable {//内存i节点表，一个i节点对应一个已打开的文件

    HashMap<Integer, MemInode> memInodeTable = new HashMap<>();

    private static MemInodeTable memInodeTableInstance;

    private MemInodeTable () {}

    public static MemInodeTable getInstance() {
        if (memInodeTableInstance == null) {
            memInodeTableInstance = new MemInodeTable();
        }
        return memInodeTableInstance;
    }

    public void incQuoteCount(int index) {
        memInodeTable.get(index).incCount();
    }

    public boolean decQuoteCount(int index) {
        return memInodeTable.get(index).decCount();
    }

    /**
     * 返回新创建的mem inode index
     * @param diskINodeIndex
     * @return
     */
    public int allocINode(int diskINodeIndex){
        MemInode memInode = null;
        int findIdx = -1;
        for (Map.Entry<Integer, MemInode> integerINodeEntry : memInodeTable.entrySet()) {
            if (integerINodeEntry.getValue().getDiskInodeIndex() == diskINodeIndex) {
                memInode = integerINodeEntry.getValue();
                findIdx = integerINodeEntry.getKey();
                break;
            }
        }

        if (memInode == null) {
            int unusedIndex = getUnusedIndex();
            memInodeTable.put(unusedIndex, new MemInode(diskINodeIndex));
            return unusedIndex;
        } else {
            memInode.incCount();
            return findIdx;
        }
    }

    public boolean deleteINode(int memItemIndex){
        if (memInodeTable.containsKey(memItemIndex)) {
            boolean zero = memInodeTable.get(memItemIndex).decCount();
            if (zero) {
                MemInode removedInode = memInodeTable.remove(memItemIndex);
                // write back
                if (removedInode.isModified()) {
                    DiskINode diskINode = BFD.getInstance().get(removedInode.getDiskInodeIndex());
                    diskINode.setSize(removedInode.getSize());
                    diskINode.setFirstBlock(removedInode.getFirstBlock());
                    diskINode.setLastBlock(removedInode.getLastBlock());
                    diskINode.setModifyTime(removedInode.getModifyTime());
                }
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public MemInode get(int index) {
        return memInodeTable.get(index);
    }

    private int getUnusedIndex() {
        for (int i = 0; i < 20; i++) {
            if (!memInodeTable.containsKey(i)) {
                return i;
            }
        }
        return 100;
    }

}
