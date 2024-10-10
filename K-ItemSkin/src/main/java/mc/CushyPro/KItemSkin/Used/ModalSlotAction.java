package mc.CushyPro.KItemSkin.Used;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ModalSlotAction {

    protected MenuModule main;
    protected MenuModal sub;
    protected int x;
    protected int y;
    protected int slot;
    protected int count;

    public MenuModule getMain() {
        return this.main;
    }

    public MenuModal getSub() {
        return this.sub;
    }


    public int getSlot() {
        return this.slot;
    }

    public int getCount() {
        return this.count;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void updateThis() {
        this.sub.updateSlot(this.slot);
    }

    public abstract ItemStack getIcon();

    public void onClick(InventoryClickEvent e) {
    }

    public boolean disableKey() {
        return false;
    }
}


