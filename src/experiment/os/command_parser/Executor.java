package experiment.os.command_parser;

import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Executor {
    String excute(String command, String[] args, String[] currentPath, User excutor);

    default String[] getCombinationPath(String appendPath, String[] currentPath) throws NoSuchFileOrDirectory {
        String[] pathItem = appendPath.split("/");
        // 参数路径为绝对路径
        if (appendPath.startsWith("/")) {
            return ArrayUtils.subarray(pathItem, 1, pathItem.length);
        }

        List<String> resultPath = Arrays.asList(currentPath);

        // combine path
        for (String folder : pathItem) {
            switch (folder) {
                case ".":
                    continue;
                case "..":
                    if (resultPath.size() <= 0) {
                        throw new NoSuchFileOrDirectory(StringUtils.join(resultPath, "/"));
                    }
                    resultPath.remove(resultPath.size() - 1);
                    break;
                default:
                    resultPath.add(folder);
            }
        }

        return (String[]) resultPath.toArray();
    }

    default List<String[]> getCombinationPaths(String[] appendPaths, String[] currentPath) {
        List<String[]> result = new ArrayList<>();
        for (String appendPath : appendPaths) {
            String[] combinationPath = new String[0];
            try {
                combinationPath = getCombinationPath(appendPath, currentPath);
                result.add(combinationPath);
            } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
                System.out.println(noSuchFileOrDirectory);
            }
        }
        return result;
    }
}
