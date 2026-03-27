package kr.pyke.acau_hardcore.mixin.client.editbox;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.screen.MailSendScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
    @Unique
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
        Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "widget/text_field"),
        Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "widget/text_field_highlighted")
    );

    @Redirect(
        method = "renderWidget",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
        )
    )
    private void renderCustomBorder(GuiGraphics guiGraphics, RenderPipeline pipeline, Identifier originalTexture, int x, int y, int width, int height) {
        AbstractWidget self = (AbstractWidget) (Object) this;

        if (Minecraft.getInstance().screen instanceof MailSendScreen) {
            Identifier customTexture = BACKGROUND_SPRITES.get(self.isActive(), self.isFocused());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, customTexture, x, y, width, height);
        }
        else {
            guiGraphics.blitSprite(pipeline, originalTexture, x, y, width, height);
        }
    }
}