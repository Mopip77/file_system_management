package experiment.os.exception;

public class DirectoryIsFull extends Exception {
    public DirectoryIsFull() {
        super("The directory is full!");
    }
}
