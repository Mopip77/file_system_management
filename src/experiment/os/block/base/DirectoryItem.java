package experiment.os.block.base;

public class DirectoryItem {
    private char[] dName = new char[14];
    private int dIno;

    public DirectoryItem() {
    }

    public DirectoryItem(char[] dName, int dIno) {
        this.dName = dName;
        this.dIno = dIno;
    }

    public int getdIno() {
        return dIno;
    }

    public void setdIno(int dIno) {
        this.dIno = dIno;
    }

    public char[] getdName() {
        return dName;
    }

    public void setdName(char[] dName) {
        this.dName = dName;
    }
}
