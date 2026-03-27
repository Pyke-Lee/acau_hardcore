package kr.pyke.acau_hardcore.client.gui.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class NoticeHud {
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_COLOR = 0x80000000;
    private static final int TEXT_Y_OFFSET = 2;
    private static final double SCROLL_SPEED = 1.0;

    private static Component currentMessage = Component.empty();
    private static double currentX = 0;
    private static int textWidth = 0;
    private static boolean hasMessage = false;
    private static long expirationTime = 0;

    private NoticeHud() { }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!hasMessage) { return; }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) { return; }

        int screenWidth = guiGraphics.guiWidth();

        currentX -= SCROLL_SPEED;

        if (currentX < -textWidth) {
            if (System.currentTimeMillis() > expirationTime) {
                hasMessage = false;
                return;
            }
            currentX = screenWidth;
        }

        guiGraphics.fill(0, 0, screenWidth, BAR_HEIGHT, BAR_COLOR);

        guiGraphics.enableScissor(0, 0, screenWidth, BAR_HEIGHT);
        guiGraphics.drawString(mc.font, currentMessage, (int) currentX, TEXT_Y_OFFSET, 0xFFFFFFFF, false);
        guiGraphics.disableScissor();
    }

    public static void updateMessage(Component message, int durationSeconds) {
        if (message.getString().isEmpty()) {
            hasMessage = false;
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        currentMessage = message;
        textWidth = mc.font.width(message);
        currentX = mc.getWindow().getGuiScaledWidth();
        expirationTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        hasMessage = true;
    }
}