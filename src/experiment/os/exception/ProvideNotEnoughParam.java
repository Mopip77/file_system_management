package experiment.os.exception;

public class ProvideNotEnoughParam extends Exception {
    public ProvideNotEnoughParam() {
        super("the command needs more parameters");
    }
}
