package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.registry.component.ModComponents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class PlayerRespawnHandler {
    private PlayerRespawnHandler() { }

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> ModComponents.HARDCORE_INFO.get(newPlayer).loadItem());
    }
}
