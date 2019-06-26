package experiment.os.system;

import experiment.os.block.base.DiskINode;
import experiment.os.exception.NoFreeINode;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.myEnum.AuthorityType;
import experiment.os.properties.GlobalProperties;
import experiment.os.user.User;

import java.util.ArrayList;
import java.util.stream.Stream;

public class BFD {//i节点表
    private int freeINodes;//空闲可分配的i节点数目
    public static int TOTAL_NUM = GlobalProperties.getInt("INode.totalNum");
    public static int MFD_INDEX = GlobalProperties.getInt("INode.MFDIndex");
    private ArrayList<Integer> freeINodesIndex = new ArrayList<>();
    private DiskINode[] iNodes;

    private static BFD bfd;

    private BFD(){
        iNodes = new DiskINode[TOTAL_NUM];
        for(int i = 0; i < TOTAL_NUM; i++){
            freeINodesIndex.add(i + 2);//第一和第二个i节点已经占用
        }
        freeINodes = TOTAL_NUM - 2;
    }

    public static BFD getInstance() {
        if (bfd == null) {
            bfd = new BFD();
        }
        return bfd;
    }

    //public int getIndex(String[] )
    public DiskINode getFreeINode() throws NoFreeINode {
        if(freeINodes == 0){
            throw new NoFreeINode();
            //return null;
        }else {
            int i = freeINodesIndex.get(0);
            freeINodesIndex.remove(0);
            freeINodes -= 1;
            return iNodes[i];
        }
    }

    public boolean hasAuthority(Integer[] inodeIndexes, User user, AuthorityType...authroityTypes) {
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

    public int getFreeINodes() {
        return freeINodes;
    }

    public ArrayList<Integer> getFreeINodesIndex() {
        return freeINodesIndex;
    }

    public DiskINode[] getiNodes() {
        return iNodes;
    }
}
