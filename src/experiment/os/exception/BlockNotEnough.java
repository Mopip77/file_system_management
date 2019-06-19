package experiment.os.exception;

public class BlockNotEnough extends Exception {
    public BlockNotEnough() {
        super("there are not enough blocks");
    }
}
