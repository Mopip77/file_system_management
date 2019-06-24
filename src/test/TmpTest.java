package test;

import experiment.os.block.DataBlocks;
import experiment.os.block.base.File;
import experiment.os.exception.BlockNotEnough;
import experiment.os.system.BlockBuffer;
import experiment.os.system.MemSuperBlock;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class TmpTest {

    @Test
    public void save() throws BlockNotEnough {
        // load memory super block
        MemSuperBlock memSuperBlock = new MemSuperBlock();
//        File block = (File) BlockBuffer.get(10);
//        System.out.println(1);
        int[] blocks = memSuperBlock.dispaterBlock(14);
        for (int idx : blocks) {
            if ((idx & 1) == 0) {
                BlockBuffer.getInstance().set(idx, new File(String.valueOf(idx).toCharArray(), -1));
                File fff = (File) BlockBuffer.getInstance().get(idx);
//                System.out.println(1);
            }
        }
//
        memSuperBlock.save();
        BlockBuffer.getInstance().clear();
        DataBlocks.getInstance().save();
    }

    @Test
    public void load() {
        MemSuperBlock memSuperBlock = new MemSuperBlock();
        File block = (File) BlockBuffer.getInstance().get(10);
        System.out.println(1);
    }

    @Test
    public void fifo() {
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>(2, 0.75f, true);
        map.put(1, 2);
        map.put(2, 3);
        map.put(3, 4);
//        System.out.println(map.keySet());
        System.out.println(map.entrySet());
        map.get(1);
        System.out.println(map.entrySet());
//        System.out.println(map.keySet());
    }
}
