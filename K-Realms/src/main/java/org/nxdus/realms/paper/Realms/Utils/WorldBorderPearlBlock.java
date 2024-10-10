package org.nxdus.realms.paper.Realms.Utils;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.nxdus.realms.paper.MainPaper;

public class WorldBorderPearlBlock implements Listener {

    public WorldBorderPearlBlock(MainPaper instance) {
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        double worldBorderSize = event.getPlayer().getWorld().getWorldBorder().getSize() / 2.0D;
        double centerX = event.getPlayer().getWorld().getWorldBorder().getCenter().getX();
        double centerZ = event.getPlayer().getWorld().getWorldBorder().getCenter().getZ();

        if (centerX + worldBorderSize < event.getTo().getX() || centerX - worldBorderSize > event.getTo().getX() || centerZ + worldBorderSize < event.getTo().getZ() || centerZ - worldBorderSize > event.getTo().getZ()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You can't ender pearl outside the border!");
            event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
        }
    }
}
