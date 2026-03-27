package kr.pyke.acau_hardcore.type;

public enum HARDCORE_TYPE {
    BEGINNER("beginner", "바닐라"),
    EXPERT("expert", "숙련자");

    private final String key;
    private final String displayName;

    HARDCORE_TYPE(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() { return key; }
    public static HARDCORE_TYPE byKey(String key) {
        for (HARDCORE_TYPE type : HARDCORE_TYPE.values()) {
            if (type.key.equals(key)) { return type; }
        }

        return BEGINNER;
    }

    public String getDisplayName() { return displayName; }
}
