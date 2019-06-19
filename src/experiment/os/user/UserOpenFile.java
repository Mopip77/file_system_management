package experiment.os.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserOpenFile implements Serializable {

    transient private static int MAX_OPEN_FILE_COUNT = 10;

    private short mode;
    private short uid;
    private short gid;
    private int[] openFileInodeIndexes;

    public UserOpenFile(short mode, short uid, short gid) {
        this.mode = mode;
        this.uid = uid;
        this.gid = gid;
        openFileInodeIndexes = new int[MAX_OPEN_FILE_COUNT];
    }
}
