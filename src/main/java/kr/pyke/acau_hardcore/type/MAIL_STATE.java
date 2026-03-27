package kr.pyke.acau_hardcore.type;

public enum MAIL_STATE {
    UNREAD(0),
    READ(1);

    final int id;

    MAIL_STATE(int value) { this.id = value; }

    public int getID() { return this.id; }

    public static MAIL_STATE byID(int value) {
        for (MAIL_STATE state : MAIL_STATE.values()) {
            if (state.id == value) { return state; }
        }

        return UNREAD;
    }
}
