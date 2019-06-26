package experiment.os.block.base;

import experiment.os.exception.DirectoryIsFull;
import experiment.os.properties.GlobalProperties;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Directory implements Block {
    private static int MAX_SIZE = GlobalProperties.getInt("directory.maxItemPerDirectory");
    private DirectoryItem[] directoryItems = new DirectoryItem[MAX_SIZE];
    private int size;

    public Directory() {
    }

    public boolean hasFreeItem(int requireCount) {
        return size + requireCount <= MAX_SIZE;
    }

    public void addItem(DirectoryItem directoryItem) throws DirectoryIsFull {
        if (size == MAX_SIZE) {
            // TODO
            throw new DirectoryIsFull();
        }

        directoryItems[size++] = directoryItem;
    }

    public DirectoryItem removeItem(String name) {
        int idx = -1;
        DirectoryItem result = null;
        for (int i = 0; i < size; i++) {
            if (name.equals(new String(directoryItems[i].getdName()))) {
                idx = i;
                result = directoryItems[i];
                break;
            }
        }

        if (idx == -1) {
            return null;
        }

        System.arraycopy(directoryItems, idx + 1, directoryItems, idx, MAX_SIZE - idx - 1);
        size--;
        return result;
    }

    public int find(String name) {
        if (size == 0)
            return -1;
        for (int i = 0; i < size; i++) {
            if (name.equals(new String(directoryItems[i].getdName()))) {
                return directoryItems[i].getdIno();
            }
        }
        return -1;
    }

    public DirectoryItem[] getDirectoryItems() {
        return ArrayUtils.subarray(directoryItems, 0, size);
    }

    public int getSize() {
        return size;
    }
}
