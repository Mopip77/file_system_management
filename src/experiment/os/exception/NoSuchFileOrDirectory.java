package experiment.os.exception;

public class NoSuchFileOrDirectory extends Exception {
    public NoSuchFileOrDirectory(String path) {
        super("no such file or directory: " + path);
    }
}
