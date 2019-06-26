package test;

import experiment.os.user.UserManager;
import org.junit.Test;

public class UserTest {

    @Test
    public void add() {
        UserManager.register("user1", "asdf");
        UserManager.save();
    }
}
