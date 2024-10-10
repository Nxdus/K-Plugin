package org.nxdus.mythiccrucible;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PreventPickItems implements Listener {

    private final MainPaper plugin;

    public PreventPickItems(MainPaper plugin) {
        this.plugin = plugin;
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();
            PersistentDataContainer container = armorStand.getPersistentDataContainer();

            Player player = event.getPlayer();

            for (String key : FurnitureKeys.LIST) {
                NamespacedKey namespacedKey = new NamespacedKey(MainPaper.mythicCruciblePlugin, key);
                if (container.has(namespacedKey, PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    break;
                }
            }

        }
    }
}
