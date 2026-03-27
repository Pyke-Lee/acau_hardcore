package kr.pyke.acau_hardcore.client.key;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.screen.HelpRequestScreen;
import kr.pyke.acau_hardcore.client.gui.screen.MailBoxScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.lwjgl.glfw.GLFW;

public class AcauHardCoreKeyMapping {
    private AcauHardCoreKeyMapping() { }

    private static KeyMapping mailboxKey;
    private static KeyMapping helpRequestKey;

    private static void bind() {
        KeyMapping.Category customCategory = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "general")
        );

        mailboxKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.mailbox.open", GLFW.GLFW_KEY_P, customCategory));
        helpRequestKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.help_request.open", GLFW.GLFW_KEY_O, customCategory));
    }

    public static void register() {
        bind();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (mailboxKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(MailBoxScreen.create());
                }
            }

            while (helpRequestKey.consumeClick()) {
                if (client.player != null) {
                    if (client.player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2)))) {
                        client.setScreen(HelpRequestScreen.create());
                    }
                }
            }
        });
    }
}
