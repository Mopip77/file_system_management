package test;

import experiment.os.user.UserManager;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserTest {

    @Test
    public void add() {
        List<Short> aa = new ArrayList<>();
        aa.add((short)1);
        aa.add((short)2);
        Short[] shorts = aa.toArray(new Short[0]);
        rrr(shorts);
//        Arrays.copyOf(aa.toArray());
//        rrr(aa.stream().map(e -> (short) e);
    }

    public static  void rrr(Short ...a) {
        for (short i : a) {
            System.out.println(i);
        }
    }

    @Test
    public void asdf() {
        char[] mychar = new char[200];
        char[] chars = "asdf".toCharArray();
        System.arraycopy(chars, 0, mychar, 0, chars.length);
        System.out.println(mychar.length);
        System.out.println(new String(mychar));
    }

    @Test
    public void e() {
        System.out.println((7 << 6) + (7 << 3) + 7);
    }
}
