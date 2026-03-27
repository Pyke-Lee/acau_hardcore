package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.boss.raid.BossRaidManager;
import kr.pyke.util.constants.COLOR;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class RaidCommand {
    private RaidCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("레이드")
            .executes(RaidCommand::openRaidGui)
            .then(Commands.literal("포기")
                .executes(RaidCommand::forfeitRaid)
            )
        );
    }

    private static int openRaidGui(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BossRaidManager.openRaidSelect(player);
        return 1;
    }

    private static int forfeitRaid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!BossRaidManager.isInRaid(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "현재 레이드에 참여 중이 아닙니다.");
            return 0;
        }

        BossRaidManager.forfeitRaid(context.getSource().getServer(), player);
        return 1;
    }
}