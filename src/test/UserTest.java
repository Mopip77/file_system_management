package test;

import experiment.os.properties.GlobalProperties;
import experiment.os.user.User;
import experiment.os.user.UserManager;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void tt() {
        int asdf = Integer.valueOf(GlobalProperties.get("file.nameMaxLength"));
        System.out.println(asdf);
    }
}
