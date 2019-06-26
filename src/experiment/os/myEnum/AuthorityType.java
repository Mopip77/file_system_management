package experiment.os.myEnum;

public enum AuthorityType {
    READ((short)256),
    WRITE((short)128),
    EXCUTE((short)64),

    GROUP_READ((short)32),
    GROUP_WRITE((short)16),
    GROUP_EXCUTE((short)8),

    OTHER_READ((short)4),
    OTHER_WRITE((short)2),
    OTHER_EXCUTE((short)1),

    FILE_DEFAULT_AUTHORITY((short)436),
    DIRECTORY_DEFAULT_AUTHORITY((short)509);

    private short mode;

    AuthorityType(short mode) {
        this.mode = mode;
    }

    public short getMode() {
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
