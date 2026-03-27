package kr.pyke.acau_hardcore.data.randombox;

import kr.pyke.acau_hardcore.type.BOX_MESSAGE_TYPE;
import kr.pyke.acau_hardcore.type.BOX_RARITY;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record BoxReward(String item, int count, @Nullable String nbt, @Nullable String customName, int weight, BOX_RARITY rarity, @Nullable String sound, BOX_MESSAGE_TYPE messageType, String message) {
    public Identifier getItemID() { return Identifier.parse(item); }
    public @Nullable Identifier getSoundID() { return sound != null ? Identifier.parse(sound) : null; }
}
