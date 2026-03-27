package kr.pyke.acau_hardcore.registry.component.mailbox;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.type.MAIL_STATE;
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MailBoxComponent implements IMailBoxComponent {
    final Player player;
    List<MailBoxData> mailBoxData = new ArrayList<>();

    public MailBoxComponent(Player player) { this.player = player; }

    public List<MailBoxData> getMails() { return mailBoxData; }

    public void addMail(MailBoxData mail) {
        mailBoxData.add(mail);

        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.YELLOW.getColor(), "새 우편이 도착했습니다!");
        this.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        ModComponents.MAIL_BOX.sync(player);
    }

    public void removeMail(MailBoxData mail) {
        mailBoxData.remove(mail);
        ModComponents.MAIL_BOX.sync(player);
    }

    public void updateMail(MailBoxData mail, MAIL_STATE state) {
        MailBoxData updatedMail = mail.withState(MAIL_STATE.READ);
        mailBoxData.replaceAll(m -> m.mailUUID().equals(updatedMail.mailUUID()) ? updatedMail : m);
        ModComponents.MAIL_BOX.sync(player);
    }

    public void clearAll() {
        mailBoxData.clear();
        ModComponents.MAIL_BOX.sync(player);
    }

    public void claimMail(Player player, MailBoxData mail) {
        for (ItemStack itemStack : mail.itemStackList()) {
            ItemStack copyItem = itemStack.copy();
            player.getInventory().add(copyItem);

            if (!copyItem.isEmpty()) { player.drop(copyItem, false); }
        }

        updateMail(mail, MAIL_STATE.READ);
        ModComponents.MAIL_BOX.sync(player);
    }

    @Override
    public void readData(ValueInput input) {
        mailBoxData.clear();

        Optional<ValueInput.ValueInputList> mailsList = input.childrenList("Mails");
        if (mailsList.isEmpty()) { return; }

        for (ValueInput mailInput : mailsList.get()) {
            Optional<UUID> uuid = mailInput.read("UUID", UUIDUtil.CODEC);
            if (uuid.isEmpty()) { continue; }

            String title = mailInput.getStringOr("Title", "");
            String sender = mailInput.getStringOr("Sender", "");
            long date = mailInput.getLongOr("Date", 0L);
            String message = mailInput.getStringOr("Message", "");
            int state = mailInput.getIntOr("STATE", 0);

            List<ItemStack> items = new ArrayList<>();
            Optional<ValueInput.TypedInputList<ItemStack>> itemsList = mailInput.list("Items", ItemStack.CODEC);
            if (itemsList.isPresent()) {
                for (ItemStack item : itemsList.get()) {
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }

            mailBoxData.add(new MailBoxData(uuid.get(), title, sender, date, message, items, MAIL_STATE.byID(state)));
        }
    }

    @Override
    public void writeData(ValueOutput output) {
        ValueOutput.ValueOutputList mailsList = output.childrenList("Mails");

        for (MailBoxData mail : mailBoxData) {
            ValueOutput mailOutput = mailsList.addChild();

            mailOutput.store("UUID", UUIDUtil.CODEC, mail.mailUUID());

            mailOutput.putString("Title", mail.mailTitle());
            mailOutput.putString("Sender", mail.senderName());
            mailOutput.putLong("Date", mail.sentDate());
            mailOutput.putString("Message", mail.mailMessage());
            mailOutput.putInt("STATE", mail.state().getID());

            ValueOutput.TypedOutputList<ItemStack> itemsList = mailOutput.list("Items", ItemStack.CODEC);
            for (ItemStack item : mail.itemStackList()) {
                if (!item.isEmpty()) {
                    itemsList.add(item);
                }
            }
        }
    }
}