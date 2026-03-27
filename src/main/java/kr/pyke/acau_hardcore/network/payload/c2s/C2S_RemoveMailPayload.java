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
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public record C2S_RemoveMailPayload(UUID mailUUID) implements CustomPacketPayload {
    public static final Type<C2S_RemoveMailPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_remove_mail"));

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_RemoveMailPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, C2S_RemoveMailPayload::mailUUID,
        C2S_RemoveMailPayload::new
    );

    public static void handle(C2S_RemoveMailPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            Player player = context.player();
            IMailBoxComponent mailbox = ModComponents.MAIL_BOX.get(player);
            UUID targetID = payload.mailUUID();

            Optional<MailBoxData> targetMail = mailbox.getMails().stream().filter(mail -> mail.mailUUID().equals(targetID)).findFirst();
            targetMail.ifPresent(mailbox::removeMail);
        });
    }
}
