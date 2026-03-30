package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.config.PrefixConfig;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenPrefixScreenPayload;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.prefix.IPrefixes;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class PrefixCommand {
    private PrefixCommand() { }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREFIX_IDS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(PrefixRegistry.getKeys(), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("칭호")
            .executes(PrefixCommand::openPrefixScreen)
        );

        dispatcher.register(Commands.literal("칭호관리")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(Commands.literal("지급")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("id", StringArgumentType.word())
                        .suggests(SUGGEST_PREFIX_IDS)
                        .executes(PrefixCommand::grantPrefix)
                    )
                )
            )
            .then(Commands.literal("회수")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("id", StringArgumentType.word())
                        .suggests(SUGGEST_PREFIX_IDS)
                        .executes(PrefixCommand::revokePrefix)
                    )
                )
            )
            .then(Commands.literal("리로드")
                .executes(PrefixCommand::reloadConfig)
            )
        );
    }

    private static int openPrefixScreen(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

        S2C_OpenPrefixScreenPayload payload = new S2C_OpenPrefixScreenPayload();
        ServerPlayNetworking.send(serverPlayer, payload);

        return 1;
    }

    private static int grantPrefix(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        String id = StringArgumentType.getString(context, "id");

        if (PrefixRegistry.get(id) == null) {
            if (serverPlayer != null) {
                PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "존재하지 않는 칭호 ID입니다.");
            }
            return 0;
        }

        IPrefixes prefixes = ModComponents.PREFIXES.get(target);
        if (prefixes.getPrefixes().contains(id)) {
            if (serverPlayer != null) {
                PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "플레이어가 이미 해당 칭호를 보유하고 있습니다.");
            }
            return 0;
        }

        prefixes.addPrefix(id);

        String prefixDisplay = PrefixRegistry.get(id).prefix();

        if (serverPlayer != null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&r님에게 칭호 &7%s&r를 지급했습니다.", target.getDisplayName().getString(), prefixDisplay));
        }
        PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), String.format("새로운 칭호 &7%s&r를 획득했습니다!", prefixDisplay));

        return 1;
    }

    private static int revokePrefix(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        String id = StringArgumentType.getString(context, "id");

        IPrefixes prefixes = ModComponents.PREFIXES.get(target);
        if (!prefixes.getPrefixes().contains(id)) {
            if (serverPlayer != null) {
                PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "플레이어가 해당 칭호를 보유하고 있지 않습니다.");
            }
            return 0;
        }

        prefixes.removePrefix(id);

        if (serverPlayer != null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&f님의 &7%s&f 칭호를 회수했습니다.", target.getDisplayName().getString(), id));
        }

        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        ServerPlayer serverPlayer = context.getSource().getPlayer();
        boolean success = PrefixConfig.reload(context.getSource().getServer());

        if (success) {
            if (serverPlayer != null) {
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "칭호 설정을 성공적으로 리로드했습니다.");
            }
            return 1;
        }
        else {
            if (serverPlayer != null) {
                PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "칭호 설정 리로드 중 오류가 발생했습니다. 콘솔을 확인하세요.");
            }
            return 0;
        }
    }
}