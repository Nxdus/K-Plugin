package mc.CushyPro.KItemSkin.Used;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class MenuModule implements Listener {

    private final Plugin plugin;
    private final Player player;
    private final HashMap<Integer, MenuModal> maps;
    private MenuModal opened;
    private boolean tempo_close;
    Consumer<String> activechat;

    public MenuModule(Plugin plugin, Player player) {
        this(plugin, player, true);
    }

    public MenuModule(Plugin plugin, Player player, boolean register) {
        this.maps = new HashMap<>();
        this.tempo_close = false;
        this.player = player;
        this.plugin = plugin;
        if (register) {
            registerinclass();
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void onExit() {
        HandlerList.unregisterAll(this);
        exitmenu = true;
    }

    private boolean exitmenu = false;

    public boolean isExitmenu() {
        return exitmenu;
    }

    public Player getPlayer() {
        return this.player;
    }

    public <T> T getInstance() {
        return (T) this.plugin;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public void open() {
        if (this.opened == null && !this.maps.isEmpty()) if (this.maps.containsKey(0)) {
            this.opened = this.maps.get(0);
        } else {
            this.opened = (MenuModal) this.maps.values().toArray()[0];
        }
        if (this.opened == null) {
            onExit();
            throw new RuntimeException("not have open menu");
        }
        this.tempo_close = true;
        this.opened.onOpen(this);
        this.tempo_close = false;
    }

    public void open(int id) {
        setOpened(id);
        open();
    }

    public void open(int id, Consumer<MenuModal> args) {
        setOpened(id);
        args.accept(getOpened());
        open();
    }

    public void open(Class<?> cls) {
        if (cls.isAnnotationPresent(MenuCreate.class)) {
            MenuCreate sx = cls.getAnnotation(MenuCreate.class);
            open(sx.id());
        }
    }

    public void open(MenuModal menu) {
        setOpened(menu);
        open();
    }

    public void TemClose() {
        this.tempo_close = true;
        this.player.closeInventory();
        this.tempo_close = false;
    }

    public MenuModal getOpened() {
        return this.opened;
    }

    public boolean isNullopened() {
        return (this.opened == null);
    }

    public void setOpened(int id) {
        if (this.maps.containsKey(id)) this.opened = this.maps.get(id);
    }

    public void setOpened(Class<?> cls) {
        if (cls.isAnnotationPresent(MenuCreate.class)) {
            MenuCreate sx = cls.getAnnotation(MenuCreate.class);
            setOpened(sx.id());
        }
    }

    public void setOpened(MenuModal menu) {
        this.opened = menu;
    }

    public MenuModal getMenu(int id) {
        if (this.maps.containsKey(id)) return this.maps.get(id);
        return null;
    }

    public MenuModal getMenu(Class<?> cls) {
        if (cls.isAnnotationPresent(MenuCreate.class)) {
            MenuCreate sx = cls.getAnnotation(MenuCreate.class);
            return getMenu(sx.id());
        }
        return null;
    }

    public void registermenu(int id, MenuModal menu, int size, CancelSlot canceltype) {
        try {
            menu.mainmenu = this;
            menu.registerinclass();
            menu.Create(false, size, canceltype);
            this.maps.put(id, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registermenu(int id, Class<?> cls, int size, CancelSlot canceltype) {
        try {
            Object obj = cls.getConstructor(getClass()).newInstance(this);
            if (obj instanceof MenuModal menu) {
                registermenu(id, menu, size, canceltype);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerinclass() {
        try {
            for (Class<?> cls : getClass().getClasses()) {
                MenuCreate maker = cls.getAnnotation(MenuCreate.class);
                if (maker != null) {
                    registermenu(maker.id(), cls, maker.size(), maker.canceltype());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void UseChatPlayer(String msg) {
        this.tempo_close = true;
        this.player.closeInventory();
        this.tempo_close = false;
        this.player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public void UseChatPlayer(String msg, Consumer<String> onchat) {
        UseChatPlayerReturn((new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', msg))).create(), onchat);
    }

    public void UseChatPlayerReturn(BaseComponent[] base, Consumer<String> onchat) {
        this.tempo_close = true;
        this.player.closeInventory();
        this.tempo_close = false;
        if (base != null) {
            this.player.spigot().sendMessage(base);
        }
        this.activechat = onchat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (this.player.equals(player)) {
            e.setCancelled(true);
            try {
                if (this.opened != null) {
                    String msg = e.getMessage();
                    if (!msg.equals("cancel")) {
                        this.opened.onChatMessage(msg);
                        if (this.activechat != null) {
                            this.activechat.accept(msg);
                        }
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            new BukkitRunnable() {
                public void run() {
                    open();
                }
            }.runTask(this.plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpen(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        if (this.player.equals(player)) {
            if (e.getInventory().getType() == InventoryType.CHEST) {
                if (!this.opened.inv.equals(e.getInventory())) {
                    onExit();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            e.getInventory().close();
                        }
                    }.runTask(this.plugin);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (this.player == null) {
            onExit();
            return;
        }
        if (this.player.equals(player)) {
            if (e.getInventory().getType() == InventoryType.CHEST) {
                boolean lonster = this.opened.inv.equals(e.getInventory());
                if (!lonster) {
                    return;
                }
                this.opened.onClick(e);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (this.player.equals(player)) {
            this.opened.onDrag(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (this.player.equals(player)) {
            if (this.opened != null) {
                this.opened.onClose(e);
            }
            if (this.tempo_close) {
                this.tempo_close = false;
                return;
            }
            onExit();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (this.player.equals(player)) {
            this.opened.onQuit(e);
            onExit();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpen(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (this.player.equals(player)) {
            e.setCancelled(true);
            open();
        }
    }

    public boolean isAirItem(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    public void sendMessage(String... args) {
        getPlayer().sendMessage(args);
    }


}