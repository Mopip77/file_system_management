package experiment.os.command_parser;

import org.apache.commons.lang3.ArrayUtils;

public class CommandParser {
    // cd xxx
    // ls xxx
    // open xxx
    // close xxx
    // read xxx
    // write xxx
    // mkdir xxx
    // rm xxx
    // logout

    public void parse(String queryCommand, String currentPath) {
        String command;
        String[] args;

        command, args = queryCommand.split(" ");
    }
}
