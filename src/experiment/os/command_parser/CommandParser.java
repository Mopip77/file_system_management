package experiment.os.command_parser;

import experiment.os.exception.CommandNotFound;
import experiment.os.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class CommandParser {
    // cd xxx
    // ls xxx
    // mkdir xxx
    // rm xxx
    // mv xxx xxx
    // cp xxx xxx
    // ln [-s] xxx xxx

    // open xxx
    // close xxx
    // read xxx
    // write xxx
    // logout

    private Executor directoryExcutor = new DirectoryExecutor();
    private Executor fileExcutor = new FileExecutor();


    public void parse(String queryCommand, String[] currentPath, User excutor) throws Exception {
        String command;
        String[] args;
        if (StringUtils.isBlank(queryCommand)) {
            throw new CommandNotFound(queryCommand);
        }
        String[] commandItem = queryCommand.split(" ");
        command = commandItem[0].toLowerCase();
        args = ArrayUtils.subarray(commandItem, 1, commandItem.length);
        switch (command) {
            case "cd":
            case "ls":
            case "mkdir":
            case "rm":
            case "mv":
            case "cp":
            case "ln":
                directoryExcutor.excute(command, args, currentPath, excutor);
                break;
            case "open":
            case "close":
            case "read":
            case "write":
                fileExcutor.excute(command, args, currentPath, excutor);


        }
    }
}
