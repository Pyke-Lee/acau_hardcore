package kr.pyke.acau_hardcore.type;

public enum BOX_MESSAGE_TYPE {
    PRIVATE("private"),
    BROADCAST("broadcast"),
    NOTICE("notice");

    private final String key;

    BOX_MESSAGE_TYPE(String key) { this.key = key; }

    public static BOX_MESSAGE_TYPE byKey(String key) {
        for (BOX_MESSAGE_TYPE type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return PRIVATE;
    }
}
