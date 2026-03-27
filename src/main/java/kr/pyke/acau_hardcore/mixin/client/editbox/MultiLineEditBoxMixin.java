package kr.pyke.acau_hardcore.mixin.client.editbox;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiLineEditBox.class)
public class MultiLineEditBoxMixin {
    @Redirect(
        method = "renderDecorations",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
        )
    )
    private void removeCharacterLimitText(GuiGraphics instance, Font font, Component component, int x, int y, int color) {

    }
}