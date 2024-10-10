package org.nxdus.realms.paper.General;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nxdus.realms.paper.MainPaper;

public class OnlyBuilderCanBuildService implements Listener {

    private final MainPaper plugin;

    public OnlyBuilderCanBuildService(MainPaper plugin) {
        this.plugin = plugin;

        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info(ChatColor.GREEN + "Block iTORz_ Service Loaded");
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("spawn-world")) {
            if (player.getName().equalsIgnoreCase("iTORz_") || player.getName().equalsIgnoreCase("iTORKUNGz")) {
                player.sendMessage(ChatColor.RED + "ซนไงจะตีมันทำไม มันวางไม่ได้ -.-");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("spawn-world")) {
            if (player.getName().equalsIgnoreCase("iTORz_") || player.getName().equalsIgnoreCase("iTORKUNGz")) {
                player.sendMessage(ChatColor.RED + "วางทำไมเดี๋ยวพัง !");
                event.setCancelled(true);
            }
        }
    }
}
