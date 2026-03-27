package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_SendNoticePayload;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.List;

public class NoticeCommand {
    private NoticeCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        var commandNode = Commands.argument("duration", IntegerArgumentType.integer(1))
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(NoticeCommand::sendNotice)
            );

        dispatcher.register(Commands.literal("알림")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(commandNode)
        );

        dispatcher.register(Commands.literal("notice")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(commandNode)
        );
    }

    private static int sendNotice(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        int duration = IntegerArgumentType.getInteger(ctx, "duration");
        String rawMessage = StringArgumentType.getString(ctx, "message");
        String formattedMessage = rawMessage.replace("&", "§");

        S2C_SendNoticePayload packet = new S2C_SendNoticePayload(formattedMessage, duration);

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) { ServerPlayNetworking.send(player, packet); }

        PykeLib.sendSystemMessage(players, COLOR.LIME.getColor(), String.format("전체 알림을 전송했습니다. (지속시간: %d초)", duration));
        AcauHardCore.LOGGER.info("Notice: [{}] {}", duration, formattedMessage);

        return 1;
    }
}