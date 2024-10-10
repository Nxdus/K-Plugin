package mc.CushyPro.KItemSkin.Menus;

import mc.CushyPro.KItemSkin.IconData;
import mc.CushyPro.KItemSkin.KItemSkinMain;
import mc.CushyPro.KItemSkin.StoreData;
import mc.CushyPro.KItemSkin.Used.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MenuSetting extends MenuModule {

    private final StoreData store;

    public MenuSetting(Player player) {
        super(KItemSkinMain.getInstance(), player);
        store = KItemSkinMain.getInstance().getStoreConfig();
    }

    @Override
    public void onExit() {
        super.onExit();
        KItemSkinMain.getInstance().getStoreConfig().saveConfig();
    }

    @MenuCreate(size = 3)
    public class MainMenu extends MenuModal {

        @Slot(LEFT_R2 + 2)
        public class setTypeOfMaterial extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.DIAMOND).setDisplay(ChatColor.AQUA + "Setting Type of Material").getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                open(1);
            }
        }

        @Slot(CENTER_R2)
        public class setItemSkin extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.DIAMOND_SWORD).setDisplay("set Item Skin").hideAttr().getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                open(2);
            }
        }

        @Slot(RIGHT_R2 - 2)
        public class setIconSkin extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.APPLE).setDisplay("set Icon Skin").hideAttr().getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                open(3);
            }
        }
    }

    public class backbutton extends ModalSlotAction {

        public int getExit() {
            return 0;
        }

        @Override
        public ItemStack getIcon() {
            ItemStack stack = new ItemStack(Material.DARK_OAK_DOOR);
            stack.editMeta(meta -> meta.setDisplayName(ChatColor.GREEN + "Back to MainMenu"));
            return stack;
        }

        @Override
        public void onClick(InventoryClickEvent e) {
            open(getExit());
        }
    }

    @MenuCreate(id = 1, size = 6, canceltype = CancelSlot.INMENU)
    public class MenuSetTypeOfMaterial extends MenuModal {

        @Slot(CENTER_R6)
        public class back extends backbutton {

        }

        @Override
        public void onClickBottom(InventoryClickEvent e) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
            }
        }

        @Slot(LEFT_R6)
        public class arrow extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.ARROW)
                        .setDisplay("Page: " + (dropdown + 1))
                        .addLore(ChatColor.YELLOW + "Left - Next")
                        .addLore(ChatColor.YELLOW + "Right - Back")
                        .getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    dropdown++;
                } else if (e.getClick() == ClickType.RIGHT) {
                    if (0 >= dropdown) {
                        return;
                    }
                    dropdown--;
                }
                update();
            }
        }

        int page = 0;
        int dropdown = 0;

        @Slot(value = 0, custom = "2-8:1-5")
        public class show extends ModalSlotAction {

            @Override
            public ItemStack getIcon() {
                int de = page + (getX() - 1);
                if (store.getTypes().size() > de) {
                    String type = store.getTypes().get(de);
                    List<String> list = store.getMaterialOf(type);
                    if (getY() == 0) {
                        return new ItemOrder(Material.ANVIL)
                                .setDisplay(ChatColor.YELLOW + type)
                                .addLore(ChatColor.RED + "Shift-Left for Delete")
                                .getItem();
                    }
                    if (getY() > 0) {
                        int mon = dropdown + (getY() - 1);
                        if (list.size() > mon) {
                            String t = list.get(mon);
                            try {
                                return new ItemOrder(Material.valueOf(t)).hideAttr().getItem();
                            } catch (Exception e) {
                                return new ItemOrder(Material.BARRIER).meta(meta -> meta.setDisplayName(ChatColor.YELLOW + "Error: " + t)).getItem();
                            }
                        }
                        return new ItemOrder(a(getX())).hideToolTip().getItem();
                    }
                }
                if (getY() == 0) {
                    return new ItemOrder(Material.EMERALD).meta(meta -> meta.setDisplayName(ChatColor.GREEN + "Click for Add")).getItem();
                }
                return new ItemOrder(Material.GLASS_PANE).hideToolTip().getItem();
            }

            public Material a(int a) {
                return switch (a) {
                    case 1 -> Material.BLACK_STAINED_GLASS_PANE;
                    case 2 -> Material.YELLOW_STAINED_GLASS_PANE;
                    case 3 -> Material.LIME_STAINED_GLASS_PANE;
                    case 4 -> Material.BLUE_STAINED_GLASS_PANE;
                    case 5 -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
                    case 6 -> Material.GRAY_STAINED_GLASS_PANE;
                    case 7 -> Material.WHITE_STAINED_GLASS_PANE;
                    case 8 -> Material.GREEN_STAINED_GLASS_PANE;
                    case 9 -> Material.PINK_STAINED_GLASS_PANE;
                    default -> Material.GLASS_PANE;
                };
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                int de = page + (getX() - 1);
                if (store.getTypes().size() > de) {
                    String type = store.getTypes().get(de);
                    if (getY() == 0) {
                        if (e.getClick() == ClickType.SHIFT_LEFT) {
                            open(10, menuModal -> {
                                if (menuModal instanceof MenuConfirm a) {
                                    a.setResult(result -> {
                                        if (result) {
                                            store.getTypes().remove(type);
                                            update();
                                        }
                                        open(1);
                                    });
                                }
                            });
                        }
                        return;
                    }
                    if (getY() > 0) {
                        List<String> list = store.getMaterialOf(type);
                        if (isAirItem(e.getCursor())) {
                            Material stack = e.getCurrentItem().getType();
                            if (!list.contains(stack.toString())) {
                                return;
                            }
                            list.remove(stack.toString());
                            e.getWhoClicked().setItemOnCursor(e.getCurrentItem().clone());
                        } else {
                            if (list.contains(e.getCursor().getType().toString())) {
                                return;
                            }
                            list.add(e.getCursor().getType().toString());
                            e.getCursor().setAmount(0);
                        }
                        update();
                    }
                } else {
                    if (getY() == 0) {
                        UseChatPlayer("Typing types in chat, typing cancel for return menu", s -> {
                            store.getTypes().add(s);
                            update();
                        });
                    }
                }
            }
        }


        @Slot(value = 0, custom = "1:1-5")
        public class backpage extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.PAPER).setDisplay(ChatColor.AQUA + "Back").getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (0 >= page) {
                    return;
                }
                page--;
                update();
            }
        }

        @Slot(value = 0, custom = "9:1-5")
        public class nextpage extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.PAPER).setDisplay(ChatColor.AQUA + "Next").getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                page++;
                update();
            }
        }

    }

    @MenuCreate(id = 2, size = 6, canceltype = CancelSlot.INMENU)
    public class MenuSetSkinItem extends MenuModal {

        List<String> list = new ArrayList<>();
        String type = null;
        int page = 0;

        @Override
        public void onOpen(MenuModule menu) {
            type = null;
            updatetype();
            super.onOpen(menu);
        }

        @Slot(CENTER_R6)
        public class back extends backbutton {

        }

        public void updatetype() {
            list = new ArrayList<>(store.getSkins());
            if (type != null) {
                list.removeIf(s -> !s.toUpperCase().contains(type.toUpperCase()));
            }
        }

        @Slot(LEFT_R6)
        public class arrow extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.ARROW)
                        .setDisplay("Page: " + (page + 1))
                        .addLore(ChatColor.YELLOW + "Left - Next")
                        .addLore(ChatColor.YELLOW + "Right - Back")
                        .getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    page++;
                } else if (e.getClick() == ClickType.RIGHT) {
                    if (0 >= page) {
                        return;
                    }
                    page--;
                }
                update();
            }
        }

        @Slot(LEFT_R6 + 1)
        public class setType extends ModalSlotAction {

            int selected = 0;

            public List<String> getTypes() {
                List<String> list = new ArrayList<>();
                list.add("All");
                list.addAll(store.getTypes());
                return list;
            }

            @Override
            public ItemStack getIcon() {
                List<String> lore = new ArrayList<>();
                for (int x = 0; x < getTypes().size(); x++) {
                    String v = getTypes().get(x);
                    if (selected == x) {
                        lore.add(ChatColor.GREEN + "- " + v);
                    } else {
                        lore.add(ChatColor.RED + "- " + v);
                    }
                }
                ItemStack stack = new ItemStack(Material.BOOK);
                stack.editMeta(meta -> {
                    meta.setDisplayName("Types");
                    meta.setLore(lore);
                });
                return stack;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    selected++;
                    if (selected >= getTypes().size()) {
                        selected = 0;
                    }
                } else if (e.getClick() == ClickType.RIGHT) {
                    selected--;
                    if (0 > selected) {
                        selected = getTypes().size() - 1;
                    }
                }
                type = getTypes().get(selected);
                if (type.equalsIgnoreCase("all")) {
                    type = null;
                }
                updatetype();
                update();
            }
        }

        @Slot(value = 0, custom = "0-44")
        public class show extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                int c = getCount() + (page * 45);
                if (list.size() > c) {
                    try {
                        String[] skins = list.get(c).split(",");
                        String type = skins[0];
                        int idmodel = Integer.parseInt(skins[1]);
                        Material m = Material.valueOf(store.getMaterialOf(type).getFirst());
                        return new ItemOrder(m).setCustomModel(idmodel).setDisplay(ChatColor.RED + "Model: " + idmodel).addLore(ChatColor.AQUA + "Type: " + type).getItem();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ItemOrder(Material.BARRIER).setDisplay(type + " cant load " + c).getItem();
                    }
                }
                return null;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (isAirItem(e.getCursor())) {
                    int c = getCount() + (page * 45);
                    if (c >= list.size()) {
                        return;
                    }
                    String a = list.get(c);
                    store.getSkins().remove(a);
                    String[] skins = a.split(",");
                    String type = skins[0];
                    int idmodel = Integer.parseInt(skins[1]);
                    Material m = Material.valueOf(store.getMaterialOf(type).getFirst());
                    e.getWhoClicked().setItemOnCursor(new ItemOrder(m).setCustomModel(idmodel).getItem());
                } else {
                    if (store.addSkin(e.getCursor().clone()) == null) {
                        return;
                    }
                    e.getCursor().setAmount(0);
                }
                updatetype();
                update();
            }
        }

        @Override
        public void onClickBottom(InventoryClickEvent e) {
            if (e.isShiftClick()) {
                if (store.addSkin(e.getCurrentItem().clone()) == null) {
                    return;
                }
                e.getCurrentItem().setAmount(0);
                updatetype();
                update();
            }
        }
    }

    @MenuCreate(id = 3, size = 6, canceltype = CancelSlot.INMENU)
    public class MenuSetIcon extends MenuModal {

        List<IconData> list = new ArrayList<>();
        int page = 0;
        String type = null;

        @Override
        public void onOpen(MenuModule menu) {
            updatetype();
            super.onOpen(menu);
        }

        public void updatetype() {
            list = new ArrayList<>(store.getIcons());
            if (type != null) {
                list.removeIf(s -> {
                    if (type.equalsIgnoreCase("empty")) {
                        return s.getType() != null;
                    }
                    if (s.getType() != null) {
                        return !s.getType().equalsIgnoreCase(type);
                    }
                    return true;
                });
            }
        }

        @Slot(CENTER_R6)
        public class back extends backbutton {

        }

        @Override
        public void onClickBottom(InventoryClickEvent e) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
            }
        }

        @Slot(LEFT_R6)
        public class arrow extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.ARROW)
                        .setDisplay("Page: " + (page + 1))
                        .addLore(ChatColor.YELLOW + "Left - Next")
                        .addLore(ChatColor.YELLOW + "Right - Back")
                        .getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    page++;
                } else if (e.getClick() == ClickType.RIGHT) {
                    if (0 >= page) {
                        return;
                    }
                    page--;
                }
                update();
            }
        }

        @Slot(LEFT_R6 + 1)
        public class setType extends ModalSlotAction {

            int selected = 0;

            public List<String> Types() {
                List<String> list = new ArrayList<>();
                list.add("All");
                list.add("Empty");
                list.addAll(store.getTypes());
                return list;
            }

            @Override
            public ItemStack getIcon() {
                List<String> lore = new ArrayList<>();
                for (int x = 0; x < Types().size(); x++) {
                    String v = Types().get(x);
                    if (selected == x) {
                        lore.add(ChatColor.GREEN + "- " + v);
                    } else {
                        lore.add(ChatColor.RED + "- " + v);
                    }
                }
                ItemStack stack = new ItemStack(Material.BOOK);
                stack.editMeta(meta -> {
                    meta.setDisplayName("Types");
                    meta.setLore(lore);
                });
                return stack;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    selected++;
                    if (selected >= Types().size()) {
                        selected = 0;
                    }
                } else if (e.getClick() == ClickType.RIGHT) {
                    selected--;
                    if (0 > selected) {
                        selected = Types().size() - 1;
                    }
                }
                type = Types().get(selected);
                if (type.equalsIgnoreCase("all")) {
                    type = null;
                }
                updatetype();
                update();
            }
        }

        @Slot(value = 0, custom = "18-26")
        public class glass extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.GLASS_PANE).hideToolTip().getItem();
            }
        }

        @Slot(value = 0, custom = "0-44")
        public class showicon extends ModalSlotAction {

            @Override
            public ItemStack getIcon() {
                if (getY() == 3) {
                    return null;
                }
                int c = (getX() - 1) + (page * 9);
                if (getY() == 1 || getY() == 2) {
                    return getIconItem(c, getY() == 1);
                }
                if (getY() == 4 || getY() == 5) {
                    return getIconItem(c + 9, getY() == 4);
                }
                return null;
            }

            public ItemStack getIconItem(int c, boolean iconshow) {
                if (list.size() > c) {
                    IconData ty = list.get(c);
                    if (iconshow) {
                        return ty.getStack();
                    }
                    if (ty.getType() != null) {
                        String ad = store.getMaterialOf(ty.getType()).getFirst();
                        return new ItemOrder(Material.valueOf(ad))
                                .setCustomModel(ty.getIdmodel())
                                .setDisplay(ChatColor.AQUA + "Click for setType")
                                .addLore(ChatColor.RED + "Type: " + ty.getType())
                                .addLore(ChatColor.YELLOW + "ID: " + ty.getIdmodel())
                                .getItem();
                    }
                    return new ItemOrder(Material.BARRIER).setDisplay(ChatColor.AQUA + "click for setType").getItem();
                }
                return null;
            }

            public void setIconItem(InventoryClickEvent e, int c, boolean iconshow) {
                if (list.size() > c) {
                    IconData ty = list.get(c);
                    if (iconshow) {
                        if (!isAirItem(e.getCursor())) {
                            ItemStack stack = ty.getStack().clone();
                            ty.setStack(e.getCursor().clone());
                            e.getWhoClicked().setItemOnCursor(stack);
                        } else if (e.getClick() == ClickType.MIDDLE) {
                            e.getWhoClicked().setItemOnCursor(ty.getStack().clone());
                        }
                        update();
                    } else {
                        if (isAirItem(e.getCursor())) {
                            open(9, menuModal -> {
                                if (menuModal instanceof MenuSelectSkin args) {
                                    args.setResult(3, s -> {
                                        String[] skin = s.split(",");
                                        ty.setType(skin[0]);
                                        ty.setIdmodel(Integer.parseInt(skin[1]));
                                        open(3);
                                    });
                                }
                            });
                        } else {
                            ItemStack cur = e.getCursor();
                            String idskin = store.getSkin(cur);
                            if (idskin == null) {
                                return;
                            }
                            String[] skin = idskin.split(",");
                            ty.setType(skin[0]);
                            ty.setIdmodel(Integer.parseInt(skin[1]));
                            update();
                        }
                    }
                } else {
                    if (!isAirItem(e.getCursor())) {
                        add(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        type = "empty";
                        updateThis();
                        update();
                    }
                }
            }

            public void add(ItemStack icon) {
                store.getIcons().add(new IconData(icon));
                type = "empty";
                updatetype();
                update();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (getY() == 3) {
                    if (!isAirItem(e.getCursor())) {
                        add(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                    }
                    return;
                }
                int c = (getX() - 1) + (page * 9);
                if (getY() == 1 || getY() == 2) {
                    setIconItem(e, c, getY() == 1);
                } else if (getY() == 4 || getY() == 5) {
                    setIconItem(e, c + 9, getY() == 4);
                }
            }
        }


    }

    @MenuCreate(id = 9, size = 6)
    public class MenuSelectSkin extends MenuModal {

        Consumer<String> args;
        int idmenu;

        List<String> list = new ArrayList<>();
        String type = null;
        int page = 0;

        public void setResult(int idmenu, Consumer<String> args) {
            this.idmenu = idmenu;
            this.args = args;
        }

        @Override
        public void onOpen(MenuModule menu) {
            type = null;
            updatetype();
            super.onOpen(menu);
        }

        @Slot(LEFT_R6)
        public class back extends backbutton {
            @Override
            public int getExit() {
                return idmenu;
            }
        }

        public void updatetype() {
            list = new ArrayList<>(store.getSkins());
            if (type != null) {
                list.removeIf(s -> !s.toUpperCase().contains(type.toUpperCase()));
            }
        }

        @Slot(LEFT_R6)
        public class arrow extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.ARROW)
                        .setDisplay("Page: " + (page + 1))
                        .addLore(ChatColor.YELLOW + "Left - Next")
                        .addLore(ChatColor.YELLOW + "Right - Back")
                        .getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    page++;
                } else if (e.getClick() == ClickType.RIGHT) {
                    if (0 >= page) {
                        return;
                    }
                    page--;
                }
                update();
            }
        }

        @Slot(LEFT_R6 + 1)
        public class setType extends ModalSlotAction {

            int selected = 0;

            @Override
            public ItemStack getIcon() {
                List<String> lore = new ArrayList<>();
                for (int x = 0; x < store.getTypes().size(); x++) {
                    String v = store.getTypes().get(x);
                    if (selected == x) {
                        lore.add(ChatColor.GREEN + "- " + v);
                    } else {
                        lore.add(ChatColor.RED + "- " + v);
                    }
                }
                ItemStack stack = new ItemStack(Material.BOOK);
                stack.editMeta(meta -> {
                    meta.setDisplayName("Types");
                    meta.setLore(lore);
                });
                return stack;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.LEFT) {
                    selected++;
                    if (selected >= store.getTypes().size()) {
                        selected = 0;
                    }
                } else if (e.getClick() == ClickType.RIGHT) {
                    selected--;
                    if (0 > selected) {
                        selected = store.getTypes().size() - 1;
                    }
                }
                type = store.getTypes().get(selected);
                updatetype();
                update();
            }
        }

        @Slot(value = 0, custom = "0-44")
        public class show extends ModalSlotAction {
            @Override
            public ItemStack getIcon() {
                int c = getCount() + (page * 45);
                if (list.size() > c) {
                    try {
                        String[] skins = list.get(c).split(",");
                        String type = skins[0];
                        int idmodel = Integer.parseInt(skins[1]);
                        Material m = Material.valueOf(store.getMaterialOf(type).getFirst());
                        return new ItemOrder(m).setCustomModel(idmodel).setDisplay(ChatColor.RED + "Model: " + idmodel).addLore(ChatColor.AQUA + "Type: " + type).getItem();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ItemOrder(Material.BARRIER).setDisplay(type + " cant load " + c).getItem();
                    }
                }
                return null;
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                int c = getCount() + (page * 45);
                if (c >= list.size()) {
                    return;
                }
                String ty = list.get(c);
                if (args != null) {
                    args.accept(ty);
                }
            }
        }

    }

    @MenuCreate(id = 10, size = 3)
    public class MenuConfirm extends MenuModal {

        Consumer<Boolean> args;

        public void setResult(Consumer<Boolean> args) {
            this.args = args;
        }

        @Slot(CENTER_R2 - 2)
        public class Confirm extends ModalSlotAction {

            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.EMERALD_BLOCK).setDisplay(ChatColor.GREEN + "Confirm").getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (args != null) {
                    args.accept(true);
                }
            }
        }

        @Slot(CENTER_R2 + 2)
        public class Cancel extends ModalSlotAction {

            @Override
            public ItemStack getIcon() {
                return new ItemOrder(Material.REDSTONE_BLOCK).setDisplay(ChatColor.RED + "Cancel").getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e) {
                if (args != null) {
                    args.accept(false);
                }
            }
        }
    }

}