package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequestData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_HelpRequestRemovePayload;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.item.ticket.TicketItem;
import kr.pyke.acau_hardcore.type.HARDCORE_TYPE;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record C2S_StartHardCorePayload(String hardcoreType) implements CustomPacketPayload {
    public static final Type<C2S_StartHardCorePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_hardcore_start"));

    public static final StreamCodec<FriendlyByteBuf, C2S_StartHardCorePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, C2S_StartHardCorePayload::hardcoreType,
        C2S_StartHardCorePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_StartHardCorePayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            var info = ModComponents.HARDCORE_INFO.get(context.player());

            if (!info.isStarted()) {
                info.startHardCore(HARDCORE_TYPE.byKey(payload.hardcoreType));

                ItemStack itemStack = context.player().getMainHandItem();
                if (itemStack.getItem() instanceof TicketItem) {
                    itemStack.shrink(1);
                }
            }
        });
    }
}