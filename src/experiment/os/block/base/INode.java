package experiment.os.block.base;

public class INode{//内存i节点
    private int iFlag;//iNode编号
    private int quoteNum;//引用计数
    private short access;//存取权限
    private short firstBlock;
    private short lastBlock;
    private long size;//文件大小
    private int fileType;//文件类型,文件或文件夹
    private short userId;
    private short groupId;//可以按组设置权限
    private long createTime;//文件最近修改时间
    private long modifyTime;//文件最近修改时间

    public INode(int iFlag, short access, short firstBlock, short lastBlock, long size, int fileType, short userId, short groupId) {
        this.iFlag = iFlag;
        this.access = access;
        this.firstBlock = firstBlock;
        this.lastBlock = lastBlock;
        this.size = size;
        this.fileType = fileType;
        this.userId = userId;
        this.groupId = groupId;

        quoteNum = 1;
        createTime = System.currentTimeMillis();
        modifyTime = createTime;
    }

    public int getiFlag() {
        return iFlag;
    }

    public void setiFlag(int iFlag) {
        this.iFlag = iFlag;
    }

    public int getQuoteNum() {
        return quoteNum;
    }

    public void setQuoteNum(int quoteNum) {
        this.quoteNum = quoteNum;
    }

    public short getAccess() {
        return access;
    }

    public void setAccess(short access) {
        this.access = access;
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

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public short getUserId() {
        return userId;
    }

    public void setUserId(short userId) {
        this.userId = userId;
    }

    public short getGroupId() {
        return groupId;
    }

    public void setGroupId(short groupId) {
        this.groupId = groupId;
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
