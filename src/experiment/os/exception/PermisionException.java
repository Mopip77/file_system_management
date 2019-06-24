package experiment.os.exception;

public class PermisionException extends Exception {
    public PermisionException(String commond) {
        super("You do not have " + commond + " permission");
    }
}
