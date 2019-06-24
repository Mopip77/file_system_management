package test;

import experiment.os.system.MemSuperBlock;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Tmp2 {

    public static void main(String[] args) throws FileNotFoundException {
        MemSuperBlock.getInstance().test2();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MemSuperBlock.getInstance().test1();
                MemSuperBlock.getInstance().test2();
            }
        }).start();
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();

//        AuthorityType.EXCUTE
    }

    @Test
    public void test() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "2");
        map.put(2, "3");

        map.entrySet().stream()
                .map(entry -> entry.getValue() + "\t")
                .forEach(System.out::print);
    }
}
