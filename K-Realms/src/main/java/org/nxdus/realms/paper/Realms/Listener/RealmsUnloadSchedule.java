package org.nxdus.realms.paper.Realms.Listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RealmsUnloadSchedule implements Listener {

    private final MainPaper instance;

    public RealmsUnloadSchedule(MainPaper instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
        scheduleWorldCheck();
    }

    // Method to schedule regular world checks
    public void scheduleWorldCheck() {
        Bukkit.getScheduler().runTaskTimer(instance, () -> {
            // Loop through each world on the server
            Bukkit.getWorlds().forEach(this::processWorld);
        }, 0L, 20L * 60L * 5L); // Run every 5 minutes
    }

    // Process each world by updating server entries and timestamps
    private void processWorld(World world) {
        int playerCount = world.getPlayers().size();

        // Clean up server entries older than 1 hour and unload the world if necessary
        updateServerEntries(world);

        // Update timestamp for worlds with active players
        if (playerCount > 0) {
            updateWorldTimestamp(world.getName());
        }
    }

    // Update server entries older than 1 hour
    private void updateServerEntries(World world) {
        String sql = "UPDATE realm_servers SET server_id = NULL WHERE updated_at < NOW() - INTERVAL 5 MINUTE";
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(sql)) {
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                Bukkit.unloadWorld(world.getName(), true);
                Bukkit.getLogger().info("Unloaded world: " + world.getName());
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to update server entries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update the world timestamp in the database
    private void updateWorldTimestamp(String worldName) {
        String sql = "UPDATE realms SET updated_at = NOW() WHERE world_id = (SELECT id FROM realm_slime_world WHERE name = ?)";
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(sql)) {
            preparedStatement.setString(1, worldName);
            preparedStatement.executeUpdate();
            Bukkit.getLogger().info("Updated timestamp for world: " + worldName);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to update timestamp for world " + worldName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to handle server shutdown and unload associated worlds
    public static void onServerShutdown() {
        UUID realmServerUUID = KCore.serverUUID;
        Connection dbConnection = KCore.databaseConnection;

        try {
            // Retrieve server ID from realm_servers
            String serverIdQuery = "SELECT rs.id FROM realm_servers rs WHERE rs.server_id = ? LIMIT 1";
            try (PreparedStatement preparedStatement = dbConnection.prepareStatement(serverIdQuery)) {
                preparedStatement.setString(1, realmServerUUID.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                
                while (resultSet.next()) {
                    int serverId = resultSet.getInt("id");
                    Bukkit.getLogger().info("Server Shutdown: Unloading worlds associated with server ID " + serverId);

                    // Unload associated worlds
                    unloadWorldsForServer(serverId, dbConnection);

                    // Nullify server_id in realms
                    nullifyServerIdForRealms(serverId, dbConnection);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed during server shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Unload worlds associated with the server ID
    private static void unloadWorldsForServer(int serverId, Connection dbConnection) throws SQLException {
        // Use `IN` instead of `=` to handle multiple rows returned by the subquery
        String unloadWorldQuery = "SELECT name FROM realm_slime_world WHERE id IN (SELECT realms.world_id FROM realms WHERE realms.server_id = ?)";
        try (PreparedStatement unloadWorldStatement = dbConnection.prepareStatement(unloadWorldQuery)) {
            unloadWorldStatement.setInt(1, serverId);
            ResultSet unloadWorldResultSet = unloadWorldStatement.executeQuery();

            while (unloadWorldResultSet.next()) {
                String worldName = unloadWorldResultSet.getString("name");
                Bukkit.unloadWorld(worldName, true); // Ensure that you catch any exceptions or handle errors appropriately
                Bukkit.getLogger().info("Unloading world: " + worldName);
            }
        }
    }

    // Nullify the server_id for the realms associated with the server
    private static void nullifyServerIdForRealms(int serverId, Connection dbConnection) throws SQLException {
        String updateRealmsQuery = "UPDATE realms SET server_id = NULL, updated_at = NOW() WHERE server_id = ?";
        try (PreparedStatement unloadStatement = dbConnection.prepareStatement(updateRealmsQuery)) {
            unloadStatement.setInt(1, serverId);
            int rowsUpdated = unloadStatement.executeUpdate();

            if (rowsUpdated > 0) {
                Bukkit.getLogger().info("Set server_id to NULL for Server ID " + serverId);
            }
        }
    }
}
