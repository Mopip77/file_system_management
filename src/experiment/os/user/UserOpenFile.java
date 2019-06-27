package experiment.os.user;

import experiment.os.properties.GlobalProperties;
import experiment.os.system.BFD;
import experiment.os.system.SysOpenFile;
import experiment.os.system.SysOpenFileItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserOpenFile implements Serializable {

    // diskInodeIndex, item
    private Map<Integer, UserOpenFileItem> openFileMap = new HashMap<>();

    public boolean allocate(int diskInodeIndex, String[] path) {
        if (openFileMap.containsKey(diskInodeIndex)) {
            // 同一个用户重复打开一个文件 (有可能是硬链接)
//            UserOpenFileItem userOpenFileItem = openFileMap.get(diskInodeIndex);
//            String[] path1 = userOpenFileItem.getPath();
//            if (path.length == path1.length) {
//                boolean flag = true;
//                for (int i = 0; i < path.length; i++) {
//                    if (!path[i].equals(path1[i])) {
//                        flag = false;
//                        break;
//                    }
//                }
//            } else {
//                openFileMap.put(diskInodeIndex, )
//            }
            return true;
        }

        int sysOpenFileIndex = SysOpenFile.getInstance().addItem(diskInodeIndex);
        if (sysOpenFileIndex == -1) {
            // 不能分配
            return false;
        } else {
            // 新建一个, 或者是另一个用户打开的一个文件
            openFileMap.put(diskInodeIndex, new UserOpenFileItem(diskInodeIndex, sysOpenFileIndex, path));
            return true;
        }
    }

    public boolean delete(int diskInodeIndex) {
        UserOpenFileItem removeItem = openFileMap.remove(diskInodeIndex);

        if (removeItem == null) {
            return true;
        }

        return SysOpenFile.getInstance().delItem(removeItem.getSysOpenFileOffset());
    }

    public int getDiskinodeByPath(String[] path) {
        for (Map.Entry<Integer, UserOpenFileItem> entry : openFileMap.entrySet()) {
            if (Arrays.equals(entry.getValue().getPath(), path)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void clear() {
        Iterator<Map.Entry<Integer, UserOpenFileItem>> iterator = openFileMap.entrySet().iterator();
        while (iterator.hasNext()) {
            delete(iterator.next().getKey());
        }
    }
}