package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.party.PartyManager;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ServerTickHandler {
    private static int tickCounter = 0;

    private ServerTickHandler() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerList().getPlayers().forEach(player -> ModComponents.HARDCORE_INFO.get(player).serverTick());

            tickCounter++;
            if (tickCounter >= 10) {
                tickCounter = 0;
                PartyManager partyManager = PartyManager.getServerState(server);
                partyManager.tickHealthSync(server);
                partyManager.tickInvites(server);
            }
        });
    }
}
