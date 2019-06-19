package test;

import experiment.os.user.User;
import experiment.os.user.UserManager;
import org.junit.Test;

import java.util.List;
import java.util.Scanner;

public class UserTest {

    @Test
    public void test() {
        UserManager.register();
        UserManager.save();
    }

    @Test
    public void testLoad() {
        System.out.println(UserManager.getCurrentGid());
        System.out.println(UserManager.getCurrentUid());
        List<User> registerUsers = UserManager.getRegisterUsers();
        System.out.println(registerUsers);

    }
}
