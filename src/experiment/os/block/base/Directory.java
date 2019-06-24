package experiment.os.block.base;

import experiment.os.properties.GlobalProperties;

import java.util.Arrays;

public class Directory implements Block {
    private static int MAX_SIZE = GlobalProperties.getInt("directory.maxItemPerDirectory");
    private DirectoryItem[] directoryItems = new DirectoryItem[MAX_SIZE];
    private int size;

    public Directory() {
    }

    public Directory(DirectoryItem[] directoryItems, int size) {
        this.directoryItems = directoryItems;
        this.size = size;
    }

    public void addItem(DirectoryItem directoryItem) {
        if (size == MAX_SIZE) {
            // TODO
            return;
        }

        directoryItems[size++] = directoryItem;
    }

    public int find(String name) {
        for (DirectoryItem directoryItem : directoryItems) {
            if (name.equals(directoryItem.getdName().toString())) {
                return directoryItem.getdIno();
            }
        }
        return -1;
    }

    public void changeItemName(String before, String after) {
        for (DirectoryItem directoryItem : directoryItems) {
            if (directoryItem.getdName().toString().equals(before)) {
                directoryItem.setdName(after.toCharArray());
            }
        }
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
