package org.nxdus.realms.paper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.TeleportAPI.KTeleportAPI;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.shared.managers.ConfigManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ForceLastLocation implements Listener {

    private final MainPaper instance;

    public ForceLastLocation(MainPaper instance) {
        this.instance = instance;

        RegisterHandler();
    }

    private void RegisterHandler() {
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ConfigManager.ConfigType serverType = KCore.configManager.getType();

        if (!(serverType == ConfigManager.ConfigType.SPAWN)) return;

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            try {
                PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("SELECT users.last_location FROM users WHERE uuid = ? LIMIT 1");
                preparedStatement.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String location = resultSet.getString("last_location");
                    String[] result = location.split(",");

                    String lastServer = result[0];
                    String worldName = result[1];
                    double coordsX = Double.parseDouble(result[2]);
                    double coordsY = Double.parseDouble(result[3]);
                    double coordsZ = Double.parseDouble(result[4]);
                    float yaw = Float.parseFloat(result[5]);
                    float pitch = Float.parseFloat(result[6]);

                    preparedStatement.close();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            KTeleportAPI.teleportPlayerToWorld(player,lastServer,worldName,coordsX,coordsY,coordsZ,yaw,pitch);
                        }
                    }.runTaskLater(instance, 20L);
                }

                PreparedStatement deleteStatement = KCore.databaseConnection.prepareStatement("UPDATE users SET last_location = NULL WHERE uuid = ?");
                deleteStatement.setString(1, player.getUniqueId().toString());
                deleteStatement.executeUpdate();
                deleteStatement.close();

            } catch (Exception ignored) {}
        }, 20L * 5);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        ConfigManager.ConfigType serverType = KCore.configManager.getType();

        if (!(serverType == ConfigManager.ConfigType.REALM)) return;

        String lastServer = KCore.configManager.getType().getValue();

        if (!lastServer.equals("realm-base")) {
            lastServer = lastServer + "-" + KCore.serverUUID;
        }

        World world = player.getWorld();
        Location location = player.getLocation();

        String result = lastServer + "," + world.getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();

        try {
            PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("UPDATE users SET last_location = ? WHERE uuid = ?");
            preparedStatement.setString(1, result);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        } catch (Exception ignored) {}

    }

}
