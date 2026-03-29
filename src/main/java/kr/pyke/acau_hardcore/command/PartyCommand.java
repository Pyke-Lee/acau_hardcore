package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.party.Party;
import kr.pyke.acau_hardcore.party.PartyManager;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.util.constants.COLOR;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class PartyCommand {
    private PartyCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("파티")
            .then(Commands.literal("생성")
                .then(Commands.argument("party", StringArgumentType.greedyString())
                    .executes(PartyCommand::createParty)
                )
            )
            .then(Commands.literal("초대")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(PartyCommand::invitePlayer)
                )
            )
            .then(Commands.literal("수락")
                .executes(PartyCommand::acceptInvite)
            )
            .then(Commands.literal("거절")
                .executes(PartyCommand::rejectInvite)
            )
            .then(Commands.literal("탈퇴")
                .executes(PartyCommand::leaveParty)
            )
            .then(Commands.literal("추방")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(PartyCommand::kickMember)
                )
            )
            .then(Commands.literal("해산")
                .executes(PartyCommand::disbandParty)
            )
            .then(Commands.literal("위임")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(PartyCommand::transferLeader)
                )
            )
            .then(Commands.literal("정보")
                .executes(PartyCommand::partyInfo)
            )
        );
    }

    private static int createParty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "이미 파티에 소속되어 있습니다.");
            return 0;
        }

        var info = ModComponents.HARDCORE_INFO.get(player);
        if (!info.isStarted()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "하드코어를 시작해야 파티를 생성할 수 있습니다.");
            return 0;
        }
        if (info.isJail()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "감옥에서 파티를 생성할 수 없습니다.");
            return 0;
        }

        String teamName = StringArgumentType.getString(context, "party");
        Party party = manager.createParty(server, player, teamName);
        if (party == null) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티 생성에 실패했습니다.");
            return 0;
        }

        PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), "파티가 생성되었습니다! &e/파티 초대 <플레이어>&f로 파티원을 초대하세요.");
        return 1;
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티에 소속되어 있지 않습니다.");
            return 0;
        }

        Party party = manager.getPartyByPlayer(player.getUUID());
        if (party == null || !party.isLeader(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티장만 초대할 수 있습니다.");
            return 0;
        }
        if (player.getUUID().equals(target.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "자기 자신을 초대할 수 없습니다.");
            return 0;
        }

        var selfInfo = ModComponents.HARDCORE_INFO.get(player);
        var targetInfo = ModComponents.HARDCORE_INFO.get(target);

        if (targetInfo.getHardcoreType() != selfInfo.getHardcoreType()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "하드코어 유형이 같은 플레이어만 파티가 가능합니다.");
            return 0;
        }
        if (!selfInfo.isStarted()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "하드코어를 시작해야 파티원을 초대할 수 있습니다.");
            return 0;
        }
        if (!targetInfo.isStarted()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "하드코어를 시작하지 않은 플레이어를 초대할 수 없습니다.");
            return 0;
        }
        if (selfInfo.isJail()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "감옥에서 파티원을 초대할 수 없습니다.");
            return 0;
        }
        if (targetInfo.isJail()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "감옥에 있는 플레이어를 초대할 수 없습니다.");
            return 0;
        }

        manager.invitePlayer(server, player, target);
        return 1;
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "이미 파티에 소속되어 있습니다.");
            return 0;
        }
        if (!manager.hasPendingInvite(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "받은 파티 초대가 없습니다.");
            return 0;
        }

        manager.acceptInvite(server, player);
        return 1;
    }

    private static int rejectInvite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.hasPendingInvite(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "받은 파티 초대가 없습니다.");
            return 0;
        }

        manager.rejectInvite(server, player);
        return 1;
    }

    private static int leaveParty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티에 소속되어 있지 않습니다.");
            return 0;
        }

        manager.leaveParty(server, player);
        return 1;
    }

    private static int kickMember(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티에 소속되어 있지 않습니다.");
            return 0;
        }

        manager.kickMember(server, player, target);
        return 1;
    }

    private static int disbandParty(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티에 소속되어 있지 않습니다.");
            return 0;
        }

        Party party = manager.getPartyByPlayer(player.getUUID());
        if (party == null || !party.isLeader(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티장만 해산할 수 있습니다.");
            return 0;
        }

        manager.disbandParty(server, player);
        return 1;
    }

    private static int transferLeader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티에 소속되어 있지 않습니다.");
            return 0;
        }

        manager.transferLeader(server, player, target);
        return 1;
    }

    private static int partyInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        PartyManager manager = PartyManager.getServerState(server);
        if (!manager.isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티에 소속되어 있지 않습니다.");
            return 0;
        }

        Party party = manager.getPartyByPlayer(player.getUUID());
        if (party == null) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티 정보를 불러올 수 없습니다.");
            return 0;
        }

        PykeLib.sendSystemMessage(player, 0xFFFFFF, "");
        PykeLib.sendSystemMessage(player, COLOR.AQUA.getColor(), String.format("파티 정보 (%d/%d명)", party.getMemberCount(), Party.MAX_MEMBERS));

        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            String name = member != null ? member.getDisplayName().getString() : "오프라인";
            String status = member != null ? "&a온라인" : "&7오프라인";
            String leaderTag = party.isLeader(memberId) ? " &6[파티장]" : "";

            PykeLib.sendSystemMessage(player, 0xFFFFFF, String.format("  %s%s &8- %s", name, leaderTag, status));
        }

        PykeLib.sendSystemMessage(player, 0xFFFFFF, "");
        return 1;
    }
}