package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.shop.ShopManager;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenShopPayload;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.concurrent.CompletableFuture;

public class ShopCommand {
    private ShopCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("상점")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(Commands.literal("열기")
               .then(Commands.argument("id", StringArgumentType.string())
                   .suggests(ShopCommand::suggest)
                   .executes(ShopCommand::openShop)
               )
            )
            .then(Commands.literal("리로드")
                .executes(ShopCommand::loadShop)
            )
        );
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestionsBuilder) {
        ShopManager.getShops().keySet().forEach(suggestionsBuilder::suggest);

        return suggestionsBuilder.buildFuture();
    }

    private static int openShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String shopID = StringArgumentType.getString(context, "id");

        ServerPlayNetworking.send(player, new S2C_OpenShopPayload(shopID));

        return 1;
    }

    private static int loadShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();

        ShopManager.load(server, true);
        PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), "상점 데이터가 로드 되었습니다.");
        return 1;
    }
}
