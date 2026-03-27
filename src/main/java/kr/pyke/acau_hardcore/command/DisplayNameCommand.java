package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenChangeDisplayNameScreenPayload;
import kr.pyke.acau_hardcore.util.Utils;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class DisplayNameCommand {
    private DisplayNameCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("이름변경")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(Commands.argument("target", EntityArgument.player())
                .executes(DisplayNameCommand::openChangeDisplayNameScreen)

                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                    .executes(DisplayNameCommand::changeDisplayName)
                )
            )
        );
    }

    private static int changeDisplayName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        String displayName = StringArgumentType.getString(context, "displayName");

        Utils.updateDisplayName(target, displayName, serverPlayer);

        return 1;
    }

    private static int openChangeDisplayNameScreen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");

        targets.forEach(target -> {
           S2C_OpenChangeDisplayNameScreenPayload payload = new S2C_OpenChangeDisplayNameScreenPayload();
           ServerPlayNetworking.send(target, payload);
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&f명에게 닉네임 변경을 권유하였습니다.", targets.size()));

        return 1;
    }
}