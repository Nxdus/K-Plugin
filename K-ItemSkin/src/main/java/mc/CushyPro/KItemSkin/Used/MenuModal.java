package mc.CushyPro.KItemSkin.Used;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MenuModal {

    public static final int CENTER_R1 = 4;
    public static final int LEFT_R1 = 0;
    public static final int RIGHT_R1 = 8;
    public static final int CENTER_R2 = 13;
    public static final int LEFT_R2 = 9;
    public static final int RIGHT_R2 = 17;
    public static final int CENTER_R3 = 22;
    public static final int LEFT_R3 = 18;
    public static final int RIGHT_R3 = 26;
    public static final int CENTER_R4 = 31;
    public static final int LEFT_R4 = 27;
    public static final int RIGHT_R4 = 35;
    public static final int CENTER_R5 = 40;
    public static final int LEFT_R5 = 36;
    public static final int RIGHT_R5 = 44;
    public static final int CENTER_R6 = 49;
    public static final int LEFT_R6 = 45;
    public static final int RIGHT_R6 = 53;

    MenuModule mainmenu;
    Inventory inv;
    private final HashMap<Integer, ModalSlotAction> maps = new HashMap<>();

    public MenuModule getMainmenu() {
        return this.mainmenu;
    }

    int size = 9;

    public void Create(boolean force, int size, CancelSlot canceltype) {
        this.size = size;
        this.canceltype = canceltype;
        if (this.inv == null || force)
            this.inv = Bukkit.createInventory(null, 9 * size, ChatColor.translateAlternateColorCodes('&', getTitle()));
    }

    public void registerslot(String slot, Class<?> cls) {
        String value = slot.replace(" ", "");
        if (value.contains(",")) {
            int st = 0;
            String[] ar = value.split(",");
            for (String vx : ar) {
                st = subman(cls, vx, st);
            }
            return;
        }
        subman(cls, value, 0);
    }

    public void registerinclass() {
        try {
            List<Class<?>> list = new ArrayList<>();
            for (Class<?> cls : getClass().getClasses()) {
                if (cls.isAnnotationPresent(Slot.class)) {
                    Slot modal = cls.getAnnotation(Slot.class);
                    if (modal != null) {
                        list.add(cls);
                    }
                }
            }
            try {
                list.sort(new Comparator<>() {
                    public int compare(Class<?> o1, Class<?> o2) {
                        int am1 = getPriority(o1);
                        int am2 = getPriority(o2);
                        return am1 - am2;
                    }

                    private int getPriority(Class<?> cs) {
                        if (cs.isAnnotationPresent(Slot.class)) {
                            return cs.getAnnotation(Slot.class).priority();
                        }
                        return 0;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Class<?> cls : list) {
                if (cls.isAnnotationPresent(Slot.class)) {
                    Slot modal = cls.getAnnotation(Slot.class);
                    if (modal.custom().isEmpty()) {
                        if ((modal.value()).length > 0) {
                            int r = 0;
                            for (int s : modal.value()) {
                                int[] ads = slottoXY(s);
                                registerKey(getConstructor(cls, s), s, r, ads[0], ads[1]);
                                r++;
                            }
                            continue;
                        }
                        subman(cls, modal.x(), modal.y(), modal.tx(), modal.ty(), 0);
                    } else {
                        registerslot(modal.custom(), cls);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return getClass().getSimpleName();
    }

    public Inventory getInventory() {
        return this.inv;
    }

    private int subman(Class<?> cls, int xa, int ya, int txa, int tya, int start) {
        int x = xa - 1;
        int y = ya - 1;
        if (x >= 0 && y >= 0) {
            int tx = txa;
            int ty = tya;
            if (tx == -1) {
                tx = x;
            } else {
                tx--;
            }
            if (ty == -1) {
                ty = y;
            } else {
                ty--;
            }
            for (int b = y; b <= ty; b++) {
                for (int a = x; a <= tx; a++) {
                    int s = b * 9 + a;
                    registerKey(getConstructor(cls, s), s, start, a, b);
                    start++;
                }
            }
        }
        return start;
    }

    private int subman(Class<?> cls, String value, int start) {
        try {
            if (value.contains(":")) {
                int lx = value.indexOf(":");
                String key_x = value.substring(0, lx);
                String key_y = value.substring(lx + 1);
                int x;
                int y;
                int tx;
                int ty;
                if (key_x.contains("-")) {
                    String[] zx = key_x.split("-");
                    x = Integer.parseInt(zx[0]);
                    tx = Integer.parseInt(zx[1]);
                } else {
                    x = Integer.parseInt(key_x);
                    tx = -1;
                }
                if (key_y.contains("-")) {
                    String[] zx = key_y.split("-");
                    y = Integer.parseInt(zx[0]);
                    ty = Integer.parseInt(zx[1]);
                } else {
                    y = Integer.parseInt(key_y);
                    ty = -1;
                }
                start = subman(cls, x, y, tx, ty, start);
            } else if (value.contains("-")) {
                int rx = value.indexOf("-");
                int num1 = Integer.parseInt(value.substring(0, rx));
                int num2 = Integer.parseInt(value.substring(rx + 1));
                int min = Math.min(num1, num2);
                int max = Math.max(num1, num2);
                for (int z = min; z <= max; z++) {
                    int[] ads = slottoXY(z);
                    registerKey(getConstructor(cls, z), z, start, ads[0], ads[1]);
                    start++;
                }
            } else {
                int s = Integer.parseInt(value);
                int[] ads = slottoXY(s);
                registerKey(getConstructor(cls, s), s, start, ads[0], ads[1]);
                start++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return start;
    }

    private int[] slottoXY(int slot) {
        if (slot >= 45) return new int[]{slot - 45 + 1, 6};
        if (slot >= 36) return new int[]{slot - 36 + 1, 5};
        if (slot >= 27) return new int[]{slot - 27 + 1, 4};
        if (slot >= 18) return new int[]{slot - 18 + 1, 3};
        if (slot >= 9) return new int[]{slot - 9 + 1, 2};
        return new int[]{slot + 1, 1};
    }

    private void registerKey(Object obj, int s, int r, int x, int y) {
        if (obj instanceof ModalSlotAction key) {
            key.slot = s;
            if (this.mainmenu != null) {
                key.main = getMainmenu();
            }
            key.sub = this;
            key.count = r;
            key.x = x;
            key.y = y;
            this.maps.put(s, key);
        }
    }

    public void setSlotActive(int slot, ModalSlotAction menu) {
        this.maps.put(slot, menu);
    }

    public void ClearSlotActive(int slot) {
        this.maps.remove(slot);
    }

    public Object getConstructor(Class<?> cls, int slot) {
        for (Constructor<?> cor : cls.getConstructors()) {
            try {
                if ((cor.getParameterTypes()).length == 2) {
                    String str = cor.getParameterTypes()[1].getTypeName();
                    if (str.equalsIgnoreCase("int") || str.contains("Integer")) {
                        return cor.newInstance(this, slot);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return cls.getConstructor(getClass()).newInstance(this);
        } catch (NoSuchMethodException e) {
            for (Constructor<?> cor : cls.getConstructors()) {
                try {
                    return cor.newInstance(this);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error Menu load Constructor " + cls.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update() {
        for (int slot : this.maps.keySet()) {
            try {
                updateSlot(slot);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: MenuAmoryLib " + getClass().getSimpleName() + " S[" + slot + "]");
                e.printStackTrace();
            }
        }

    }

    public void updateSlot(int slot) {
        if (this.maps.containsKey(slot)) {
            ModalSlotAction key = this.maps.get(slot);
            if (!key.disableKey()) {
                this.inv.setItem(slot, key.getIcon());
            }
        }
    }

    @Deprecated
    public <T> T getSlot(int slot) {
        return getSlotObj(slot);
    }

    public <T> T getSlotObj(int slot) {
        if (this.maps.containsKey(slot)) {
            return (T) this.maps.get(slot);
        }
        return null;
    }

    public void onOpen(MenuModule menu) {
        update();
        menu.getPlayer().openInventory(this.inv);
    }

    CancelSlot canceltype = CancelSlot.ALL;

    public void onClick(InventoryClickEvent e) {
        switch (canceltype) {
            case INMENU -> {
                if (e.getRawSlot() >= 0 && e.getRawSlot() < e.getInventory().getSize()) {
                    e.setCancelled(true);
                }
            }
            case ININVENTORY -> {
                if (e.getRawSlot() >= e.getInventory().getSize()) {
                    e.setCancelled(true);
                }
            }
            case ALL -> e.setCancelled(true);
        }
        if (this.maps.containsKey(e.getRawSlot())) {
            this.maps.get(e.getRawSlot()).onClick(e);
        }
        if (e.getRawSlot() >= e.getInventory().getSize()) {
            onClickBottom(e);
        }
    }

    public void onClickBottom(InventoryClickEvent e) {
    }

    public void onClose(InventoryCloseEvent e) {
    }

    public void onQuit(PlayerQuitEvent e) {
    }

    public void onDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    public void onChatMessage(String msg) {

    }

}
