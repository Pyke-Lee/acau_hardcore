package kr.pyke.acau_hardcore;

import kr.pyke.acau_hardcore.command.*;
import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.config.RuneConfig;
import kr.pyke.acau_hardcore.data.randombox.BoxRegistry;
import kr.pyke.acau_hardcore.data.shop.ShopManager;
import kr.pyke.acau_hardcore.handler.ModHandlers;
import kr.pyke.acau_hardcore.network.AcauHardCorePacket;
import kr.pyke.acau_hardcore.party.PartyManager;
import kr.pyke.acau_hardcore.registry.attribute.ModAttributes;
import kr.pyke.acau_hardcore.registry.creativemodetabs.ModCreativeModeTabs;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import kr.pyke.acau_hardcore.registry.menu.ModMenus;
import kr.pyke.acau_hardcore.util.BlockBreakQueue;
import kr.pyke.acau_hardcore.util.HousingBuildQueue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AcauHardCore implements ModInitializer {
	public static final String MOD_ID = "acau_hardcore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER_INSTANCE;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			SERVER_INSTANCE = server;
			ModConfig.load(server);
			RuneConfig.load(server);
			ShopManager.load(server, false);
			BoxRegistry.load(server, false);
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> PartyManager.getServerState(server).restoreTeams(server));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			SERVER_INSTANCE = null;
			UserCommand.clearCooldowns();
		});

		AcauHardCorePacket.registerCodec();
		AcauHardCorePacket.registerServer();

		CommandRegistrationCallback.EVENT.register(DisplayNameCommand::register);
		CommandRegistrationCallback.EVENT.register(LobbyCommand::register);
		CommandRegistrationCallback.EVENT.register(ManageCommand::register);
		CommandRegistrationCallback.EVENT.register(UserCommand::register);
		CommandRegistrationCallback.EVENT.register(MailBoxCommand::register);
		CommandRegistrationCallback.EVENT.register(NoticeCommand::register);
		CommandRegistrationCallback.EVENT.register(AnnouncementCommand::register);
		CommandRegistrationCallback.EVENT.register(ShopCommand::register);
		CommandRegistrationCallback.EVENT.register(PartyCommand::register);
		CommandRegistrationCallback.EVENT.register(RaidCommand::register);

		ModAttributes.register();
		ModHandlers.register();
		ModItems.register();
		ModCreativeModeTabs.register();
		ModMenus.register();

		BlockBreakQueue.register();
		HousingBuildQueue.register();
	}

	public static List<ServerPlayer> getOps(MinecraftServer server) {
		if (server == null) { return List.of(); }

		return server.getPlayerList().getPlayers().stream().filter(player -> server.getPlayerList().isOp(player.nameAndId())).collect(Collectors.toList());
	}
}