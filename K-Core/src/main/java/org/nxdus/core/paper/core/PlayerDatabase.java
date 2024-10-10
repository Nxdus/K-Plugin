package org.nxdus.core.paper.core;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.shared.managers.ConfigManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class PlayerDatabase implements Listener {

    private final KCore plugin;

    public PlayerDatabase(KCore plugin) {
        this.plugin = plugin;
        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try (PreparedStatement statementSelectUser = KCore.databaseConnection.prepareStatement("SELECT * FROM users WHERE uuid = ? LIMIT 1")) {
            statementSelectUser.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statementSelectUser.executeQuery();
            if (KCore.configManager.getType() == ConfigManager.ConfigType.PRE_AUTH) {
                System.out.println("Debug 1");
                preAuth(player, resultSet);
            } else {
                general(player, resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void general(Player player, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            //
        } else {
            // ?? เข้าเชิฟไม่ผ่าน Pre-Auth ได้ไงงง
            player.kickPlayer(ChatColor.RED + "Something went wrong");
        }
    }

    private void preAuth(Player player, ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            System.out.println("Debug 2");
            if (resultSet.getString("uuid").equals(player.getUniqueId().toString())) {
                System.out.println("Debug 3");
                // & if username has changing
                if (!resultSet.getString("username").equals(player.getName())) {
                    // Insert log history change-name on `users_history_changename`
                    PreparedStatement statementInsertHistoryChangeName = KCore.databaseConnection.prepareStatement("INSERT INTO users_history_changename (`user_id`,`old_name`, `new_name`) VALUES (?, ?, ?);");
                    statementInsertHistoryChangeName.setInt(1, resultSet.getInt("id"));
                    statementInsertHistoryChangeName.setString(2, resultSet.getString("username"));
                    statementInsertHistoryChangeName.setString(3, player.getName());
                    statementInsertHistoryChangeName.executeUpdate();

                    // Update Username on `users`
                    PreparedStatement statementUpdated = KCore.databaseConnection.prepareStatement("UPDATE users SET username = ? WHERE uuid = ?");
                    statementUpdated.setString(1, player.getName());
                    statementUpdated.setString(2, player.getUniqueId().toString());
                    statementUpdated.executeUpdate();
                } else if (resultSet.getString("game_platform") == null) {
                    if (FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                        PreparedStatement statementFloodGate = KCore.databaseConnection.prepareStatement("UPDATE users SET game_platform = 'bedrock' WHERE uuid = ?");
                        statementFloodGate.setString(1, player.getUniqueId().toString());
                        statementFloodGate.executeUpdate();
                    } else {
                        PreparedStatement statementFloodGate = KCore.databaseConnection.prepareStatement("UPDATE users SET game_platform = 'java' WHERE uuid = ?");
                        statementFloodGate.setString(1, player.getUniqueId().toString());
                        statementFloodGate.executeUpdate();
                    }
                } else if (resultSet.getString("skin_url") == null) {
                    PreparedStatement statementSkin = KCore.databaseConnection.prepareStatement("UPDATE users SET skin_url = ? WHERE uuid = ?");

                    System.out.println(player.getPlayerProfile().getTextures().getSkin().toString());

                    statementSkin.setString(1, player.getPlayerProfile().getTextures().getSkin().toString());
                    statementSkin.setString(2, player.getUniqueId().toString());
                    statementSkin.executeUpdate();
                }

                // Update last_logged_at on `users`
                PreparedStatement statementUpdated = KCore.databaseConnection.prepareStatement("UPDATE users SET last_logged_at = NOW() WHERE uuid = ?");
                statementUpdated.setString(1, player.getUniqueId().toString());
                statementUpdated.executeUpdate();
            }

        } else {
            try {
                // Insert Into `users`
                PreparedStatement statementInsertUser = KCore.databaseConnection.prepareStatement("INSERT INTO users (uuid,username) VALUES (?,?)");
                statementInsertUser.setString(1, player.getUniqueId().toString());
                statementInsertUser.setString(2, player.getName());
                statementInsertUser.executeUpdate();
            }catch (Exception e) {
                if(e instanceof SQLIntegrityConstraintViolationException) {
                    player.kickPlayer(ChatColor.RED + "The username is already taken on another platform.");
                    return;
                }
                e.printStackTrace();
            }
        }
    }
}
