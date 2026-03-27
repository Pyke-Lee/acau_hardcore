package kr.pyke.acau_hardcore.type;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum HELP_REQUEST_STATE implements StringRepresentable {
    WAITING("waiting", "대기", ChatFormatting.YELLOW),
    PROCESSING("processing", "처리 중", ChatFormatting.AQUA),
    COMPLETED("completed", "완료", ChatFormatting.GREEN);

    public static final Codec<HELP_REQUEST_STATE> CODEC = StringRepresentable.fromEnum(HELP_REQUEST_STATE::values);

    private final String key;
    private final String displayName;
    private final ChatFormatting color;

    HELP_REQUEST_STATE(String key, String displayName, ChatFormatting color) {
        this.key = key;
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public ChatFormatting getColor() { return color; }

    @Override public @NonNull String getSerializedName() { return key; }

    public static HELP_REQUEST_STATE byKey(String key) {
        for (HELP_REQUEST_STATE status : values()) {
            if (status.key.equals(key)) { return status; }
        }

        return WAITING;
    }
}
