package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.AcauHardCoreClient;
import kr.pyke.acau_hardcore.client.gui.screen.RandomBoxScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public record S2C_OpenRandomBoxPayload(String boxId, ItemStack winningStack, int rewardIndex, String rarityKey) implements CustomPacketPayload {

    public static final Type<S2C_OpenRandomBoxPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_open_random_box"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_OpenRandomBoxPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, S2C_OpenRandomBoxPayload::boxId,
        ItemStack.STREAM_CODEC, S2C_OpenRandomBoxPayload::winningStack,
        ByteBufCodecs.VAR_INT, S2C_OpenRandomBoxPayload::rewardIndex,
        ByteBufCodecs.STRING_UTF8, S2C_OpenRandomBoxPayload::rarityKey,
        S2C_OpenRandomBoxPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}