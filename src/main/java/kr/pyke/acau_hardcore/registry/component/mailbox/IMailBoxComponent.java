package kr.pyke.acau_hardcore.registry.component.mailbox;

import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.acau_hardcore.type.MAIL_STATE;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.List;

public interface IMailBoxComponent extends ComponentV3, AutoSyncedComponent {
    List<MailBoxData> getMails();
    void addMail(MailBoxData mail);
    void removeMail(MailBoxData mail);
    void updateMail(MailBoxData mail, MAIL_STATE state);
    void clearAll();
    void claimMail(Player player, MailBoxData mail);
}
