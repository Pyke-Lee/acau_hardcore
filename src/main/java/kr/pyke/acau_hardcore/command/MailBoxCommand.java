package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import kr.pyke.acau_hardcore.client.gui.menu.MailSendMenu;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.SimpleMenuProvider;

public class MailBoxCommand {
    private MailBoxCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("우편")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(Commands.literal("보내기")
                .executes(MailBoxCommand::openSendMenu)
            )
        );
    }

    private static int openSendMenu(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer serverPlayer = ctx.getSource().getPlayer();
        if (null == serverPlayer) { return 0; }

        if (!serverPlayer.level().isClientSide()) {
            SimpleMenuProvider menuProvider = new SimpleMenuProvider((containerID, playerInventory, player) -> new MailSendMenu(containerID, playerInventory), Component.literal("우편 보내기"));
            serverPlayer.openMenu(menuProvider);
        }

        return 1;
    }
}
