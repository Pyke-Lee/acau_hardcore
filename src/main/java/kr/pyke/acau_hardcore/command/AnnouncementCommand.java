package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.util.Utils;
import kr.pyke.util.constants.COLOR;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.List;

public class AnnouncementCommand {
    private AnnouncementCommand() { }

    private static final SuggestionProvider<CommandSourceStack> COLOR_SUGGESTER = (ctx, builder) -> {
        for (COLOR color : COLOR.values()) { builder.suggest(color.toString()); }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        var commandNode = Commands.argument("color", StringArgumentType.word()).suggests(COLOR_SUGGESTER)
            .then(Commands.argument("message", StringArgumentType.greedyString())
                .executes(AnnouncementCommand::sendAnnouncement)
            );

        dispatcher.register(Commands.literal("공지사항")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(commandNode)
        );

        dispatcher.register(Commands.literal("anc")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(commandNode)
        );
    }

    private static int sendAnnouncement(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        String typeStr = StringArgumentType.getString(ctx, "color");
        COLOR color = Utils.parseEnum(typeStr, COLOR.class);

        String rawMessage = StringArgumentType.getString(ctx, "message");
        String formattedMessage = rawMessage.replace("&", "§");

        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        PykeLib.sendBroadcastMessage(players, color == null ? 0xFFFFFF : color.getColor(), formattedMessage);
        AcauHardCore.LOGGER.info("Announcement: [{}] {}", color == null ? "white" : color.name(), Component.literal(formattedMessage));

        return 1;
    }
}