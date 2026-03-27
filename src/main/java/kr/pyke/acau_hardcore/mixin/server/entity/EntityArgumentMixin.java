package kr.pyke.acau_hardcore.mixin.server.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(EntityArgument.class)
public class EntityArgumentMixin {
    @WrapOperation(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;fillSuggestions(Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Suggestions> fillSuggestions(EntitySelectorParser instance, SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer, Operation<CompletableFuture<Suggestions>> original) {
        return original.call(instance, builder, (Consumer<SuggestionsBuilder>) list -> {
            consumer.accept(list);

            var vanilla = new HashSet<String>();
            list.build().getList().forEach(s -> {
                String raw = s.getText();
                if (raw == null) { return; }
                String lowerRaw = raw.toLowerCase(Locale.ROOT);
                vanilla.add(lowerRaw);
                if (lowerRaw.length() >= 2 && lowerRaw.startsWith("\"") && lowerRaw.endsWith("\"")) {
                    vanilla.add(lowerRaw.substring(1, lowerRaw.length() - 1));
                }
            });

            var connection = Minecraft.getInstance().getConnection();
            if (connection == null) { return; }

            Collection<PlayerInfo> onlinePlayers = connection.getOnlinePlayers();
            if (onlinePlayers.isEmpty()) { return; }

            final String remainingRaw = builder.getRemaining() == null ? "" : builder.getRemaining();
            final String remaining = remainingRaw.toLowerCase(Locale.ROOT);
            final String remainingNoQuote = remaining.startsWith("\"") ? remaining.substring(1) : remaining;

            var added = new HashSet<String>();

            for (PlayerInfo info : onlinePlayers) {
                String plain = AcauHardCoreCache.displayNames.get(info.getProfile().id());
                if (plain == null || plain.isEmpty()) { continue; }

                String lower = plain.toLowerCase(Locale.ROOT);
                if (vanilla.contains(lower) || vanilla.contains("\"" + lower + "\"")) { continue; }

                String quoted = "\"" + plain + "\"";
                String quotedLower = quoted.toLowerCase(Locale.ROOT);

                boolean allow;
                if (remaining.isEmpty()) { allow = true; }
                else if (remaining.startsWith("\"")) { allow = quotedLower.startsWith(remaining); }
                else { allow = lower.startsWith(remainingNoQuote) || quotedLower.startsWith("\"" + remaining); }

                if (!allow) { continue; }
                if (added.add(lower)) { list.suggest(quoted); }
            }
        });
    }
}