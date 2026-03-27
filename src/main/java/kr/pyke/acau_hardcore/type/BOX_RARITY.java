package kr.pyke.acau_hardcore.type;

import net.minecraft.ChatFormatting;

public enum BOX_RARITY {
    COMMON("common", "일반", ChatFormatting.GRAY, 0xFFBDBDBD),
    UNCOMMON("uncommon", "고급", ChatFormatting.GREEN, 0xFF4CAF50),
    RARE("rare", "희귀", ChatFormatting.AQUA, 0xFF2196F3),
    EPIC("epic", "영웅", ChatFormatting.LIGHT_PURPLE, 0xFFAB47BC),
    LEGENDARY("legendary", "전설", ChatFormatting.GOLD, 0xFFFFB300);

    private final String key;
    private final String displayName;
    private final ChatFormatting chatColor;
    private final int argbColor;

    BOX_RARITY(String key, String displayName, ChatFormatting chatColor, int argbColor) {
        this.key = key;
        this.displayName = displayName;
        this.chatColor = chatColor;
        this.argbColor = argbColor;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public ChatFormatting getChatColor() { return chatColor; }
    public int getArgbColor() { return argbColor; }

    public static BOX_RARITY byKey(String key) {
        for (BOX_RARITY rarity : values()) {
            if (rarity.key.equals(key)) {
                return rarity;
            }
        }

        return COMMON;
    }
}
