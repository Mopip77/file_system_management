package experiment.os.command_parser;

import experiment.os.Session;
import experiment.os.exception.CommandNotFound;
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

    private static Executor directoryExcutor = new DirectoryExecutor();
    private static Executor fileExcutor = new FileExecutor();
    private static Executor authorityExcutor = new AuthorityExecutor();


    public static String parse(String queryCommand, String[] currentPath, Session session) throws Exception {
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
            case "ln":
                return directoryExcutor.excute(command, args, currentPath, session);
            case "touch":
            case "open":
            case "close":
            case "cat":
            case "echo":
                return fileExcutor.excute(command, args, currentPath, session);
            case "chgrp":
            case "chmod":
            case "stat":
                return authorityExcutor.excute(command, args, currentPath, session);
            default:
                return "";
        }
    }
}
