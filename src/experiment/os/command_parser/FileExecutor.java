package experiment.os.command_parser;

import experiment.os.exception.NoSuchFileOrDirectory;
import experiment.os.user.User;

import java.util.List;

public class FileExecutor implements Executor {
    // open xxx
    // close xxx
    // read xxx
    // write xxx


    @Override
    public String excute(String command, String[] args, String[] currentPath, User excutor) {
        switch (command) {
            case "open":
                return open(args, currentPath, excutor);

            case "close":
                return close(args, currentPath, excutor);

            case "read":
                return read(args, currentPath, excutor);

            case "write":
                return write(args, currentPath, excutor);
            default:
                return null;

        }
    }

    private String open(String[] args, String[] currentPath, User excutor) {
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        // TODO open function
        return null;
    }

    private String close(String[] args, String[] currentPath, User excutor) {
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        // TODO close function
        return null;
    }

    private String read(String[] args, String[] currentPath, User excutor) {
        if (args.length <= 0)
            return null;

        // 只读第一个
        try {
            String[] path = getCombinationPath(args[0], currentPath);
            return null;
            // TODO read function

        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            System.out.println(noSuchFileOrDirectory);
            return null;
        }
    }

    private String write(String[] args, String[] currentPath, User excutor) {
//        if (args.length <= 0)
//            return;
//
//        // 只读第一个
//        try {
//            String[] path = getCombinationPath(args[0], currentPath);
//
//            // TODO write function
//
//        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
//            System.out.println(noSuchFileOrDirectory);
//        }
        return null;
    }

}
