package kr.pyke.acau_hardcore.handler;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.boss.raid.BossRaidManager;
import kr.pyke.acau_hardcore.data.housing.HousingStructureManager;
import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import kr.pyke.acau_hardcore.util.Utils;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class PlayerDeathHandler {
    private PlayerDeathHandler() { }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
            if (entity instanceof Player player) {
                if (player instanceof ServerPlayer serverPlayer && BossRaidManager.isInRaid(serverPlayer.getUUID())) { return true; }

                Inventory inventory = player.getInventory();
                IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);

                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack itemStack = inventory.getItem(i);
                    if (!itemStack.isEmpty()) {
                        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
                        if (customData != null && customData.copyTag().contains("KeepOnDeath")) {
                            info.saveItem(i, itemStack);
                        }

                        inventory.setItem(i, ItemStack.EMPTY);
                    }
                }

                if (info.isStarted()) {
                    MinecraftServer server = player.level().getServer();
                    if (server != null) {
                        String hardcoreType = info.getHardcoreType().toString();
                        long seconds = info.getCurrentLiveTime() / 20;
                        String liveTime = String.format("%02d 시간 %02d 분 %02d 초", seconds / 3600, (seconds % 3600) / 60, seconds % 60);

                        PykeLib.sendBroadcastMessage(server.getPlayerList().getPlayers(), COLOR.CHARCOAL.getColor(), String.format("&7%s님께서 %s 월드에서 생존하다 사망하셨습니다. (생존 시간: %s)", player.getDisplayName().getString(), hardcoreType.toUpperCase(), liveTime));
                        server.getPlayerList().getPlayers().forEach(Utils::refreshTabList);

                        var housingData = ModComponents.HOUSING_DATA.get(server.overworld());
                        if (info.getHousingID() != null) {
                            HousingZone zone = housingData.getHousingZone(info.getHousingID());
                            int tier = Math.max(1, zone.getTier() - 1);
                            HousingStructureManager.changeTier(server.overworld(), zone, tier, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "structure_" + tier), true);
                        }
                    }
                }
            }

            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof Player player) {
                IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);

                if (info.isStarted()) {
                    info.setStarted(false);
                    info.setCurrentLiveTime(0);
                    info.addDeathCount();
                }
            }
        });
    }
}
