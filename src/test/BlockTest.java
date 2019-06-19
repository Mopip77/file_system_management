package test;

import experiment.os.block.SuperBlock;
import experiment.os.exception.BlockNotEnough;
import experiment.os.system.MemSuperBlock;
import org.junit.Test;

import java.io.*;

public class BlockTest {

    @Test
    public void testFormat() {
//        MemSuperBlock memSuperBlock = new MemSuperBlock();
//        try {
//            memSuperBlock.dispaterBlock(20);
//        } catch (BlockNotEnough blockNotEnough) {
//            blockNotEnough.printStackTrace();
//        }
//        memSuperBlock.recall(7,8,3,2,1,5,0,9,61,59,4);
//        memSuperBlock.save();
    }

    @Test
    public void testLoad() {
        MemSuperBlock memSuperBlock = new MemSuperBlock();
        System.out.println(1);
    }

    @Test
    public void testSave() {
        MemSuperBlock memSuperBlock = new MemSuperBlock();
        try {
            memSuperBlock.dispaterBlock(20);
        } catch (BlockNotEnough blockNotEnough) {
            blockNotEnough.printStackTrace();
        }
        memSuperBlock.recall(7,8,3,2,1,5,0,9,61,59,4);
        memSuperBlock.save();
    }
}
