package org.nxdus.realms.paper.General;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.realms.paper.MainPaper;

public class SitPacketService implements Listener {

    private final MainPaper plugin;
    private final ProtocolManager protocolManager;

    public SitPacketService(MainPaper plugin, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;

        registerPacketListener();
    }



    private void registerPacketListener() {
        protocolManager.addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                handlePlayerSit(player);
            }
        });
    }

    private void handlePlayerSit(Player player) {
        if (player.getVehicle() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getVehicle() != null) {
                        // Adjust the player's Y position by -0.2
                        // plugin.getServer().broadcastMessage("§ePlayer " + player.getName() + " sits up!");
                        player.setMetadata("sitHeightAdjusted", new FixedMetadataValue(plugin, true));
                        // player.teleport(player.getLocation().add(0, 1, 0));
                    }
                }
            }.runTaskLater(this.plugin, 1L); // Delay by 1 tick to ensure the player is already sitting
        }
    }

//    private void handlePlayerSit(Player player) {
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                // Check if the player is sitting on an entity (like an ArmorStand)
//                if (player.getVehicle() != null) {
//                    // Adjust the player's Y position by -0.2
//                    double currentY = player.getLocation().getY();
//                    player.teleport(player.getLocation().add(0, -0.2, 0));
//                }
//            }
//        }.runTaskLater(this.plugin, 1L); // Delay by 1 tick to ensure the player is already sitting
//    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        plugin.getServer().broadcastMessage("§a[EventListenser] §ePlayer " + player.getName() + " sits up !!+");


        if (player.hasMetadata("sitHeightAdjusted")) {
            player.removeMetadata("sitHeightAdjusted", this.plugin);

            // event.setCancelled(true);

            plugin.getServer().broadcastMessage("§a[EventListenser] §ePlayer " + player.getName() + " sits up !!!");

            double currentY = player.getLocation().getY();
            // player.teleport(player.getLocation().add(0, 1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);

            // player.teleport(player.getLocation().add(0, 1, 0));
        }
    }
}
