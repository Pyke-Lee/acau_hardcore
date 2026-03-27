package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.mailbox.IMailBoxComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record C2S_ClaimMailPayload(UUID mailUUID) implements CustomPacketPayload {
    public static final Type<C2S_ClaimMailPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_claim_mail"));

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_ClaimMailPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, C2S_ClaimMailPayload::mailUUID,
        C2S_ClaimMailPayload::new
    );

    public static void handle(C2S_ClaimMailPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            Player player = context.player();
            IMailBoxComponent mailbox = ModComponents.MAIL_BOX.get(player);
            UUID mailUUID = payload.mailUUID();

            Optional<MailBoxData> targetMail = mailbox.getMails().stream()
                .filter(mail -> mail.mailUUID().equals(mailUUID))
                .findFirst();

            targetMail.ifPresent(mail -> mailbox.claimMail(player, mail));
        });
    }
}
