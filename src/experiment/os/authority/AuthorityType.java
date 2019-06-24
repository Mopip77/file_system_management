package experiment.os.authority;

public enum AuthorityType {
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

    AuthorityType(Integer mode) {
        this.mode = mode;
    }

    public Integer getMode() {
        return mode;
    }

    public static boolean hasAuthority(short targetMode, short flagPos) {
        return (targetMode & flagPos) != 0;
    }

    public static boolean hasAuthority(short uUid, short uGid, short fUid, short fGid, short targetMode, AuthorityType ...authorityTypes) {
        boolean flag = true;
        int offset = 0;
        if (uUid == fUid) {
            offset = 0;
        } else if (uGid == fGid) {
            offset = 3;
        } else {
            offset = 6;
        }

        for (AuthorityType authorityType : authorityTypes) {
            flag &= hasAuthority(targetMode, (short) (authorityType.getMode() >> offset));
            if (!flag) {
                return false;
            }
        }
        return true;
    }
}
