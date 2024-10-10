package org.nxdus.realms.paper.Realms.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.nxdus.TeleportAPI.KTeleportAPI;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.PreparedStatement;

public class inRealmsListener implements Listener {

    private final MainPaper instance;

    public inRealmsListener(MainPaper instance) {
        instance.getServer().getPluginManager().registerEvents(this, instance);
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerSpawned(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();

        String[] coords = KCore.settings.getString("realms.spawn.coordinate").split(" ");

        KTeleportAPI.teleportPlayerToWorld(player, "realm-spawn", coords[0], Double.parseDouble(coords[1]), Double.parseDouble(coords[2]), Double.parseDouble(coords[3]), Float.parseFloat(coords[4]), Float.parseFloat(coords[5]));

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            try {
                PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("UPDATE users SET last_location = null WHERE uuid = ?");
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20L * 3L);
    }

}
