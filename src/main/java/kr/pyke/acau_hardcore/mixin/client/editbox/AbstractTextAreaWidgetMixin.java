package kr.pyke.acau_hardcore.mixin.client.editbox;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.screen.MailSendScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractTextAreaWidget.class)
public abstract class AbstractTextAreaWidgetMixin {
    @Unique
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
        Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "widget/text_field"),
        Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "widget/text_field_highlighted")
    );

    @Inject(method = "renderBorder", at = @At("HEAD"), cancellable = true)
    protected void renderBorder(GuiGraphics guiGraphics, int i, int j, int k, int l, CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof MailSendScreen) {
            AbstractWidget self = (AbstractWidget) (Object) this;

            Identifier resourceLocation = BACKGROUND_SPRITES.get(self.isActive(), self.isFocused());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, i, j, k, l);

            ci.cancel();
        }
    }
}