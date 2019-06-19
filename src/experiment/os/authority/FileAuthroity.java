package experiment.os.authority;

public enum FileAuthroity {
    Read(1),
    Write(2),
    Excute(4);

    private Integer mode;

    FileAuthroity(Integer mode) {
        this.mode = mode;
    }

    public Integer getMode() {
        return mode;
    }
}
