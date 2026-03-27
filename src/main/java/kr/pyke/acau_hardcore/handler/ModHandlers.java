package kr.pyke.acau_hardcore.handler;

public class ModHandlers {
    private ModHandlers() { }

    public static void register() {
        PlayerJoinHandler.register();
        PlayerDeathHandler.register();
        PlayerRespawnHandler.register();
        PlayerLeaveHandler.register();

        BlockBreakEventHandler.register();

        ServerTickHandler.register();
        ConsumptionHandler.register();

        ItemGroupEventsHandler.register();

        RuneEventHandler.register();

        DonationEventHandler.register();

        UseBlockCallbackEventHandler.register();
        UseItemCallbackEventHandler.register();
    }
}
