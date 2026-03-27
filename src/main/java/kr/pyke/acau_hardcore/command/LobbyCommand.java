package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.ServerSavedData;
import kr.pyke.util.constants.COLOR;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class LobbyCommand {
    private LobbyCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("로비설정")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .executes(LobbyCommand::setLobbyPosition)
        );

        dispatcher.register(Commands.literal("로비이동")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .executes(LobbyCommand::teleportLobby)
        );

        dispatcher.register(Commands.literal("감옥설정")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .executes(LobbyCommand::setJailPosition)
        );

        dispatcher.register(Commands.literal("감옥이동")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .executes(LobbyCommand::teleportJail)
        );
    }

    private static int setLobbyPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ServerLevel serverLevel = serverPlayer.level();

        BlockPos blockPos = serverPlayer.blockPosition();
        Vec3 fixedPos = new Vec3(blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d);

        float yaw = serverPlayer.getYRot();

        ServerSavedData data = ServerSavedData.getServerState(context.getSource().getServer());
        data.setLobbyPosition(serverLevel, fixedPos, yaw);

        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("로비 위치가 설정되었습니다. (%.1f, %.1f, %.1f, %s)", fixedPos.x, fixedPos.y, fixedPos.z, serverLevel.dimension().identifier().getPath()));

        return 1;
    }

    private static int teleportLobby(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

        ServerSavedData data = ServerSavedData.getServerState(context.getSource().getServer());
        TeleportTransition transition = data.createLobbyTransition(context.getSource().getServer(), TeleportTransition.DO_NOTHING);

        if (transition == null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "로비 위치가 설정되지 않았거나, 해당 월드를 불러올 수 없습니다.");
            return 0;
        }

        serverPlayer.teleport(transition);

        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "로비로 이동했습니다.");

        return 1;
    }

    private static int setJailPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ServerLevel serverLevel = serverPlayer.level();

        BlockPos blockPos = serverPlayer.blockPosition();
        Vec3 fixedPos = new Vec3(blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d);

        float yaw = serverPlayer.getYRot();

        ServerSavedData data = ServerSavedData.getServerState(context.getSource().getServer());
        data.setJailPosition(serverLevel, fixedPos, yaw);

        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("감옥 위치가 설정되었습니다. (%.1f, %.1f, %.1f, %s)", fixedPos.x, fixedPos.y, fixedPos.z, serverLevel.dimension().identifier().getPath()));

        return 1;
    }

    private static int teleportJail(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

        ServerSavedData data = ServerSavedData.getServerState(context.getSource().getServer());
        TeleportTransition transition = data.createJailTransition(context.getSource().getServer(), TeleportTransition.DO_NOTHING);

        if (transition == null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "감옥 위치가 설정되지 않았거나, 해당 월드를 불러올 수 없습니다.");
            return 0;
        }

        serverPlayer.teleport(transition);

        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "감옥으로 이동했습니다.");

        return 1;
    }
}