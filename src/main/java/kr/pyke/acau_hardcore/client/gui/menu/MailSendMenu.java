package kr.pyke.acau_hardcore.client.gui.menu;

import kr.pyke.acau_hardcore.registry.menu.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class MailSendMenu extends AbstractContainerMenu {
    public final Container mailSlots;

    public MailSendMenu(int containerID, Inventory playerInventory) {
        this(containerID, playerInventory, new SimpleContainer(6));
    }

    public MailSendMenu(int containerID, Inventory playerInventory, Container mailSlots) {
        super(ModMenus.MAIL_SEND_MENU, containerID);
        this.mailSlots = mailSlots;

        checkContainerSize(mailSlots, 6);
        mailSlots.startOpen(playerInventory.player);

        int slotXStart = 8;
        int slotY = 138;

        for (int i = 0; i < 6; ++i) { this.addSlot(new Slot(mailSlots, i, slotXStart + (i * 18), slotY)); }

        int playerInvStartX = 8;
        int playerInvStartY = 161;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, playerInvStartX + col * 18, playerInvStartY + row * 18));
            }
        }

        int hotbarY = playerInvStartY + 58;

        for (int col = 0; col < 9; ++col) { this.addSlot(new Slot(playerInventory, col, playerInvStartX + col * 18, hotbarY)); }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack originStack = slot.getItem();
            itemStack = originStack.copy();

            if (index < 6) {
                if (!this.moveItemStackTo(originStack, 6, 41, true)) { return ItemStack.EMPTY; }
            }
            else {
                if (!this.moveItemStackTo(originStack, 0, 6, false)) { return ItemStack.EMPTY; }
            }

            if (originStack.isEmpty()) { slot.set(ItemStack.EMPTY); }
            else { slot.setChanged(); }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return this.mailSlots.stillValid(player);
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        this.clearContainer(player, this.mailSlots);
    }
}
