package experiment.os.block.base;

import experiment.os.properties.GlobalProperties;

public class File implements Block {
    static int FILE_TEXT_MAX_LENGTH = GlobalProperties.getInt("block.blockSize") - 2;

    private char[] data = new char[FILE_TEXT_MAX_LENGTH];
    private int nextFileIndex;

    public File(char[] data, int nextFileIndex) {
        int textLen = data.length > FILE_TEXT_MAX_LENGTH ? FILE_TEXT_MAX_LENGTH : data.length;
        System.arraycopy(data, 0, this.data, 0, textLen);
        this.nextFileIndex = nextFileIndex;
    }

    public char[] getData() {
        return data;
    }

    public void setData(char[] data) {
        this.data = data;
    }

    public int getNextFileIndex() {
        return nextFileIndex;
    }

    public void setNextFileIndex(int nextFileIndex) {
        this.nextFileIndex = nextFileIndex;
    }
}
