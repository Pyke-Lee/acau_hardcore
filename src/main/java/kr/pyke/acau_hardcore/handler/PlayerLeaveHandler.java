package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.party.PartyManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class PlayerLeaveHandler {
    private PlayerLeaveHandler() { }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PartyManager.getServerState(server).onPlayerLeave(server, handler.getPlayer());
        });
    }
}
