package kr.pyke.acau_hardcore.registry.menu;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.menu.MailSendMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
    public static final MenuType<MailSendMenu> MAIL_SEND_MENU = new MenuType<>(MailSendMenu::new, FeatureFlags.VANILLA_SET);

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "mail_send_menu"), MAIL_SEND_MENU);
    }
}
