package experiment.os.exception;

public class IllegalRecursionCommand extends Exception {
    public IllegalRecursionCommand(String message) {
        super("There is illegal recursion command: " + message);
    }
}
