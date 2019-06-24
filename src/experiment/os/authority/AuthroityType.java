package experiment.os.authority;

public enum AuthroityType {
    READ(256),
    WRITE(128),
    EXCUTE(64),
    GROUP_READ(32),
    GROUP_WRITE(16),
    GROUP_EXCUTE(8),
    OTHER_READ(4),
    OTHER_WRITE(2),
    OTHER_EXCUTE(1);

    private Integer mode;

    AuthroityType(Integer mode) {
        this.mode = mode;
    }

    public Integer getMode() {
        return mode;
    }
}
