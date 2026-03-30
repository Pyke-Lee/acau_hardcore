package kr.pyke.acau_hardcore.client.gui.hud;

import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

public class AcauHardCoreHudOverlay {
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) { return; }

        renderHardCoreInfo(guiGraphics, mc, player);
    }

    public void renderHardCoreInfo(GuiGraphics guiGraphics, Minecraft minecraft, LocalPlayer player) {
        IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);

        long totalSeconds = info.getTotalPlayTime() / 20;
        String totalPlayTimeText = String.format("플레이 타임: %02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);

        int currentEarth = info.getDeathCount() + 1;
        String liveAndEarthText;
        if (info.isStarted()) {
            long liveSeconds = info.getCurrentLiveTime() / 20;
            liveAndEarthText = String.format("생존 시간: %02d:%02d:%02d (%d 지구)", liveSeconds / 3600, (liveSeconds % 3600) / 60, liveSeconds % 60, currentEarth);
        }
        else {
            liveAndEarthText = String.format("생존 시간: --:--:-- (%d 지구)", currentEarth);
        }

        String displayName = player.getDisplayName().getString();
        if (info.isStarted()) { displayName += "(" + info.getHardcoreType().getDisplayName() + ")"; }

        String dimensionText = Component.translatable(getDimensionTranslationKey(player.level().dimension())).getString();
        if (info.isJail()) { dimensionText = "감옥"; }

        String currencyText = String.format("소지금: %,d 원", info.getCurrency());

        float baseScale = (float) minecraft.getWindow().getGuiScale() / 3.5f;
        float finalScale = Math.clamp(baseScale, 0.9f, 1.5f);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(finalScale, finalScale);

        int startX = 8;
        int startY = 8;
        int headSize = 32;
        int padding = 2;

        int headBgSize = headSize + (padding * 2);
        guiGraphics.fill(startX, startY, startX + headBgSize, startY + headBgSize, 0x80000000);

        PlayerFaceRenderer.draw(guiGraphics, player.getSkin().body().texturePath(), startX + padding, startY + padding, headSize, true, false, 0xFFFFFFFF);

        int dimTextWidth = minecraft.font.width(dimensionText);
        int dimBgWidth = Math.max(headBgSize, dimTextWidth + 6);
        int dimY = startY + headBgSize + 2;
        int dimBgHeight = minecraft.font.lineHeight;

        guiGraphics.fill(startX, dimY, startX + dimBgWidth, dimY + dimBgHeight, 0x80000000);
        int dimTextX = startX + (dimBgWidth - dimTextWidth) / 2;
        guiGraphics.drawString(minecraft.font, dimensionText, dimTextX, dimY + 1, 0xFFFFFFFF, true);

        int textX = startX + headBgSize + 2;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(textX, (float) startY);
        guiGraphics.pose().scale(0.83f, 0.83f);

        int localY = 0;
        int paddingX = 3;
        int textBgHeight = minecraft.font.lineHeight + 1;

        int minWidth = 90;
        int lineSpacing = 1;

        int nameWidth = minecraft.font.width(displayName);
        guiGraphics.fill(0, localY, nameWidth + (paddingX * 2), localY + textBgHeight, 0x80000000);
        guiGraphics.drawString(minecraft.font, displayName, paddingX, localY + 1, 0xFFE0E0E0, true);

        localY += textBgHeight + lineSpacing;

        int totalTimeWidth = Math.max(minWidth, minecraft.font.width(totalPlayTimeText));
        guiGraphics.fill(0, localY, totalTimeWidth + (paddingX * 2), localY + textBgHeight, 0x80000000);
        guiGraphics.drawString(minecraft.font, totalPlayTimeText, paddingX, localY + 1, 0xFFE0E0E0, true);

        localY += textBgHeight + lineSpacing;

        int liveWidth = Math.max(minWidth, minecraft.font.width(liveAndEarthText));
        guiGraphics.fill(0, localY, liveWidth + (paddingX * 2), localY + textBgHeight, 0x80000000);
        guiGraphics.drawString(minecraft.font, liveAndEarthText, paddingX, localY + 1, 0xFFE0E0E0, true);

        localY += textBgHeight + lineSpacing;

        int currencyWidth = minecraft.font.width(currencyText);
        guiGraphics.fill(0, localY, currencyWidth + (paddingX * 2), localY + textBgHeight, 0x80000000);
        guiGraphics.drawString(minecraft.font, currencyText, paddingX, localY + 1, 0xFFE0E0E0, true);

        guiGraphics.pose().popMatrix();
        guiGraphics.pose().popMatrix();
    }

    private static String getDimensionTranslationKey(ResourceKey<Level> dimension) {
        return "dimension." + dimension.identifier().getNamespace() + "." + dimension.identifier().getPath();
    }
}
