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
    public void excute(String command, String[] args, String[] currentPath, User excutor) {
        switch (command) {
            case "open":
                open(args, currentPath, excutor);
                break;
            case "close":
                close(args, currentPath, excutor);
                break;
            case "read":
                read(args, currentPath, excutor);
                break;
            case "write":
                write(args, currentPath, excutor);
                break;
        }
    }

    private void open(String[] args, String[] currentPath, User excutor) {
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        // TODO open function
    }

    private void close(String[] args, String[] currentPath, User excutor) {
        // 加载全部args
        List<String[]> combinationPaths = getCombinationPaths(args, currentPath);

        // TODO close function
    }

    private void read(String[] args, String[] currentPath, User excutor) {
        if (args.length <= 0)
            return;

        // 只读第一个
        try {
            String[] path = getCombinationPath(args[0], currentPath);

            // TODO read function

        } catch (NoSuchFileOrDirectory noSuchFileOrDirectory) {
            System.out.println(noSuchFileOrDirectory);
        }
    }

    private void write(String[] args, String[] currentPath, User excutor) {
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
    }

}
