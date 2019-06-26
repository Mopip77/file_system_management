package experiment.os.user;

import java.io.Serializable;

public class User implements Serializable{

    private String name;
    private String password;
    private short uid;
    private short gid;
    private short defaultFileMode = 436;
    private short defaultFolderMode = 509;
    private UserOpenFile userOpenFile;

    public User(String name, String password, short uid, short gid, short defaultFileMode, short defaultFolderMode) {
        this.name = name;
        this.password = password;
        this.uid = uid;
        this.gid = gid;
        this.defaultFileMode = defaultFileMode;
        this.defaultFolderMode = defaultFolderMode;
        userOpenFile = new UserOpenFile();
    }

    public void logout() {
        // 关闭所有userOpenFile的item
        userOpenFile.clear();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public short getUid() {
        return uid;
    }

    public void setUid(short uid) {
        this.uid = uid;
    }

    public short getGid() {
        return gid;
    }

    public void setGid(short gid) {
        this.gid = gid;
    }

    public UserOpenFile getUserOpenFile() {
        return userOpenFile;
    }

    public void setUserOpenFile(UserOpenFile userOpenFile) {
        this.userOpenFile = userOpenFile;
    }

    public short getDefaultFileMode() {
        return defaultFileMode;
    }

    public void setDefaultFileMode(short defaultFileMode) {
        this.defaultFileMode = defaultFileMode;
    }

    public short getDefaultFolderMode() {
        return defaultFolderMode;
    }

    public void setDefaultFolderMode(short defaultFolderMode) {
        this.defaultFolderMode = defaultFolderMode;
    }
}
