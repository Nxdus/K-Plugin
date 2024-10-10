package org.nxdus.auth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp()) return;

        Location location = new Location(player.getWorld(), 777.777, -77.77777, 777.777);

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
        player.teleport(location);
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlySpeed(0);
        player.setWalkSpeed(0);
        new BukkitRunnable() {

            @Override
            public void run() {
                player.hidePlayer(Main.this, player);
            }
        }.runTaskLater(this, 20L);

        String[] messages = {
                "<yellow>Server is Connecting .</yellow>",
                "<yellow>Server is Connecting ..</yellow>",
                "<yellow>Server is Connecting ...</yellow>",
                "<yellow>Server is Connecting ....</yellow>"
        };

        // ใช้ scheduler เพื่อทำแอนิเมชัน
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                Component message = MiniMessage.miniMessage().deserialize(messages[index]);
                player.sendActionBar(message);
                index++;

                if (index >= messages.length) {
                    index = 0;
                }
            }
        }.runTaskTimer(this, 0L, 10L);

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("§eServer is Connecting . . .");
    }
}
