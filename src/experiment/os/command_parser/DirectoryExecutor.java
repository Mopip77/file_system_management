package experiment.os.command_parser;

import experiment.os.block.base.Block;
import experiment.os.block.base.Directory;
import experiment.os.block.base.DiskINode;
import experiment.os.myEnum.AuthorityType;
import experiment.os.exception.FileOrDirectoryAlreadyExists;
import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.exception.PermisionException;
import experiment.os.myEnum.FileType;
import experiment.os.system.BFD;
import experiment.os.system.BlockBuffer;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DirectoryExecutor implements Executor {
    // cd xxx
    // ls xxx
    // mkdir xxx
    // rm xxx
    // mv xxx xxx
    // cp xxx xxx
    // ln [-s] xxx xxx

    private BFD bfd = BFD.getInstance();

    @Override
    public String excute(String command, String[] args, String[] currentPath, User excutor) {
        switch (command) {
            case "cd":
                return cd(args, currentPath, excutor);

            case "ls":
                return ls(args, currentPath, excutor);

            case "mkdir":
                return mkdir(args, currentPath, excutor);

            case "rm":
                return rm(args, currentPath, excutor);

            case "mv":
                return mv(args, currentPath, excutor);

            case "cp":
                return cp(args, currentPath, excutor);

            case "ln":
                return ln(args, currentPath, excutor);

        }
        return "";
    }

    /**
     * 过滤checkPath 中 为 targetPath 祖先目录的项
     *
     * @param checkPaths
     * @param targetPath
     * @return
     */
    private List<String[]> filterAncestorPath(List<String[]> checkPaths, String[] targetPath) {
        List<String[]> result = new ArrayList<>();
        for (String[] checkPath : checkPaths) {
            boolean isAncient = true;
            for (int i = 0; i < checkPath.length; i++) {
                if (checkPath[i].equals(targetPath[i])) {
                    isAncient = false;
                    break;
                }
            }
            if (isAncient) {
                System.out.println("there is illegal command: " + StringUtils.join(checkPath, "/") + " -> " + StringUtils.join(targetPath, "/"));
            } else {
                result.add(checkPath);
            }
        }
        return result;
    }

    private String cd(String[] args, String[] currentPath, User excutor) {
        // 只取第一个, 如果一个也没有则跳转家目录
        String[] resultPath;
        if (args.length == 0) {
            resultPath = new String[]{excutor.getName()};
        } else {
            try {
                resultPath = getCombinationPath(args[0], currentPath);
            } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
                System.out.println(noSuchFileOrDirectory);
                return "";
            }
        }

        // TODO cd path function
        return "";
    }

    private String ls(String[] args, String[] currentPath, User excutor) {
        String[] resultPath = null;
        try {
            // 只取第一个, 如果一个也没有则为当前目录
            if (args.length == 0) {
                resultPath = currentPath;
            } else {
                resultPath = getCombinationPath(args[0], currentPath);
            }

            // getIndex inode index
            Integer[] eachFolderINodeIndex;
            eachFolderINodeIndex = bfd.getEachIndex(resultPath);

            // 层次的鉴权
            boolean flag = true;
            flag &= bfd.hasAuthority(ArrayUtils.subarray(eachFolderINodeIndex, 0, eachFolderINodeIndex.length - 1), excutor, AuthorityType.EXCUTE);
            if (flag) {
                if ((bfd.get(eachFolderINodeIndex[-1]).getFileType() & FileType.DIRECTORY.getType()) == 0 ||
                        !bfd.hasAuthority(new Integer[]{eachFolderINodeIndex[-1]}, excutor, AuthorityType.READ)) {
                    throw new NoSuchFileOrDirectory(resultPath.toString());
                }

                // print folder in path
                DiskINode diskINode = bfd.get(eachFolderINodeIndex[-1]);
                Directory directory = (Directory) BlockBuffer.getInstance().get(diskINode.getFileType());

                Stream.of(directory.getDirectoryItems())
                        .map(item -> item.getdName() + "\t")
                        .forEach(System.out::print);
            } else {
                throw new NoSuchFileOrDirectory(resultPath.toString());
            }
            return "";
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            System.out.println(new NoSuchFileOrDirectory(resultPath.toString()));
            return "";
        }
    }

    private String mkdir(String[] args, String[] currentPath, User excutor) {
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
        return "";
    }

    private String rm(String[] args, String[] currentPath, User excutor) {
        // 取所有的args, args.length == 0 或者结合后为当前路径的祖先都跳过
        if (args.length == 0)
            return "";

        List<String[]> createPaths = getCombinationPaths(args, currentPath);
        // 过滤祖先文件夹
        List<String[]> filterPaths = filterAncestorPath(createPaths, currentPath);

        // TODO rm path function
        return "";
    }

    private String mv(String[] args, String[] currentPath, User excutor) {
        // 取所有的args 都移动到最后一个 arg, args.length == 0, 1 或者结合后为目标路径的祖先都跳过
        if (args.length <= 1)
            return "";

        List<String[]> createPaths = getCombinationPaths(ArrayUtils.subarray(args, 0, args.length - 1), currentPath);

        String[] targetPath;
        try {
            targetPath = getCombinationPath(args[args.length - 1], currentPath);
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            noSuchFileOrDirectory.printStackTrace();
            return "";
        }

        List<String[]> filterPaths = filterAncestorPath(createPaths, targetPath);

        // TODO mv path function
        return "";
    }

    private String cp(String[] args, String[] currentPath, User excutor) {
        // 只用前两个
        if (args.length < 2) {
            return "";
        }

        try {
            String[] sourcePath = getCombinationPath(args[0], currentPath);
            String[] targetPath = getCombinationPath(args[1], currentPath);

            // 判断source 和 target 的有效性


            // TODO cp function
            return "";
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            noSuchFileOrDirectory.printStackTrace();return "";
        }
    }

    private String ln(String[] args, String[] currentPath, User excutor) {
        // 只用前两个 或 三个
        if (args.length < 2) {
            return "";
        }

        try {
            if (args[0].equals("-s")) {
                // 符号链接
                if (args.length < 3) {
                    return "";
                }

                String[] sourcePath = getCombinationPath(args[1], currentPath);
                String[] targetPath = getCombinationPath(args[2], currentPath);
            } else {
                // 硬链接

                String[] sourcePath = getCombinationPath(args[0], currentPath);
                String[] targetPath = getCombinationPath(args[1], currentPath);

            }
            // TODO link function
            return "";
        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            noSuchFileOrDirectory.printStackTrace();
            return "";
        }
    }
}
