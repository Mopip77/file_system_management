package experiment.os.myEnum;

public enum  FileType {

    FILE(1),
    DIRECTORY(2),
    SYMBOL_LINK(4);

    private Integer type;

    FileType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
