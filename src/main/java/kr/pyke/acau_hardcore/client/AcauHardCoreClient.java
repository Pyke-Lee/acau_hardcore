package kr.pyke.acau_hardcore.client;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.hud.*;
import kr.pyke.acau_hardcore.client.gui.screen.*;
import kr.pyke.acau_hardcore.client.gui.screen.raid.BossRaidReadyScreen;
import kr.pyke.acau_hardcore.client.gui.screen.raid.BossRaidSelectScreen;
import kr.pyke.acau_hardcore.client.gui.screen.raid.RaidSelectState;
import kr.pyke.acau_hardcore.client.key.AcauHardCoreKeyMapping;
import kr.pyke.acau_hardcore.client.renderer.HousingRenderer;
import kr.pyke.acau_hardcore.data.randombox.ClientBoxRegistry;
import kr.pyke.acau_hardcore.handler.ItemTooltipHandler;
import kr.pyke.acau_hardcore.network.AcauHardCorePacket;
import kr.pyke.acau_hardcore.network.payload.s2c.*;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.menu.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class AcauHardCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AcauHardCorePacket.registerClient();

        MenuScreens.register(ModMenus.MAIL_SEND_MENU, MailSendScreen::new);

        AcauHardCoreHudOverlay overlay = new AcauHardCoreHudOverlay();
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hardcore_overlay"), overlay::render);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "notice_hud"), NoticeHud::render);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "help_count"), HelpRequestHud::render);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "charge_gauge"), ChargeGaugeHud::render);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "party_overlay"), PartyHudOverlay::render);

        HousingRenderer.register();

        AcauHardCoreKeyMapping.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Player player = client.player;
            if (player == null) { return; }

            ModComponents.HARDCORE_INFO.get(client.player).clientTick();
        });

        ItemTooltipHandler.register();
    }

    public static void openRandomBox(S2C_OpenRandomBoxPayload payload) {
        Minecraft.getInstance().setScreen(RandomBoxScreen.create(payload.boxId(), payload.winningStack(), payload.rewardIndex(), payload.rarityKey()));
    }

    public static void openChangeDisplayName() {
        Minecraft.getInstance().setScreen(ChangeDisplayNameScreen.create());
    }

    public static void syncBoxRegistry(S2C_SyncBoxRegistryPayload payload) {
        ClientBoxRegistry.setAll(S2C_SyncBoxRegistryPayload.parseJson(payload.json()));
    }

    public static void sendNotice(S2C_SendNoticePayload payload) {
        NoticeHud.updateMessage(Component.literal(payload.message().replace("&", "§")), payload.expirationTime());
    }

    public static void displayNameChangeResponse(S2C_DisplayNameChangeResponsePayload payload) {
        ChangeDisplayNameScreen.handleResponse(payload.success(), payload.message());
    }

    public static void openShop(S2C_OpenShopPayload payload) {
        Minecraft.getInstance().setScreen(ShopScreen.create(payload.shopID()));
    }

    public static void openSelectHardCore() {
        Minecraft.getInstance().setScreen(HardCoreSelectScreen.create());
    }

    public static void openRaidSelect(S2C_OpenRaidSelectPayload payload) {
        RaidSelectState.INSTANCE.initFull(payload);
        Minecraft.getInstance().setScreen(BossRaidSelectScreen.create());
    }

    public static void openRaidReady(S2C_OpenRaidReadyPayload payload) {
        Minecraft.getInstance().setScreen(BossRaidReadyScreen.create(payload.raidTypeName(), payload.isInitiator()));
    }

    public static void openPrefix() {
        Minecraft.getInstance().setScreen(PrefixScreen.create());
    }

    public static void selectPrefixResponse(S2C_SelectPrefixResponsePayload payload) {
        PrefixScreen.handleResponse(payload.success());
    }
}
