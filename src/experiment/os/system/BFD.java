package experiment.os.system;

import experiment.os.block.SuperBlock;
import experiment.os.block.base.Directory;
import experiment.os.block.base.DiskINode;
import experiment.os.exception.BlockNotEnough;
import experiment.os.exception.NoFreeINode;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.myEnum.AuthorityType;
import experiment.os.myEnum.FileType;
import experiment.os.properties.GlobalProperties;
import experiment.os.user.User;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class BFD implements Serializable {//i节点表

    private static final String SAVE_PATH = GlobalProperties.get("savePath.BFDPath");

    private int freeINodes;//空闲可分配的i节点数目
    public static int TOTAL_NUM = GlobalProperties.getInt("INode.totalNum");
    public static int MFD_INDEX = GlobalProperties.getInt("INode.MFDIndex");
    private ArrayList<Integer> freeINodesIndex = new ArrayList<>();
    private DiskINode[] iNodes;

    private static BFD bfd;

    private BFD() {
        iNodes = new DiskINode[TOTAL_NUM];
        for (int i = 0; i < TOTAL_NUM; i++) {
            iNodes[i] = new DiskINode();
        }

        for (int i = 0; i < TOTAL_NUM; i++) {
            freeINodesIndex.add(i + 2);//第一和第二个i节点已经占用
        }
        freeINodes = TOTAL_NUM - 2;

        // MFD 分配磁盘块
        try {
            int[] index = MemSuperBlock.getInstance().dispaterBlock(1);
            BlockBuffer.getInstance().set(index[0], new Directory());
            iNodes[MFD_INDEX].initInode((short) 511, (short) index[0], FileType.DIRECTORY, (short) -1, (short) -1);
        } catch (BlockNotEnough blockNotEnough) {
            blockNotEnough.printStackTrace();
        }
    }

    public static BFD getInstance() {
        if (bfd == null) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(SAVE_PATH));
                bfd = (BFD) ois.readObject();
            } catch (Exception e) {
                System.out.println("[DEBUG] 没有配置文件, 生成默认BFD");
                // 如果读取失败就默认生成空的supber block
                bfd = new BFD();
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
        return bfd;
    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(SAVE_PATH));
            oos.writeObject(bfd);
            oos.flush();
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

    public boolean hasAuthority(Integer[] inodeIndexes, User user, AuthorityType... authroityTypes) {
        if (inodeIndexes.length == 0) {
            return true;
        }

        return Stream.of(inodeIndexes).allMatch(idx -> AuthorityType.hasAuthority(
                user.getUid(),
                user.getGid(),
                iNodes[idx].getUserId(),
                iNodes[idx].getGroupId(),
                iNodes[idx].getMode(),
                authroityTypes)
        );
    }

    public Integer[] getEachIndex(String[] path) throws NoSuchFileOrDirectory {
        return FileNameIndex.getInstance().getEach(path);
    }

    public Integer getIndex(String[] path) throws NoSuchFileOrDirectory {
        return FileNameIndex.getInstance().get(path);
    }

    public DiskINode get(int index) {
        if (index < 0 || index > TOTAL_NUM) {
            return null;
        }
        return iNodes[index];
    }

    public boolean hasFreeInode(int requireCount) {
        return freeINodesIndex.size() >= requireCount;
    }

    public ArrayList<Integer> getFreeINodesIndex() {
        return freeINodesIndex;
    }

    public Integer getFreeINodeIndex() {
        if (freeINodesIndex.size() > 0) {
            return freeINodesIndex.remove(0);
        } else {
            return null;
        }
    }

    public void recallFreeInode(int index) {
        freeINodesIndex.add(index);
    }
}
