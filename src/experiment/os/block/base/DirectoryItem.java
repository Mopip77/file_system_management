package experiment.os.block.base;

import experiment.os.properties.GlobalProperties;

import java.io.Serializable;

public class DirectoryItem implements Serializable {
    private char[] dName = new char[GlobalProperties.getInt("file.nameMaxLength")];
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

    public char[] getdName() {
        return dName;
    }
}
