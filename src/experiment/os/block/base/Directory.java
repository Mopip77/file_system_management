package experiment.os.block.base;

import experiment.os.properties.GlobalProperties;

public class Directory extends Block {
    private DirectoryItem[] directoryItems = new DirectoryItem[GlobalProperties.getInt("directory.maxItemPerDirectory")];
    private int size;

    public Directory() {
    }

    public Directory(DirectoryItem[] directoryItems, int size) {
        this.directoryItems = directoryItems;
        this.size = size;
    }

    public DirectoryItem[] getDirectoryItems() {
        return directoryItems;
    }

    public void setDirectoryItems(DirectoryItem[] directoryItems) {
        this.directoryItems = directoryItems;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
