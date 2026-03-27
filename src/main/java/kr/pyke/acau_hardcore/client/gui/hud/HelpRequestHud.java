package kr.pyke.acau_hardcore.client.gui.hud;

import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class HelpRequestHud {
    private static final int ICON_SIZE = 16;
    private static final int MARGIN = 8;

    private static final int ICON_BG = 0xFF2D2D2D;
    private static final int ICON_BORDER = 0xFF6E6E6E;
    private static final int TEXT_WHITE = 0xFFFFFFFF;

    private static int currentCount = 0;

    private HelpRequestHud() { }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) { return; }

        refreshCountFromState();
        if (currentCount <= 0) { return; }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        Font font = mc.font;

        String countText = currentCount > 99 ? "99+" : String.valueOf(currentCount);
        int textWidth = font.width(countText);
        int padding = 4;
        int boxWidth = Math.max(textWidth + padding * 2, ICON_SIZE);

        int iconX = screenWidth - boxWidth - MARGIN;
        int iconY = screenHeight - ICON_SIZE - MARGIN;

        guiGraphics.fill(iconX - 1, iconY - 1, iconX + boxWidth + 1, iconY + ICON_SIZE + 1, ICON_BORDER);
        guiGraphics.fill(iconX, iconY, iconX + boxWidth, iconY + ICON_SIZE, ICON_BG);

        int textX = iconX + (boxWidth - textWidth) / 2;
        int textY = iconY + (ICON_SIZE - font.lineHeight) / 2;
        guiGraphics.drawString(font, countText, textX, textY, TEXT_WHITE, true);
    }

    public static void refreshCountFromState() {
        updateCount(AcauHardCoreCache.getWaitingCount());
    }

    public static void updateCount(int newCount) {
        currentCount = Math.max(0, newCount);
    }
}
