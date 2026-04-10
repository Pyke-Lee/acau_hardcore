package kr.pyke.acau_hardcore.handler;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import kr.pyke.integration.event.DonationReceivedCallback;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DonationEventHandler {
    public static ServerPlayer ownedPlayer;

    private static final int NORMAL_SURVIVAL_KIT    = 0;
    private static final int RARE_SURVIVAL_KIT      = 1;
    private static final int RESTART_JAIL           = 2;
    private static final int SPAWN_MONSTER          = 3;
    private static final int INVITATION             = 4;
    private static final int RARE_SURVIVAL_KIT_X10  = 5;
    private static final int ENTER_JAIL             = 6;
    private static final int RANDOM_TELEPORT        = 7;
    private static final int SKY_DIVING             = 8;

    private DonationEventHandler() { }

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
            String platformCurrency = platform.equals("SOOP") ? String.format("&e별풍선 %,d개&r", amount) : String.format("&e%,d 치즈&r", krwAmount);

            //   5천원 : 생존 키트 | 개인
            if (ModConfig.INSTANCE.donationRewards.get(NORMAL_SURVIVAL_KIT) == krwAmount) {
                MailBoxData mail = MailBoxData.create(mailTitle, sender, message, List.of(createRandomBox("normal_survival_kit", 0, "§6생존 키트", 1)));

                ModComponents.MAIL_BOX.get(player).addMail(mail);
                sendPersonalMessage(player, notification);
            }
            //   1만원 : 고급 생존 키트 | 개인
            if (ModConfig.INSTANCE.donationRewards.get(RARE_SURVIVAL_KIT) == krwAmount) {
                MailBoxData mail = MailBoxData.create(mailTitle, sender, message, List.of(createRandomBox("rare_survival_kit", 1, "§3고급 생존 키트", 1)));

                ModComponents.MAIL_BOX.get(player).addMail(mail);
                sendPersonalMessage(player, notification);
            }
            //   2만원 : 감옥전용 태초 | 개인
            if (ModConfig.INSTANCE.donationRewards.get(RESTART_JAIL) == krwAmount) {
                var info = ModComponents.HARDCORE_INFO.get(player);

                if (info.isJail()) {
                    info.enterJail();
                }
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("와~ 태초 마을이다~")));
            }
            //   3만원 : 몬스터 소환 | 개인
            if (ModConfig.INSTANCE.donationRewards.get(SPAWN_MONSTER) == krwAmount) {
                ModConfig.MobEntry mobEntry = ModConfig.INSTANCE.getRandomMob();
                if (mobEntry != null) {
                    Identifier entityId = Identifier.parse(mobEntry.id());
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
                    int count = player.getRandom().nextInt(mobEntry.max() - mobEntry.min() + 1) + mobEntry.min();

                    for (int i = 0; i < count; i++) {
                        Entity entity = entityType.create(player.level(), EntitySpawnReason.COMMAND);
                        if (entity != null) {
                            entity.setPos(player.getX(), player.getY(), player.getZ());

                            if (mobEntry.weapon() != null && !mobEntry.weapon().isEmpty() && entity instanceof Mob mobEntity) {
                                Item weaponItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(mobEntry.weapon()));
                                mobEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weaponItem));
                            }

                            player.level().addFreshEntity(entity);
                        }
                    }
                    sendPersonalMessage(player, String.format("&7%s(이)가 &e%d&r마리 소환되었습니다.", mobEntry.display(), count));
                }
            }
            //   5만원 : 하드코어 입장권 | 서버 주인 전용 보상
            if (ModConfig.INSTANCE.donationRewards.get(INVITATION) == krwAmount) {
                MailBoxData mail = MailBoxData.create("하드코어 입장권", sender, message, List.of(new ItemStack(ModItems.HARDCORE_TICKET)));

                DisplayNameData data = DisplayNameData.getServerState(player.level().getServer());
                List<String> displayNames = data.getDisplayNames().values().stream().toList();
                if (displayNames.contains(message)) {
                    Player target = data.getPlayer(message);
                    if (target != null) {
                        ModComponents.MAIL_BOX.get(target).addMail(mail);
                    }
                    else {
                        PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "대상이 잘못되어 입장권 지급이 보류되었습니다.");
                        PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "대상: " + message);
                    }
                }
                else {
                    PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "대상이 잘못되어 입장권 지급이 보류되었습니다.");
                    PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "대상: " + message);
                }
                sendPersonalMessage(player, notification);
            }
            //   9만원 : 고급 생존 키트 10개 | 개인
            if (ModConfig.INSTANCE.donationRewards.get(RARE_SURVIVAL_KIT_X10) == krwAmount) {
                MailBoxData mail = MailBoxData.create(mailTitle, sender, message, List.of(createRandomBox("rare_survival_kit", 1, "§3고급 생존 키트", 10)));

                ModComponents.MAIL_BOX.get(player).addMail(mail);
                sendPersonalMessage(player, notification);
            }
            //   10만원 : 감옥 보내기 | 전체
            if (ModConfig.INSTANCE.donationRewards.get(ENTER_JAIL) == krwAmount) {
                ModComponents.HARDCORE_INFO.get(player).enterJail();
                sendServerMessage(player, String.format("&7%s&r님께서 %s를 받아 감옥에 수감되었습니다.", player.getDisplayName().getString(), platformCurrency));
            }
            //   30만원 : 랜덤 플레이어에게 이동 | 개인, 대상
            if (ModConfig.INSTANCE.donationRewards.get(RANDOM_TELEPORT) == krwAmount) {
                ModComponents.HARDCORE_INFO.get(player).randomTargetTeleport();
                sendServerMessage(player, String.format("&7%s&r님께서 %s를 받아 랜덤한 플레이어에게 이동합니다.", player.getDisplayName().getString(), platformCurrency));
            }
            //   50만원 : 스카이다이빙(Y 500으로 이동) | 공지
            if (ModConfig.INSTANCE.donationRewards.get(SKY_DIVING) == krwAmount) {
                ModComponents.HARDCORE_INFO.get(player).addTimerTaskMessage(10, "잠시 후 Y 500으로 이동됩니다.", () -> {
                    Vec3 pos = player.position();
                    player.teleportTo(pos.x, 500d, pos.z);
                    player.hurtMarked = true;
                    player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENDER_DRAGON_GROWL), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.5f, 1.f, player.getRandom().nextLong()));
                });
                sendServerMessage(player, String.format("&7%s&r님께서 %s를 받아 하늘로 날라갔습니다.", player.getDisplayName().getString(), platformCurrency));
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
