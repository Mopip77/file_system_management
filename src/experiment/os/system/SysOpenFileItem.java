package experiment.os.system;

import experiment.os.system.MemInodeTable;

public class SysOpenFileItem {

    private int diskInodeIndex;
    boolean flag = false;    //文件操作标志
    int memInodeIndex;

    public SysOpenFileItem(int diskInodeIndex, int memInodeIndex) {
        this.diskInodeIndex = diskInodeIndex;
        this.memInodeIndex = memInodeIndex;
    }

    public void incCount() {
        MemInodeTable.getInstance().incQuoteCount(memInodeIndex);
    }

//    public boolean decCount() {
//        return MemInodeTable.getInstance().decQuoteCount(memInodeIndex);
//    }

    public int getDiskInodeIndex() {
        return diskInodeIndex;
    }

    public void setDiskInodeIndex(int diskInodeIndex) {
        this.diskInodeIndex = diskInodeIndex;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getMemInodeIndex() {
        return memInodeIndex;
    }

    public void setMemInodeIndex(int memInodeIndex) {
        this.memInodeIndex = memInodeIndex;
    }
}
