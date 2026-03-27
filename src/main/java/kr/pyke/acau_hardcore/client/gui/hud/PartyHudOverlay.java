package kr.pyke.acau_hardcore.client.gui.hud;

import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_PartySyncPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public class PartyHudOverlay {
    private PartyHudOverlay() { }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) { return; }

        if (!AcauHardCoreCache.isInParty()) { return; }

        List<S2C_PartySyncPayload.MemberData> otherMembers = AcauHardCoreCache.getOtherPartyMembers();
        if (otherMembers.isEmpty()) { return; }

        int screenHeight = guiGraphics.guiHeight();

        float baseScale = (float) mc.getWindow().getGuiScale() / 3.5f;
        float finalScale = Math.max(0.8f, Math.min(baseScale, 1.2f));

        int entryHeight = 24;
        int totalHeight = otherMembers.size() * entryHeight;
        int startY = (screenHeight - totalHeight) / 2;
        int startX = 4;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(finalScale, finalScale);

        int scaledStartX = (int) (startX / finalScale);
        int scaledStartY = (int) (startY / finalScale);

        for (int i = 0; i < otherMembers.size(); i++) {
            S2C_PartySyncPayload.MemberData member = otherMembers.get(i);
            int y = scaledStartY + (i * (int) (entryHeight / finalScale));

            renderMemberEntry(guiGraphics, mc, member, scaledStartX, y);
        }

        guiGraphics.pose().popMatrix();
    }

    private static void renderMemberEntry(GuiGraphics guiGraphics, Minecraft mc, S2C_PartySyncPayload.MemberData member, int x, int y) {
        int headSize = 16;
        int padding = 2;
        int barWidth = 60;
        int barHeight = 5;

        int textContentWidth = Math.max(mc.font.width(member.name()), barWidth + 30);
        int bgWidth = headSize + padding + textContentWidth + 8;
        int bgHeight = headSize + padding * 2;
        guiGraphics.fill(x, y, x + bgWidth, y + bgHeight, 0x60000000);

        PlayerInfo playerInfo = mc.getConnection() != null ? mc.getConnection().getPlayerInfo(member.uuid()) : null;
        if (playerInfo != null) {
            PlayerFaceRenderer.draw(guiGraphics, playerInfo.getSkin().body().texturePath(), x + padding, y + padding, headSize - padding, true, false, 0xFFFFFFFF);
        }
        else {
            guiGraphics.fill(x + padding, y + padding, x + padding + headSize - padding, y + padding + headSize - padding, 0xFF555555);
        }

        int textX = x + headSize + padding + 4;

        boolean isLeader = member.uuid().equals(AcauHardCoreCache.getPartyLeaderId());
        String nameText = member.name();
        if (isLeader) {
            nameText = "★ " + nameText;
        }

        int nameColor = member.online() ? 0xFFFFFFFF : 0xFF888888;
        guiGraphics.drawString(mc.font, nameText, textX, y + padding, nameColor, true);

        int barY = y + padding + mc.font.lineHeight + 1;

        if (member.online()) {
            float healthRatio = member.maxHealth() > 0 ? member.health() / member.maxHealth() : 0f;
            healthRatio = Math.max(0f, Math.min(1f, healthRatio));

            guiGraphics.fill(textX, barY, textX + barWidth, barY + barHeight, 0xFF333333);

            int filledWidth = (int) (barWidth * healthRatio);
            int barColor = getHealthColor(healthRatio);
            if (filledWidth > 0) {
                guiGraphics.fill(textX, barY, textX + filledWidth, barY + barHeight, barColor);
            }

            guiGraphics.fill(textX, barY, textX + barWidth, barY + 1, 0x40FFFFFF);
            guiGraphics.fill(textX, barY + barHeight - 1, textX + barWidth, barY + barHeight, 0x40000000);

            String healthText = String.format("%.0f/%.0f", member.health(), member.maxHealth());
            guiGraphics.drawString(mc.font, healthText, textX + barWidth + 3, barY - 1, 0xFFCCCCCC, true);
        }
        else {
            guiGraphics.drawString(mc.font, "오프라인", textX, barY - 1, 0xFF666666, true);
        }
    }

    private static int getHealthColor(float ratio) {
        if (ratio > 0.6f) { return 0xFF55FF55; }
        else if (ratio > 0.3f) { return 0xFFFFFF55; }
        else { return 0xFFFF5555; }
    }
}