package kr.pyke.acau_hardcore.prefix;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PrefixData(String id, String prefix) {
    public static final StreamCodec<FriendlyByteBuf, PrefixData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, PrefixData::id,
        ByteBufCodecs.STRING_UTF8, PrefixData::prefix,
        PrefixData::new
    );
}
