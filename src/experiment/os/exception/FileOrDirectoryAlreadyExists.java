package experiment.os.exception;

public class FileOrDirectoryAlreadyExists extends Exception {
    public FileOrDirectoryAlreadyExists(String message) {
        super("file or directory already exists: " + message);
    }
}
