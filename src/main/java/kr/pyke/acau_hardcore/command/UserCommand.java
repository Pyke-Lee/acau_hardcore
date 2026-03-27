package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequest;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequestData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_HelpRequestUpdatePayload;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.util.Utils;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserCommand {
    private UserCommand() { }

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, UUID> lastMessageSource = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("포기")
            .executes(UserCommand::giveUp)
        );

        dispatcher.register(Commands.literal("도움")
            .executes(context -> submitRequest(context, ""))

            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(context -> submitRequest(context, StringArgumentType.getString(context, "message")))
            )
        );

        dispatcher.register(Commands.literal("답")
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(UserCommand::sendReply)
            )
        );

        dispatcher.register(Commands.literal("ㄷ")
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(UserCommand::sendReply)
            )
        );

        dispatcher.register(Commands.literal("귓")
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(UserCommand::sendWhisper)
                )
            )
        );

        dispatcher.register(Commands.literal("ㄱ")
            .then(Commands.argument("target", EntityArgument.player())
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(UserCommand::sendWhisper)
                )
            )
        );

        dispatcher.register(Commands.literal("복귀")
            .executes(UserCommand::returnPosition)
        );
    }

    private static int giveUp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        ModComponents.HARDCORE_INFO.get(serverPlayer).stopHardCore();
        PykeLib.sendBroadcastMessage(server.getPlayerList().getPlayers(), COLOR.GRAY.getColor(), "&7???&f님께서 하드코어 도전을 포기하셨습니다.");

        return 1;
    }

    private static int submitRequest(CommandContext<CommandSourceStack> context, String message) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        UUID playerUuid = player.getUUID();

        long now = System.currentTimeMillis();
        long cooldownMs = (long) (ModConfig.INSTANCE.helpRequestCooldown * 1000);
        Long lastRequest = cooldowns.get(playerUuid);

        if (lastRequest != null) {
            long remaining = (lastRequest + cooldownMs) - now;
            if (remaining > 0) {
                int remainingSeconds = (int) Math.ceil(remaining / 1000.0);
                PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), String.format("도움 요청은 &7%s&f초 후에 다시 할 수 있습니다.", remainingSeconds));
                return 0;
            }
        }

        cooldowns.put(playerUuid, now);

        HelpRequestData data = HelpRequestData.get(context.getSource().getServer());
        HelpRequest request = data.addRequest(playerUuid, message);

        S2C_HelpRequestUpdatePayload payload = new S2C_HelpRequestUpdatePayload(request);
        for (ServerPlayer p : context.getSource().getServer().getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
        PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), "도움 요청이 접수되었습니다. 잠시만 기다려주세요.");

        return 1;
    }

    /** 서버 종료 시 쿨다운 초기화 */
    public static void clearCooldowns() {
        cooldowns.clear();
    }

    private static int sendWhisper(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        String message = StringArgumentType.getString(context, "message");

        return executeWhisper(sender, target, message);
    }

    private static int sendReply(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sender = context.getSource().getPlayerOrException();
        UUID targetUUID = lastMessageSource.get(sender.getUUID());
        if (targetUUID == null) {
            PykeLib.sendSystemMessage(sender, COLOR.RED.getColor(), "답장할 대상이 없습니다.");
            return 0;
        }

        ServerPlayer target = sender.level().getServer().getPlayerList().getPlayer(targetUUID);
        if (target == null) {
            PykeLib.sendSystemMessage(sender, COLOR.RED.getColor(), "상대방이 오프라인 상태입니다.");
            return 0;
        }

        String message = StringArgumentType.getString(context, "message");
        return executeWhisper(sender, target, message);
    }

    private static int executeWhisper(ServerPlayer sender, ServerPlayer target, String message) {
        String stripMessage = Utils.stripColor(message);

        PykeLib.sendSystemMessage(sender, COLOR.GRAY.getColor(), String.format("§8[나 → %s] §r%s", target.getDisplayName().getString(), stripMessage));
        PykeLib.sendSystemMessage(target, COLOR.GRAY.getColor(), String.format("§8[%s → 나] §r%s", sender.getDisplayName().getString(), stripMessage));

        lastMessageSource.put(target.getUUID(), sender.getUUID());
        lastMessageSource.put(sender.getUUID(), target.getUUID());

        return 1;
    }

    private static int returnPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var info = ModComponents.HARDCORE_INFO.get(serverPlayer);

        if (!serverPlayer.level().dimension().equals(Level.OVERWORLD)) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "중앙 마을에서만 사용하실 수 있습니다.");
            return 0;
        }
        if (info.isJail()) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "감옥에서는 사용하실 수 없습니다.");
            return 0;
        }

        if (info.teleportPrevPosition()) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "중앙 마을에 입장하기 전에 있던 위치로 이동하였습니다.");
        }
        else {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "이전 위치가 존재하지 않습니다.");
        }

        return 1;
    }
}
