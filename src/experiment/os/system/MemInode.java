package experiment.os.system;

import experiment.os.block.base.DiskINode;
import experiment.os.myEnum.AuthorityType;
import experiment.os.user.User;

import java.util.stream.Stream;

public class MemInode {
    private boolean modified;
    private int diskInodeIndex;  //磁盘索引节点编号
    private int quoteNum;  //引用计数
    private int linkedFileNum; //关联文件数
    private short mode;  //存取权限
    private short uerID; //磁盘索引节点用户id
    private short groupID; //磁盘索引节点组id
    private short firstBlock;
    private short lastBlock;
    private long size; //文件大小
    private long createTime; //文件创建信息
    private long modifyTime; //文件最近修改时间

    public MemInode(int diskInodeIndex) {
        this.diskInodeIndex = diskInodeIndex;
        quoteNum = 1;
        modified = false;
        DiskINode diskINode = BFD.getInstance().get(diskInodeIndex);
        linkedFileNum = diskINode.getQuoteNum();
        mode = diskINode.getMode();
        uerID = diskINode.getUserId();
        groupID = diskINode.getGroupId();
        firstBlock = diskINode.getFirstBlock();
        lastBlock = diskINode.getLastBlock();
        size = diskINode.getSize();
        createTime = diskINode.getCreateTime();
        modifyTime = diskINode.getModifyTime();
    }

    public void incCount() {
        quoteNum++;
    }

    public boolean decCount() {
        return --quoteNum == 0;
    }

    public void modify() {
        modified = true;
    }

    public boolean hasAuthority(User user, AuthorityType...authroityTypes) {

        return AuthorityType.hasAuthority(
                user.getUid(),
                user.getGid(),
                uerID,
                groupID,
                mode,
                authroityTypes);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public int getDiskInodeIndex() {
        return diskInodeIndex;
    }

    public void setDiskInodeIndex(int diskInodeIndex) {
        this.diskInodeIndex = diskInodeIndex;
    }

    public int getQuoteNum() {
        return quoteNum;
    }

    public void setQuoteNum(int quoteNum) {
        this.quoteNum = quoteNum;
    }

    public int getLinkedFileNum() {
        return linkedFileNum;
    }

    public void setLinkedFileNum(int linkedFileNum) {
        this.linkedFileNum = linkedFileNum;
    }

    public short getMode() {
        return mode;
    }

    public void setMode(short mode) {
        this.mode = mode;
    }

    public short getUerID() {
        return uerID;
    }

    public void setUerID(short uerID) {
        this.uerID = uerID;
    }

    public short getGroupID() {
        return groupID;
    }

    public void setGroupID(short groupID) {
        this.groupID = groupID;
    }

    public short getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(short firstBlock) {
        this.firstBlock = firstBlock;
    }

    public short getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(short lastBlock) {
        this.lastBlock = lastBlock;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
}




