package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.menu.MailSendMenu;
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record C2S_SendMailPayload(String receiver, String sender, String title, String content) implements CustomPacketPayload {
    public static final Type<C2S_SendMailPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_send_mail"));

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_SendMailPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, C2S_SendMailPayload::receiver,
        ByteBufCodecs.STRING_UTF8, C2S_SendMailPayload::sender,
        ByteBufCodecs.STRING_UTF8, C2S_SendMailPayload::title,
        ByteBufCodecs.STRING_UTF8, C2S_SendMailPayload::content,
        C2S_SendMailPayload::new
    );

    public static void handle(C2S_SendMailPayload payload, ServerPlayNetworking.Context context) {
        context.player().level().getServer().execute(() -> {
            ServerPlayer sender = context.player();
            String targetInput = payload.receiver().trim();
            String senderName = payload.sender.isEmpty() ? sender.getName().getString() : payload.sender();

            List<ItemStack> itemsToSend = new ArrayList<>();
            if (sender.containerMenu instanceof MailSendMenu mailMenu) {
                Container slots = mailMenu.mailSlots;

                for (int i = 0; i < slots.getContainerSize(); ++i) {
                    ItemStack stack = slots.getItem(i);
                    if (!stack.isEmpty()) {
                        itemsToSend.add(stack.copy());
                        slots.setItem(i, ItemStack.EMPTY);
                    }
                }
            }
            else { return; }

            if (targetInput.equals("@a")) {
                if (sender.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2)))) {
                    PykeLib.sendSystemMessage(sender, COLOR.LIME.getColor(), "접속중인 모든 플레이어에게 메일을 발송했습니다.");
                    List<ServerPlayer> allPlayers = sender.level().getServer().getPlayerList().getPlayers();
                    for (ServerPlayer receiver : allPlayers) {
                        sendMailToPlayer(receiver, senderName, payload.title(), payload.content(), copyItemList(itemsToSend));
                    }
                }
                else {
                    refundItems(sender, itemsToSend);
                    PykeLib.sendSystemMessage(sender, COLOR.RED.getColor(), "전체 발송 권한이 없습니다.");
                }
            }
            else if (targetInput.equals("@s")) {
                PykeLib.sendSystemMessage(sender, COLOR.LIME.getColor(), "나 자신에게 메일을 보냈습니다.");
                sendMailToPlayer(sender, senderName, payload.title(), payload.content(), itemsToSend);
            }
            else {
                ServerPlayer receiver = sender.level().getServer().getPlayerList().getPlayerByName(payload.receiver());

                if (receiver != null) {
                    PykeLib.sendSystemMessage(sender, COLOR.LIME.getColor(), String.format("§7%s님에게 우편을 보냈습니다.", payload.receiver()));
                    sendMailToPlayer(receiver, senderName, payload.title(), payload.content(), itemsToSend);
                }
                else {
                    refundItems(sender, itemsToSend);
                    PykeLib.sendSystemMessage(sender, COLOR.RED.getColor(), "받는 사람이 접속 중이 아닙니다.");
                }
            }
        });
    }

    private static void sendMailToPlayer(ServerPlayer receiver, String senderName, String title, String content, List<ItemStack> items) {
        MailBoxData newMail = MailBoxData.create(title, senderName, content, items);
        ModComponents.MAIL_BOX.get(receiver).addMail(newMail);
    }

    private static void refundItems(ServerPlayer player, List<ItemStack> items) {
        for (ItemStack item : items) {
            if (!player.getInventory().add(item)) { player.drop(item, false); }
        }
    }

    private static List<ItemStack> copyItemList(List<ItemStack> originals) {
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack stack : originals) { copies.add(stack.copy()); }

        return copies;
    }
}
