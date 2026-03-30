package kr.pyke.acau_hardcore.mixin.server.chat;

import kr.pyke.acau_hardcore.prefix.PrefixData;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.util.ColorParser;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatType.class)
public abstract class ChatTypeMixin {
    @Inject(
        method = "bind(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/ChatType$Bound;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onBindChatType(ResourceKey<ChatType> chatTypeKey, Entity entity, CallbackInfoReturnable<ChatType.Bound> cir) {
        if (chatTypeKey == ChatType.CHAT && entity instanceof ServerPlayer player) {
            MutableComponent nameComponent = Component.empty();
            var prefixes = ModComponents.PREFIXES.get(player);

            if (!prefixes.getSelectedPrefix().equals("none")) {
                PrefixData prefixData = PrefixRegistry.get(prefixes.getSelectedPrefix());
                if (prefixData != null) {
                    nameComponent.append(ColorParser.parse(prefixData.prefix())).append(" ");
                }
            }

            nameComponent.append(player.getDisplayName());

            cir.setReturnValue(ChatType.bind(chatTypeKey, entity.level().registryAccess(), nameComponent));
        }
    }
}