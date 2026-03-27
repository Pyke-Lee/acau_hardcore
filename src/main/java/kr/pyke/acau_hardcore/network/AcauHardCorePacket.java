package kr.pyke.acau_hardcore.network;

import kr.pyke.acau_hardcore.client.AcauHardCoreClient;
import kr.pyke.acau_hardcore.client.gui.screen.raid.RaidReadyState;
import kr.pyke.acau_hardcore.client.gui.screen.raid.RaidSelectState;
import kr.pyke.acau_hardcore.network.payload.c2s.*;
import kr.pyke.acau_hardcore.network.payload.s2c.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class AcauHardCorePacket {
    private AcauHardCorePacket() { }

    public static void registerCodec() {
        // Server → Client
        PayloadTypeRegistry.playS2C().register(S2C_SendSingleDisplayNamePayload.ID, S2C_SendSingleDisplayNamePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_SendBulkDisplayNamePayload.ID, S2C_SendBulkDisplayNamePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_DisplayNameChangeResponsePayload.ID, S2C_DisplayNameChangeResponsePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_OpenChangeDisplayNameScreenPayload.ID, S2C_OpenChangeDisplayNameScreenPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestSyncAllPayload.ID, S2C_HelpRequestSyncAllPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestUpdatePayload.ID, S2C_HelpRequestUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HelpRequestRemovePayload.ID, S2C_HelpRequestRemovePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_SendNoticePayload.ID, S2C_SendNoticePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_SyncBoxRegistryPayload.ID, S2C_SyncBoxRegistryPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_OpenRandomBoxPayload.ID, S2C_OpenRandomBoxPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_ShopDataSyncAllPayload.ID, S2C_ShopDataSyncAllPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_OpenShopPayload.ID, S2C_OpenShopPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_PartySyncPayload.ID, S2C_PartySyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_OpenRaidSelectPayload.ID, S2C_OpenRaidSelectPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_OpenRaidReadyPayload.ID, S2C_OpenRaidReadyPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_RaidReadyUpdatePayload.ID, S2C_RaidReadyUpdatePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_RaidSelectUpdatePayload.ID, S2C_RaidSelectUpdatePayload.STREAM_CODEC);

        // Client → Server
        PayloadTypeRegistry.playC2S().register(C2S_ChangeDisplayNamePayload.ID, C2S_ChangeDisplayNamePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_ClaimMailPayload.ID, C2S_ClaimMailPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_RemoveMailPayload.ID, C2S_RemoveMailPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_SendMailPayload.ID, C2S_SendMailPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_UpdateMailPayload.ID, C2S_UpdateMailPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestChangeStatusPayload.ID, C2S_HelpRequestChangeStatusPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestDeletePayload.ID, C2S_HelpRequestDeletePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_HelpRequestPurgePayload.ID, C2S_HelpRequestPurgePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_RequestDisplayNamesPayload.ID, C2S_RequestDisplayNamesPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_TeleportToPlayerPayload.ID, C2S_TeleportToPlayerPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_ClaimRandomBoxRewardPayload.ID, C2S_ClaimRandomBoxRewardPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_ShopTransactionPayload.ID, C2S_ShopTransactionPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_StartHardCorePayload.ID, C2S_StartHardCorePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_RaidSelectPayload.ID, C2S_RaidSelectPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2S_RaidReadyPayload.ID, C2S_RaidReadyPayload.STREAM_CODEC);
    }

    public static void registerServer() {
        // C2S_ChangeDisplayNamePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_ChangeDisplayNamePayload.ID, C2S_ChangeDisplayNamePayload::handle);
        // C2S_ClaimMailPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_ClaimMailPayload.ID, C2S_ClaimMailPayload::handle);
        // C2S_RemoveMailPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_RemoveMailPayload.ID, C2S_RemoveMailPayload::handle);
        // C2S_SendMailPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_SendMailPayload.ID, C2S_SendMailPayload::handle);
        // C2S_UpdateMailPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_UpdateMailPayload.ID, C2S_UpdateMailPayload::handle);
        // C2S_HelpRequestChangeStatusPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestChangeStatusPayload.ID, C2S_HelpRequestChangeStatusPayload::handle);
        // C2S_HelpRequestDeletePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestDeletePayload.ID, C2S_HelpRequestDeletePayload::handle);
        // C2S_HelpRequestPurgePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_HelpRequestPurgePayload.ID, C2S_HelpRequestPurgePayload::handle);
        // C2S_RequestDisplayNamesPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_RequestDisplayNamesPayload.ID, C2S_RequestDisplayNamesPayload::handle);
        // C2S_TeleportToPlayerPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_TeleportToPlayerPayload.ID, C2S_TeleportToPlayerPayload::handle);
        // C2S_ClaimRandomBoxRewardPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_ClaimRandomBoxRewardPayload.ID, C2S_ClaimRandomBoxRewardPayload::handle);
        // C2S_ShopTransactionPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_ShopTransactionPayload.ID, C2S_ShopTransactionPayload::handle);
        // C2S_StartHardCorePayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_StartHardCorePayload.ID, C2S_StartHardCorePayload::handle);
        // C2S_RaidSelectPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_RaidSelectPayload.ID, C2S_RaidSelectPayload::handle);
        // C2S_RaidReadyPayload
        ServerPlayNetworking.registerGlobalReceiver(C2S_RaidReadyPayload.ID, C2S_RaidReadyPayload::handle);
    }

    public static void registerClient() {
        // S2C_SendSingleDisplayNamePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendSingleDisplayNamePayload.ID, S2C_SendSingleDisplayNamePayload::handle);
        // S2C_SendBulkDisplayNamePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendBulkDisplayNamePayload.ID, S2C_SendBulkDisplayNamePayload::handle);
        // S2C_HelpRequestSyncAllPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestSyncAllPayload.ID, S2C_HelpRequestSyncAllPayload::handle);
        // S2C_HelpRequestUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestUpdatePayload.ID, S2C_HelpRequestUpdatePayload::handle);
        // S2C_HelpRequestRemovePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_HelpRequestRemovePayload.ID, S2C_HelpRequestRemovePayload::handle);
        // S2C_ShopDataSyncAllPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_ShopDataSyncAllPayload.ID, S2C_ShopDataSyncAllPayload::handle);
        // S2C_PartySyncPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_PartySyncPayload.ID, S2C_PartySyncPayload::handle);

        registerClientHandle();
    }

    private static void registerClientHandle() {
        // S2C_OpenRandomBoxPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_OpenRandomBoxPayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.openRandomBox(payload)));

        // S2C_OpenChangeDisplayNameScreenPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_OpenChangeDisplayNameScreenPayload.ID, (payload, context) -> context.client().execute(AcauHardCoreClient::openChangeDisplayName));

        // S2C_SyncBoxRegistryPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SyncBoxRegistryPayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.syncBoxRegistry(payload)));

        // S2C_SendNoticePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_SendNoticePayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.sendNotice(payload)));

        // S2C_DisplayNameChangeResponsePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_DisplayNameChangeResponsePayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.displayNameChangeResponse(payload)));

        // S2C_OpenShopPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_OpenShopPayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.openShop(payload)));

        // S2C_OpenRaidSelectPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_OpenRaidSelectPayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.openRaidSelect(payload)));

        // S2C_OpenRaidReadyPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_OpenRaidSelectPayload.ID, (payload, context) -> context.client().execute(() -> AcauHardCoreClient.openRaidSelect(payload)));

        // S2C_RaidReadyUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_RaidReadyUpdatePayload.ID, (payload, context) -> context.client().execute(() -> RaidReadyState.INSTANCE.update(payload)));

        // S2C_RaidSelectUpdatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_RaidSelectUpdatePayload.ID, (payload, context) -> context.client().execute(() -> RaidSelectState.INSTANCE.updateStatus(payload)));
    }
}
