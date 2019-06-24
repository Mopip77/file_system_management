package experiment.os.system;

import experiment.os.block.base.Block;
import experiment.os.block.base.Directory;
import experiment.os.block.base.INode;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.properties.GlobalProperties;

import javax.swing.*;
import java.io.FilenameFilter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileNameIndex {
    private int MAX_SIZE = GlobalProperties.getInt("fileNameIndex.maxSize");
    private Map<String, Integer> map = new LinkedHashMap<>(MAX_SIZE, 0.75f, true);
    private int size = 0;

    private static FileNameIndex fileNameIndex;
    private FileNameIndex() {}

    public static FileNameIndex getInstance() {
        if (fileNameIndex == null) {
            fileNameIndex = new FileNameIndex();
        }
        return fileNameIndex;
    }

    /**
     * 传入path, 返回每一级的i节点
     * @param path
     * @return
     */
    public Integer[] getEach(String[] path) throws NoSuchFileOrDirectory {
        return getEachFromPos(BFD.MFD_INDEX, "", path);
    }

    /**
     * 传入起始路径(必须知道i节点编号) 和 起始路径的i节点编号, 和剩余的路径, 返回剩余路径的索引数组
     * @param parentInodeIdx
     * @param currentPath
     * @param absentPath
     * @return
     * @throws NoSuchFileOrDirectory
     */
    private Integer[] getEachFromPos(int parentInodeIdx, String currentPath, String[] absentPath) throws NoSuchFileOrDirectory {
        StringBuilder sb = new StringBuilder(currentPath);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < absentPath.length; i++) {
            sb.append("/" + absentPath[i]);
            if (map.containsKey(sb.toString())) {
                result.add(map.get(sb.toString()));
                continue;
            }
            // 加载上一级目录
            INode parentInode;
            Directory parentDirectory;

            parentInode = BFD.getInstace().get(parentInodeIdx);
            parentDirectory = (Directory) BlockBuffer.getInstance().get(parentInode.getFirstBlock());
            int idx = parentDirectory.find(absentPath[i]);
            if (idx == -1) {
                throw new NoSuchFileOrDirectory("FileNameIndex 77");
            }
            add(sb.toString(), idx);
            result.add(idx);
            parentInodeIdx = idx;
        }
        return (Integer[]) result.toArray();
    }

    /**
     * 传入path, 只返回path的i节点
     * @param path
     * @return
     * @throws NoSuchFileOrDirectory
     */
    public Integer get(String[] path) throws NoSuchFileOrDirectory {
        StringBuilder sb = new StringBuilder();
        List<String> absent = new ArrayList<>();

        Stream.of(path).forEach(e -> sb.append("/" + e));
        if (map.containsKey(sb.toString())) {
            return map.get(sb.toString());
        }

        // find the nearest
        while (sb.length() > 0 && !map.containsKey(sb)) {
            int lastIdx = sb.lastIndexOf("/");
            absent.add(0, sb.substring(lastIdx + 1));
            sb.delete(lastIdx, sb.length());
        }

        // 循环找缺失的
        if (sb.length() == 0) {
            Integer[] each = getEach(path);
            return each[-1];
        } else {
            Integer[] eachFromPos = getEachFromPos(map.get(sb.toString()), sb.toString(), (String[]) absent.toArray());
            return eachFromPos[-1];
        }
    }

    public void add(String path, Integer inodeIdx) {
        if (map.containsKey(path)) {
            size--;
        }else if (size == MAX_SIZE) {
            map.remove(map.keySet().toArray()[0]);
            size--;
        }
        map.put(path, inodeIdx);
        size++;
    }

    public void removeAllDescendant(String basePath) {
        if (!map.containsKey(basePath))
            return;

        Set<String> descendants = map.keySet().stream().filter(e -> e.startsWith(basePath)).collect(Collectors.toSet());
        for (String descendant : descendants) {
            map.remove(descendant);
            size--;
        }
    }
}
