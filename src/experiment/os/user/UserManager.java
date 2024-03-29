package experiment.os.user;

import experiment.os.myEnum.AuthorityType;
import experiment.os.properties.GlobalProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserManager implements Serializable {

//    private static Scanner scanner = new Scanner(System.in);
    private static String USER_DATA_PATH = GlobalProperties.get("savePath.userDataPath");
    private static short currentUid = 0;
    private static short currentGid = 0;

    private static List<User> registerUsers = new ArrayList<>();

    private UserManager() {
    }

    static {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(USER_DATA_PATH));
            currentUid = (short) ois.readObject();
            currentGid = (short) ois.readObject();
            registerUsers = (List<User>) ois.readObject();
            registerUsers.stream()
                    .forEach(user -> user.setUserOpenFile(new UserOpenFile()));
        } catch (Exception e) {
//            registerUsers = new ArrayList<>();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static User login(String name, String pwd) {
//        System.out.println("[Login]");
//        System.out.print("name: ");
//        String name = scanner.next();
//        System.out.print("password: ");
//        String password = scanner.next();
//
//        User user = null;
//
//        for (User registerUser : registerUsers) {
//            if (registerUser.getName().equals(name) && registerUser.getPassword().equals(password)) {
//                user = registerUser;
//                break;
//            }
//        }
        User loginUser = findByName(name);
        if (loginUser != null && loginUser.getPassword().equals(pwd)) {
            return loginUser;
        }
        return null;
    }

    public static User register(String name, String password) {
        if (findByName(name) != null) {
            return null;
        }
        User u = new User(name, password, currentUid++, currentGid++, AuthorityType.FILE_DEFAULT_AUTHORITY.getMode(),
                AuthorityType.DIRECTORY_DEFAULT_AUTHORITY.getMode());
        registerUsers.add(u);
        return u;
    }

    private static User findByName(String name) {
        User user = null;
        for (User registerUser : registerUsers) {
            if (registerUser.getName().equals(name)) {
                user = registerUser;
                break;
            }
        }
        return user;
    }

    public static void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_PATH));
            oos.writeObject(currentUid);
            oos.writeObject(currentGid);
            oos.writeObject(registerUsers);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
