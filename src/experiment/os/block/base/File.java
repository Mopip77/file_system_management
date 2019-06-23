package experiment.os.block.base;

import experiment.os.properties.GlobalProperties;

public class File extends Block {
    private char[] data = new char[GlobalProperties.getInt("block.blockSize") - 2];
    private File nextFile;

    public File() {
    }

    public File(char[] data, File nextFile) {
        this.data = data;
        this.nextFile = nextFile;
    }

    @Override
    protected File clone() throws CloneNotSupportedException {
        return (File) super.clone();
    }

    public char[] getData() {
        return data;
    }

    public void setData(char[] data) {
        this.data = data;
    }

    public File getNextFile() {
        return nextFile;
    }

    public void setNextFile(File nextFile) {
        this.nextFile = nextFile;
    }
}
