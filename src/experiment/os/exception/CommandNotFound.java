package experiment.os.exception;

public class CommandNotFound extends Exception {
    public CommandNotFound(String command) {
        super("commond " + command + " not found!");
    }
}
