package kr.pyke.acau_hardcore.type;

public enum RUNE_TYPE {
    COMBAT("combat", "전투 룬"),
    LIFE("life", "생활 룬");

    private final String key;
    private final String displayName;

    RUNE_TYPE(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }

    public static RUNE_TYPE byKey(String key) {
        for (RUNE_TYPE type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return COMBAT;
    }
}
