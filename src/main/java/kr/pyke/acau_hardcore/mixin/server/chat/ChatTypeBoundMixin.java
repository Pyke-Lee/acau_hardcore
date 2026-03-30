package kr.pyke.acau_hardcore.mixin.server.chat;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatType.Bound.class)
public abstract class ChatTypeBoundMixin {
    @Shadow public abstract Holder<ChatType> chatType();
    @Shadow public abstract Component name();

    @Inject(method = "decorate", at = @At("HEAD"), cancellable = true)
    private void onDecorate(Component content, CallbackInfoReturnable<Component> cir) {
        if (this.chatType().is(ChatType.CHAT)) {
            MutableComponent formattedMessage = Component.empty()
                .append(this.name())
                .append(Component.literal(": "))
                .append(content);

            cir.setReturnValue(formattedMessage);
        }
    }
}