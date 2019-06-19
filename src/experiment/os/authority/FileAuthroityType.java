package experiment.os.authority;

public enum FileAuthroityType {
    Read(1),
    Write(2),
    Excute(4);

    private Integer mode;

    FileAuthroityType(Integer mode) {
        this.mode = mode;
    }

    public Integer getMode() {
        return mode;
    }
}
