package experiment.os.block.base;

import experiment.os.myEnum.FileType;

import java.io.Serializable;

public class DiskINode implements Serializable {
    private int quoteNum;//引用计数
    private short mode;//存取权限
    private short firstBlock;
    private short lastBlock;
    private long size;//文件大小
    private int fileType;//文件类型,文件或文件夹
    private short userId;
    private short groupId;//可以按组设置权限
    private long createTime;//文件创建信息
    private long modifyTime;//文件最近修改时间

    public DiskINode() { }

    public void initInode(short mode, short firstBlock, FileType fileType, short userId, short groupId) {
        this.mode = mode;
        this.firstBlock = firstBlock;
        this.fileType = fileType.getType();
        this.userId = userId;
        this.groupId = groupId;

        quoteNum = 1;
        lastBlock = firstBlock;
        size = 0;
        createTime = System.currentTimeMillis();
        modifyTime = createTime;
    }

    public void incQuote() {
        this.quoteNum++;
    }

    public int getQuoteNum() {
        return quoteNum;
    }

    public short getMode() {
        return mode;
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

    public short getUserId() {
        return userId;
    }

    public short getGroupId() {
        return groupId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
}
