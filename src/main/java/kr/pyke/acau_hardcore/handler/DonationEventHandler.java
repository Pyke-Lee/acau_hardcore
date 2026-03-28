package kr.pyke.acau_hardcore.handler;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import kr.pyke.integration.event.DonationReceivedCallback;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DonationEventHandler {
    private DonationEventHandler() { }

    public static ServerPlayer ownedPlayer;

    public static void register() {
        DonationReceivedCallback.DONATION_RECEIVED.register((player, donationEvent) -> {
            String name = player.getDisplayName().getString();
            String platform = donationEvent.platform();
            String sender = donationEvent.donor();
            String message = donationEvent.donationMessage();
            int amount = donationEvent.getAmount();
            int krwAmount = amount;
            String notification = "";

            if (platform.equals("SOOP")) {
                krwAmount *= 100;
                notification = String.format("&7%s님이 &e별풍선 %,d&개&f를 후원 받으셨습니다.", name, amount);
            }
            else if (platform.equals("CHZZK")) {
                notification = String.format("&7%s님이 &e%,d 치즈&f를 후원 받으셨습니다.", name, amount);
            }

            String mailTitle = platform.equals("SOOP") ? String.format("별풍선 %,d개 보상", amount) : String.format("%,d 치즈 보상", krwAmount);

            switch(krwAmount) {
                //   5천원 : 생존 키트 | 개인
                case 5000 -> {
                    MailBoxData mail = MailBoxData.create(mailTitle, sender, message, List.of(createRandomBox("normal_survival_kit", 0, "§6생존 키트", 1)));

                    ModComponents.MAIL_BOX.get(player).addMail(mail);
                    sendPersonalMessage(player, notification);
                }
                //   1만원 : 고급 생존 키트 | 개인
                case 10000 -> {
                    MailBoxData mail = MailBoxData.create(mailTitle, sender, message, List.of(createRandomBox("rare_survival_kit", 1, "§3고급 생존 키트", 1)));

                    ModComponents.MAIL_BOX.get(player).addMail(mail);
                    sendPersonalMessage(player, notification);
                }
                //   2만원 : 감옥전용 태초 | 개인
                case 20000 -> {
                    var info = ModComponents.HARDCORE_INFO.get(player);

                    if (info.isJail()) { info.enterJail(); }
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("와~ 태초 마을이다~")));
                }
                //   3만원 : 몬스터 소환 | 개인
                case 30000 -> { }
                //   5만원 : 하드코어 입장권 | 서버 주인 전용 보상
                case 50000 -> {
                    if (!player.equals(ownedPlayer)) { break; }
                    MailBoxData mail = MailBoxData.create("하드코어 입장권", sender, message, List.of(new ItemStack(ModItems.HARDCORE_TICKET)));

                    List<String> displayNames = DisplayNameData.getServerState(player.level().getServer()).getDisplayNames().values().stream().toList();
                    if (displayNames.contains(message)) {
                        ModComponents.MAIL_BOX.get(player).addMail(mail);
                    }
                    else {
                        PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "대상이 잘못되어 입장권 지급이 보류되었습니다.");
                        PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "대상: " + message);
                    }
                    sendPersonalMessage(player, notification);
                }
                //   9만원 : 고급 생존 키트 10개 | 개인
                case 90000 -> {
                    MailBoxData mail = MailBoxData.create(mailTitle, sender, message, List.of(createRandomBox("rare_survival_kit", 1, "§3고급 생존 키트", 10)));

                    ModComponents.MAIL_BOX.get(player).addMail(mail);
                    sendPersonalMessage(player, notification);
                }
                //   10만원 : 감옥 보내기 | 전체
                case 100000 -> {
                    ModComponents.HARDCORE_INFO.get(player).enterJail();
                    sendServerMessage(player, String.format("&7%s&r님께서 %s를 받아 감옥에 수감되었습니다.", player.getDisplayName().getString(), platform.equals("SOOP") ? String.format("&e별풍선 %,d개&r", amount) : String.format("&e%,d 치즈&r", krwAmount)));
                }
                //   30만원 : 랜덤 플레이어에게 이동 | 개인, 대상
                case 300000 -> ModComponents.HARDCORE_INFO.get(player).randomTargetTeleport();
                //   50만원 : 스카이다이빙(Y 500으로 이동) | 공지
                case 500000 -> ModComponents.HARDCORE_INFO.get(player).addTimerTaskMessage(10, "잠시 후 Y 500으로 이동됩니다.", () -> {
                    Vec3 pos = player.position();
                    player.teleportTo(pos.x, 500d, pos.z);
                    player.playSound(SoundEvents.ENDER_DRAGON_GROWL, 0.5f, 1.f);
                });
            }
        });
    }

    private static void sendPersonalMessage(ServerPlayer player, String message) {
        PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), message);
    }

    private static void sendServerMessage(ServerPlayer player, String message) {
        PykeLib.sendSystemMessage(player.level().getServer().getPlayerList().getPlayers(), COLOR.LIME.getColor(), message);
    }

    private static void broadcastMessage(ServerPlayer player, String message) {
        PykeLib.sendBroadcastMessage(player.level().getServer().getPlayerList().getPlayers(), COLOR.LIME.getColor(), message);
    }

    private static ItemStack createRandomBox(String boxID, int modelData, String displayName, int amount) {
        ItemStack itemStack = new ItemStack(ModItems.RANDOM_BOX);

        CompoundTag tag = new CompoundTag();
        tag.putString("box_id", boxID);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (modelData > 0) {
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) modelData), List.of(), List.of(), List.of()));
        }

        itemStack.set(DataComponents.ITEM_NAME, Component.literal(displayName));
        itemStack.setCount(amount);

        return itemStack;
    }
}
