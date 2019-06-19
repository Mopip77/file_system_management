package experiment.os.command_parser;

import experiment.os.exception.FileOrDirectoryAlreadyExists;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DirectoryExecutor implements Executor {
    // cd xxx
    // ls xxx
    // mkdir xxx
    // rm xxx
    // mv xxx xxx
    // cp xxx xxx
    // ln [-s] xxx xxx

    @Override
    public void excute(String command, String[] args, String[] currentPath, User excutor) {
        switch (command) {
            case "cd":
                cd(args, currentPath, excutor);
                break;
            case "ls":
                ls(args, currentPath, excutor);
                break;
            case "mkdir":
                mkdir(args, currentPath, excutor);
                break;
            case "rm":
                rm(args, currentPath, excutor);
                break;
            case "mv":
                mv(args, currentPath, excutor);
                break;
            case "cp":
                cp(args, currentPath, excutor);
                break;
            case "ln":
                ln(args, currentPath, excutor);
                break;
        }
    }

    /**
     * 过滤checkPath 中 为 targetPath 祖先目录的项
     * @param checkPaths
     * @param targetPath
     * @return
     */
    private List<String[]> filterAcientPath(List<String[]> checkPaths, String[] targetPath) {
        List<String[]> result = new ArrayList<>();
        for (String[] checkPath : checkPaths) {
            boolean isAncient = true;
            for (int i = 0; i < checkPath.length; i++) {
                if (checkPath[i].equals(targetPath[i])) {
                    isAncient = false;
                    break;
                }
            }
            if (isAncient){
                System.out.println("there is illegal command: " + StringUtils.join(checkPath, "/") + " -> " + StringUtils.join(targetPath, "/"));
            } else {
                result.add(checkPath);
            }
        }
        return result;
    }

    private void cd(String[] args, String[] currentPath, User excutor) {
        // 只取第一个, 如果一个也没有则跳转家目录
        String[] resultPath;
        if (args.length == 0) {
            resultPath = new String[] {excutor.getName()};
        } else {
            try {
                resultPath = getCombinationPath(args[0], currentPath);
            } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
                System.out.println(noSuchFileOrDirectory);
                return;
            }
        }

        // TODO cd path function
    }

    private void ls(String[] args, String[] currentPath, User excutor) {
        // 只取第一个, 如果一个也没有则为当前目录
        String[] resultPath;
        if (args.length == 0) {
            resultPath = currentPath;
        } else {
            try {
                resultPath = getCombinationPath(args[0], currentPath);
            } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
                System.out.println(noSuchFileOrDirectory);
                return;
            }
        }

        // TODO ls path function
    }

    private void mkdir(String[] args, String[] currentPath, User excutor) {
        // 取所有的args
        if (args.length == 0) {
            try {
                throw new FileOrDirectoryAlreadyExists(StringUtils.join(currentPath, "/"));
            } catch (FileOrDirectoryAlreadyExists fileOrDirectoryAlreadyExists) {
                System.out.println(fileOrDirectoryAlreadyExists);
            }
        } else {
            List<String[]> createPaths = getCombinationPaths(args, currentPath);
            // TODO mkdir path function
        }
    }

    private void rm(String[] args, String[] currentPath, User excutor) {
        // 取所有的args, args.length == 0 或者结合后为当前路径的祖先都跳过
        if (args.length == 0)
            return;

        List<String[]> createPaths = getCombinationPaths(args, currentPath);
        // 过滤祖先文件夹
        List<String[]> filterPaths = filterAcientPath(createPaths, currentPath);

        // TODO rm path function
    }

    private void mv(String[] args, String[] currentPath, User excutor) {
        // 取所有的args 都移动到最后一个 arg, args.length == 0, 1 或者结合后为目标路径的祖先都跳过
        if (args.length <= 1)
            return;

        List<String[]> createPaths = getCombinationPaths(ArrayUtils.subarray(args, 0, args.length - 1), currentPath);

        String[] targetPath;
        try {
            targetPath = getCombinationPath(args[args.length - 1], currentPath);
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            noSuchFileOrDirectory.printStackTrace();
            return;
        }

        List<String[]> filterPaths = filterAcientPath(createPaths, targetPath);

        // TODO mv path function
    }

    private void cp(String[] args, String[] currentPath, User excutor) {
        // 只用前两个
        if (args.length < 2) {
            return;
        }

        try {
            String[] sourcePath = getCombinationPath(args[0], currentPath);
            String[] targetPath = getCombinationPath(args[1], currentPath);

            // 判断source 和 target 的有效性


            // TODO cp function

        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            noSuchFileOrDirectory.printStackTrace();
        }
    }

    private void ln(String[] args, String[] currentPath, User excutor) {
        // 只用前两个 或 三个
        if (args.length < 2) {
            return;
        }

        try {
            if (args[0].equals("-s")) {
                // 符号链接
                if (args.length < 3) {
                    return;
                }

                String[] sourcePath = getCombinationPath(args[1], currentPath);
                String[] targetPath = getCombinationPath(args[2], currentPath);
            } else {
                // 硬链接

                String[] sourcePath = getCombinationPath(args[0], currentPath);
                String[] targetPath = getCombinationPath(args[1], currentPath);

            }
            // TODO link function
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            noSuchFileOrDirectory.printStackTrace();
        }
    }
}
