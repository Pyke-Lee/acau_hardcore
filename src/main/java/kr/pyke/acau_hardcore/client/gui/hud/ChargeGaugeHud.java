package kr.pyke.acau_hardcore.client.gui.hud;

import kr.pyke.acau_hardcore.client.gui.render.state.CircleGaugeRenderState;
import kr.pyke.acau_hardcore.mixin.client.gui.GuiGraphicsAccessor;
import kr.pyke.acau_hardcore.registry.item.scroll.TownReturnScrollItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

public class ChargeGaugeHud {
    private static final int COLOR_BG = 0xFF424242;
    private static final int COLOR_GAUGE = 0xFFD1D1D1;

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) { return; }

        ItemStack useStack = player.getUseItem();
        if (!(useStack.getItem() instanceof TownReturnScrollItem)) { return; }

        float remainingTicks = (float) player.getUseItemRemainingTicks();
        float maxDuration = (float) useStack.getUseDuration(player);
        float progress = Mth.clamp((maxDuration - remainingTicks) / maxDuration, 0.f, 1.f);

        int cx = guiGraphics.guiWidth() / 2;
        int cy = guiGraphics.guiHeight() / 2;

        GuiGraphicsAccessor accessor = (GuiGraphicsAccessor) guiGraphics;

        submitCircle(accessor, cx, cy, 20.f, 2.5f, 1.f, COLOR_BG);
        submitCircle(accessor, cx, cy, 20.f, 2.5f, progress, COLOR_GAUGE);

        String timeLeft = (remainingTicks / 20.f < 1.f) ? String.format("%.1f", remainingTicks / 20.f) : String.format("%.0f", remainingTicks / 20.f);
        guiGraphics.drawCenteredString(mc.font, timeLeft, cx, cy - 4, 0xFFFFFFFF);
    }

    private static void submitCircle(GuiGraphicsAccessor accessor, float cx, float cy, float r, float t, float p, int col) {
        Matrix3x2f matrix = new Matrix3x2f(accessor.getPose());

        accessor.getGuiRenderState().submitGuiElement(new CircleGaugeRenderState(RenderPipelines.GUI, matrix, cx, cy, r, t, p, col, null));
    }
}