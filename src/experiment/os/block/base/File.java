package experiment.os.block.base;

public class File extends Block {
    private char[] data = new char[510];
    private File nextFile;

    public File() {
    }

    public File(char[] data, File nextFile) {
        this.data = data;
        this.nextFile = nextFile;
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
