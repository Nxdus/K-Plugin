package mc.CushyPro.KItemSkin.Menus;

import mc.CushyPro.KItemSkin.IconData;
import mc.CushyPro.KItemSkin.KItemSkinMain;
import mc.CushyPro.KItemSkin.StoreData;
import mc.CushyPro.KItemSkin.Used.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuSetSkin extends MenuModule {

    private final StoreData store;

    public MenuSetSkin(Player player) {
        super(KItemSkinMain.getInstance(), player);
        store = KItemSkinMain.getInstance().getStoreConfig();
    }

    @MenuCreate(size = 3)
    public class MainMenu extends MenuModal {

        @Slot(value = 0, custom = "0-26", priority = -1)
        public class showicon extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                if (getSlot() == 10 || getSlot() == 12 || getSlot() == 16 || getSlot() == 15) {
                    return null;
                }
                return new ItemOrder(Material.GLASS_PANE).hideToolTip().getItem();
            }
        }

        boolean toolitem = false;
        Integer slottool = null;
        Integer sloticon = null;
        IconData icondata;
        String skindata = null;

        public void resetTool() {
            slottool = null;
            skindata = null;
            toolitem = false;
        }

        public void resetIcon() {
            sloticon = null;
            icondata = null;
        }

        @Override
        public void onClickBottom(InventoryClickEvent e) {
            ItemStack stack = e.getCurrentItem();
            String skin = store.getSkin(stack);
            if (skin != null) {
                slottool = e.getSlot();
                toolitem = true;
                skindata = skin;
                update();
                return;
            }
            IconData asd = store.getIcon(stack);
            if (asd != null) {
                sloticon = e.getSlot();
                this.icondata = asd;
                update();
                return;
            }
            String type = store.getType(stack);
            if (type != null) {
                slottool = e.getSlot();
                this.toolitem = false;
                update();
            }

        }

        @Slot(LEFT_R2 + 1)
        public class itemtool extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                if (slottool != null) {
                    return getPlayer().getInventory().getItem(slottool);
                }
                return null;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                resetTool();
                update();
            }
        }

        @Slot(LEFT_R2 + 3)
        public class itemicon extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                if (sloticon != null) {
                    return getPlayer().getInventory().getItem(sloticon);
                }
                return null;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                resetIcon();
                update();
            }
        }

        @Slot(value = {RIGHT_R2 - 2, RIGHT_R2 - 1})
        public class itemskin extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                if (slottool == null) {
                    return null;
                }
                ItemStack tool = getPlayer().getInventory().getItem(slottool);
                if (isAirItem(tool)) {
                    return null;
                }
                ItemStack stack = tool.clone();
                if (toolitem) {
                    if (skindata == null) {
                        return null;
                    }
                    IconData data = store.getIcon(skindata);
                    if (data == null) {
                        return null;
                    }
                    if (getCount() == 0) {
                        stack.editMeta(meta -> meta.setCustomModelData(0));
                        return stack;
                    } else {
                        return data.getStack();
                    }
                }
                if (getCount() != 0) {
                    return null;
                }
                if (icondata == null) {
                    return null;
                }
                if (!store.setSkinItem(icondata, stack)) {
                    return null;
                }
                return stack;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (slottool == null) {
                    return;
                }
                ItemStack stack = getPlayer().getInventory().getItem(slottool);
                if (isAirItem(stack)) {
                    resetTool();
                    update();
                    return;
                }
                if (toolitem) {
                    if (skindata == null) {
                        resetTool();
                        update();
                        return;
                    }
                    IconData data = store.getIcon(skindata);
                    PInven pin = new PInven(getPlayer());
                    if (!pin.canFit(data.getStack())) {
                        sendMessage(ChatColor.RED + "Inventory is full");
                        return;
                    }
                    stack.editMeta(meta -> meta.setCustomModelData(0));
                    pin.AddItem(data.getStack());
                    resetTool();
                    update();
                    return;
                }
                if (getCount() != 0) {
                    return;
                }
                if (icondata == null) {
                    resetIcon();
                    update();
                    return;
                }
                ItemStack iconstack = getPlayer().getInventory().getItem(sloticon);
                if (isAirItem(iconstack) && 0 >= iconstack.getAmount()) {
                    resetIcon();
                    update();
                    return;
                }
                store.setSkinItem(icondata, stack);
                iconstack.setAmount(iconstack.getAmount() - 1);
                resetTool();
                resetIcon();
                update();
            }
        }
    }


}
