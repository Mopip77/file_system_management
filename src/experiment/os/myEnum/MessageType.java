package experiment.os.myEnum;

import java.time.LocalDateTime;

public enum  MessageType {
    LOGIN((byte) 0),
    REGISTER((byte) 1),
    NORMAL((byte) 2),
    INITIAL((byte) 3),
    LOGOUT((byte) 4),
    EXIT((byte) 5);


    private byte type;

    MessageType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public static MessageType getByValue(byte index) {
        MessageType mode = null;
        switch (index) {
            case 0:
                mode = LOGIN;
                break;
            case 1:
                mode = REGISTER;
                break;
            case 2:
                mode = NORMAL;
                break;
            case 3:
                mode = INITIAL;
                break;
            case 4:
                mode = LOGOUT;
                break;
            case 5:
                mode = EXIT;
                break;
        }
        return mode;
    }
}
