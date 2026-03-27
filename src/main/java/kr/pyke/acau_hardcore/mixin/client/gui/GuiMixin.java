package kr.pyke.acau_hardcore.mixin.client.gui;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import kr.pyke.acau_hardcore.registry.item.scroll.TownReturnScrollItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique private static final Identifier HP_BAR_BG = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hud/hp_bar_bg");
    @Unique private static final Identifier HP_BAR_RED = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hud/hp_bar_red");
    @Unique private static final Identifier HP_BAR_DELAY = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hud/hp_bar_delay");
    @Unique private static final Identifier HP_BAR_ABSORPTION = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hud/hp_bar_absorption");
    @Unique private static final Identifier THIRST_BAR = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hud/thirst_bar");

    @Unique private float delayedHealth = -1.f;

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void rednerHearts(GuiGraphics graphics, Player player, int xLeft, int yLineBase, int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth, int absorption, boolean blink, CallbackInfo ci) {
        if (this.delayedHealth < 0.f) {
            this.delayedHealth = (float) currentHealth;
        }

        if (currentHealth > this.delayedHealth) {
            this.delayedHealth = (float) currentHealth;
        }
        else if (currentHealth < this.delayedHealth) {
            this.delayedHealth = Mth.lerp(0.1f, this.delayedHealth, (float) currentHealth);
            if (Math.abs(this.delayedHealth - currentHealth) < 0.1f) {
                this.delayedHealth = (float) currentHealth;
            }
        }

        int barWidth = 80;
        int barHeight = 8;
        int x = graphics.guiWidth() / 2 - 91;

        float healthRatio = Math.max(0.f, Math.min(1.f, (float) currentHealth / maxHealth));
        int currentBarWidth = (int) (barWidth * healthRatio);

        float delayRatio = Math.max(0.f, Math.min(1.f, this.delayedHealth / maxHealth));
        int delayBarWidth = (int) (barWidth * delayRatio);

        float absorptionRatio = Math.max(0.f, Math.min(1.f, (float) absorption / maxHealth));
        int absorptionBarWidth = (int) (barWidth * absorptionRatio);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HP_BAR_BG, x, yLineBase + 1, barWidth, barHeight);

        if (delayBarWidth > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HP_BAR_DELAY, barWidth, barHeight, 0, 0, x, yLineBase + 1, delayBarWidth, barHeight);
        }

        if (currentBarWidth > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HP_BAR_RED, barWidth, barHeight, 0, 0, x, yLineBase + 1, currentBarWidth, barHeight);
        }

        if (absorptionBarWidth > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HP_BAR_ABSORPTION, barWidth, barHeight, 0, 0, x, yLineBase + 1, absorptionBarWidth, barHeight);
        }

        String healthText = absorption > 0 ? currentHealth + "(+" + absorption + ")" : String.valueOf(currentHealth);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + barWidth / 2.0f, yLineBase + 2.0f);
        graphics.pose().scale(0.75f, 0.75f);
        graphics.drawCenteredString(this.minecraft.font, healthText, 0, 0, -1);
        graphics.pose().popMatrix();

        ci.cancel();
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V"))
    private void renderThirstBarAlways(GuiGraphics graphics, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null) { return; }

        IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);
        int thirstLevel = info.getThirstLevel();
        int barWidth = 80;
        int barHeight = 8;
        int xRight = graphics.guiWidth() / 2 + 91;
        int x = xRight - barWidth;
        int yLineThirst = graphics.guiHeight() - 49;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HP_BAR_BG, x, yLineThirst + 1, barWidth, barHeight);

        if (thirstLevel > 0) {
            int currentBarWidth = (int) (barWidth * (thirstLevel / 20.0f));
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, THIRST_BAR, barWidth, barHeight, 0, 0, x, yLineThirst + 1, currentBarWidth, barHeight);
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + barWidth / 2.0f, yLineThirst + 2.0f);
        graphics.pose().scale(0.75f, 0.75f);
        graphics.drawCenteredString(this.minecraft.font, String.valueOf(thirstLevel), 0, 0, -1);
        graphics.pose().popMatrix();
    }

    @Inject(method = "renderAirBubbles", at = @At("HEAD"))
    private void pushAirShift(GuiGraphics graphics, Player player, int vehicleHearts, int yLineAir, int xRight, CallbackInfo ci) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0f, -10.0f);
    }

    @Inject(method = "renderAirBubbles", at = @At("RETURN"))
    private void popAirShift(GuiGraphics graphics, Player player, int vehicleHearts, int yLineAir, int xRight, CallbackInfo ci) {
        graphics.pose().popMatrix();
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        LocalPlayer player = minecraft.player;
        if (player == null) { return; }

        ItemStack useStack = player.getUseItem();
        if (!(useStack.getItem() instanceof TownReturnScrollItem)) { return; }

        float remainingTicks = (float) player.getUseItemRemainingTicks();
        if (remainingTicks > 0.f) { ci.cancel(); }
    }
}
