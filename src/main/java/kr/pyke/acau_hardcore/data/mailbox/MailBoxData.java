package kr.pyke.acau_hardcore.data.mailbox;

import kr.pyke.acau_hardcore.type.MAIL_STATE;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public record MailBoxData(UUID mailUUID, String mailTitle, String senderName, long sentDate, String mailMessage, List<ItemStack> itemStackList, MAIL_STATE state) {
    public static MailBoxData create(String title, String sender, String message, List<ItemStack> itemStackList) {
        return new MailBoxData(UUID.randomUUID(), title, sender, System.currentTimeMillis(), message, itemStackList, MAIL_STATE.UNREAD);
    }

    public MailBoxData withState(MAIL_STATE state) {
        return new MailBoxData(this.mailUUID, this.mailTitle, this.senderName, this.sentDate, this.mailMessage, itemStackList, state);
    }
}
