package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequestData;
import kr.pyke.acau_hardcore.data.shop.ShopManager;
import kr.pyke.acau_hardcore.network.payload.s2c.*;
import kr.pyke.acau_hardcore.party.PartyManager;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;

public class PlayerJoinHandler {
    private PlayerJoinHandler() { }

    public static void register() {
        ServerPlayerEvents.JOIN.register((player) -> {
            MinecraftServer server = player.level().getServer();

            Map<UUID, String> displayNames = DisplayNameData.getServerState(server).getDisplayNames();
            ServerPlayNetworking.send(player, new S2C_SendBulkDisplayNamePayload(displayNames));

            HelpRequestData data = HelpRequestData.get(server);
            ServerPlayNetworking.send(player, new S2C_HelpRequestSyncAllPayload(data.getAllRequests()));

            ServerPlayNetworking.send(player, S2C_SyncBoxRegistryPayload.fromRegistry());

            ServerPlayNetworking.send(player, new S2C_ShopDataSyncAllPayload(ShopManager.getShops()));

            PartyManager.getServerState(server).onPlayerJoin(server, player);

            ServerPlayNetworking.send(player, new S2C_PrefixSyncAllPayload(PrefixRegistry.getAll().stream().toList()));
        });
    }
}
