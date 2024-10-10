package org.nxdus.realms.paper.General;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.realms.paper.MainPaper;

public class ShowArmArmorStandService implements Listener {

    private final MainPaper plugin;

    public ShowArmArmorStandService(MainPaper plugin) {
        this.plugin = plugin;

        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            if (event.getItem() != null && event.getItem().getType() == Material.ARMOR_STAND) {
                Player player = event.getPlayer(); // Get the player who placed the block

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getClickedBlock().getLocation().getWorld().getNearbyEntities(event.getClickedBlock().getLocation().add(0.5, 1, 0.5), 1, 1, 1).stream()
                                .filter(entity -> entity instanceof ArmorStand)
                                .findFirst()
                                .ifPresent(entity -> {
                                    ArmorStand armorStand = (ArmorStand) entity;
                                    armorStand.setArms(true);
                                });
                    }
                }.runTask(this.plugin);
            }
        }
    }
}
