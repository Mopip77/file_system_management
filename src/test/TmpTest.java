package test;

import experiment.os.block.DataBlocks;
import experiment.os.block.base.File;
import experiment.os.exception.BlockNotEnough;
import experiment.os.system.BlockBuffer;
import experiment.os.system.MemSuperBlock;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class TmpTest {

    @Test
    public void save() throws BlockNotEnough {
        // load memory super block
        MemSuperBlock memSuperBlock = MemSuperBlock.getInstance();
//        File block = (File) BlockBuffer.getIndex(10);
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
        BlockBuffer.getInstance().save();
    }

    @Test
    public void load() {
        MemSuperBlock memSuperBlock = MemSuperBlock.getInstance();
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

    @Test
    public void load2() throws FileNotFoundException {
//        MemSuperBlock memSuperBlock = new MemSuperBlock();
        DataBlocks.getInstance().set(10, new File("change".toCharArray(), -1));
        File block = (File) BlockBuffer.getInstance().get(10);
        System.out.println(new String(block.getData(), 0, block.getData().length));
        InputStream is = new FileInputStream("tet.tet");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    @Test
    public void load3() {
//        MemSuperBlock memSuperBlock = new MemSuperBlock();
        File block = (File) BlockBuffer.getInstance().get(10);
        System.out.println(new String(block.getData(), 0, block.getData().length));
    }

    public static void main(String[] args) throws FileNotFoundException {
        MemSuperBlock.getInstance().test1();
        MemSuperBlock.getInstance().test2();
//        System.out.println(DataBlocks.getInstance().hashCode());
//        File block = (File) DataBlocks.getInstance().getIndex(10);
//        System.out.println(new String(block.getData(), 0, block.getData().length));
//        DataBlocks.getInstance().set(10, new File("change".toCharArray(), -1));
//
//        block = (File) DataBlocks.getInstance().getIndex(10);
//        System.out.println(new String(block.getData(), 0, block.getData().length));
//
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
//        System.out.println(s);
//        DataBlocks.getInstance().set(10, new File(s.toCharArray(), -1));
//        s = scanner.nextLine();
    }
}
