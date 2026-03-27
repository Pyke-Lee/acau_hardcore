package kr.pyke.acau_hardcore.client.gui.screen;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.menu.MailSendMenu;
import kr.pyke.acau_hardcore.network.payload.c2s.C2S_SendMailPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class MailSendScreen extends AbstractContainerScreen<MailSendMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "textures/gui/mail_send.png");

    private EditBox receiverEdit;
    private EditBox senderEdit;
    private EditBox titleEdit;
    private MultiLineEditBox contentEdit;

    public MailSendScreen(MailSendMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 250;
        this.inventoryLabelY = this.imageHeight - 88;
        this.titleLabelY = -10;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int startY = y + 25;
        int gap = 18;
        int labelWidth = 40;
        int editBoxX = x + labelWidth + 3;
        int editBoxWidth = 122;

        this.receiverEdit = new EditBox(this.font, editBoxX, startY, editBoxWidth, 14, Component.literal("Receiver"));
        this.receiverEdit.setMaxLength(16);
        this.receiverEdit.setBordered(true);
        this.addRenderableWidget(this.receiverEdit);

        this.senderEdit = new EditBox(this.font, editBoxX, startY + gap, editBoxWidth, 14, Component.literal("Sender"));
        this.senderEdit.setMaxLength(16);
        this.senderEdit.setBordered(true);
        this.addRenderableWidget(this.senderEdit);

        this.titleEdit = new EditBox(this.font, editBoxX, startY + gap * 2, editBoxWidth, 14, Component.literal("Title"));
        this.titleEdit.setMaxLength(30);
        this.addRenderableWidget(this.titleEdit);

        int contentY = startY + gap * 3;
        int contentHeight = 50;

        this.contentEdit = MultiLineEditBox.builder()
            .setX(editBoxX)
            .setY(contentY)
            .setPlaceholder(Component.literal("Content"))
            .build(this.font, editBoxWidth, contentHeight, Component.literal("Content"));
        this.contentEdit.setCharacterLimit(1000);
        this.addRenderableWidget(this.contentEdit);

        this.addRenderableWidget(Button.builder(Component.literal("보내기"), button -> sendMail())
            .bounds(x + 125, y + 136, 40, 20)
            .build());
    }

    private void sendMail() {
        String receiver = this.receiverEdit.getValue();
        String sender = this.senderEdit.getValue();
        String title = this.titleEdit.getValue();
        String content = this.contentEdit.getValue();

        if (!receiver.isEmpty() && !title.isEmpty()) {
            ClientPlayNetworking.send(new C2S_SendMailPayload(receiver, sender, title, content));
            this.onClose();
        }
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int startY = y + 25;
        int gap = 18;
        int labelX = x + 8;
        int labelColor = 0xFF404040;

        guiGraphics.drawString(this.font, "수신자", labelX, startY + 3, labelColor, false);
        guiGraphics.drawString(this.font, "발신자", labelX, startY + gap + 3, labelColor, false);
        guiGraphics.drawString(this.font, "제목", labelX, startY + gap * 2 + 3, labelColor, false);
        guiGraphics.drawString(this.font, "내용", labelX, startY + gap * 3 + 3, labelColor, false);

        String text = this.contentEdit.getValue();
        int maxLimit = 1000;
        String counter = text.length() + "/" + maxLimit;

        int color = (text.length() >= maxLimit) ? 0xFFFF5555 : 0xFFAAAAAA;

        int countX = this.contentEdit.getX() + this.contentEdit.getWidth() - this.font.width(counter) - 2;
        int countY = this.contentEdit.getY() + this.contentEdit.getHeight() - 10;

        guiGraphics.drawString(this.font, counter, countX, countY, color, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY) { }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            this.onClose();
            return true;
        }

        if (this.receiverEdit.keyPressed(event) || this.senderEdit.keyPressed(event) || this.titleEdit.keyPressed(event) || this.contentEdit.keyPressed(event)) {
            return true;
        }

        if (this.receiverEdit.isFocused() || this.senderEdit.isFocused() || this.titleEdit.isFocused() || this.contentEdit.isFocused()) {
            return true;
        }

        return super.keyPressed(event);
    }
}