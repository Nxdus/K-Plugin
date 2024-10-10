package org.nxdus.realms.paper.Realms;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.TeleportAPI.KTeleportAPI;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.realms.paper.RealmMenu.RealmMemberMenu;
import org.nxdus.realms.paper.RealmMenu.RealmsMenu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RealmsCommand implements CommandExecutor, TabCompleter {

    private final MainPaper plugin;
    public static final Map<String, String> hasPlayerInvited = new HashMap<>();

    public RealmsCommand(MainPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        if (args.length == 0) {
            new RealmsMenu(player).openInventory(player);
            return true;
        }

        String subCommand = args[0];
        switch (subCommand.toLowerCase()) {
            case "create":
                handleCreateCommand(player, args);
                break;
            case "load":
                handleLoadCommand(player);
                break;
            case "reset":
                handleResetCommand(player, args);
                break;
            case "invite":
                handleInviteCommand(player, args);
                break;
            case "remove":
                handleRemoveCommand(player, args);
                break;
            case "accept":
                handleAcceptCommand(player);
                break;
            case "member":
                new RealmMemberMenu(player).openInventory(player);
                break;
            case "join":
                handleJoinCommand(player, args);
                break;
            default:
                player.sendMessage("Unknown command.");
                break;
        }
        return true;
    }

    private void handleJoinCommand(Player player, String[] args) {
        if (args.length >= 2) {
            String username = args[1];
            try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("SELECT * FROM realms WHERE user_id = " +
                    "(SELECT users.id FROM users WHERE username = ? LIMIT 1) LIMIT 1")) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String serverID = resultSet.getString("server_id");
                    String worldId = resultSet.getString("world_id");

                    PreparedStatement getWorldName = KCore.databaseConnection.prepareStatement("SELECT name FROM realm_slime_world WHERE id = ? LIMIT 1");
                    getWorldName.setString(1, worldId);
                    ResultSet worldNameResultSet = getWorldName.executeQuery();

                    String worldName = worldNameResultSet.next() ? worldNameResultSet.getString("name") : null;

                    if (serverID != null) {
                        player.sendMessage(serverID);
                    } else {
                        String serverConnecting = getConnectServer();
                        KTeleportAPI.teleportPlayerToServer(player, serverConnecting);

                        System.out.println(serverConnecting);

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("action", "load-realm");
                        jsonObject.addProperty("player-uuid", player.getUniqueId().toString());
                        jsonObject.addProperty("world-name", worldName);

                        KCore.redisManager.publish(serverConnecting, new Gson().toJson(jsonObject));
                    }

                }
            } catch (SQLException e) { e.printStackTrace(); }

        }
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (hasWorldAlready(player)) {
            player.sendMessage("You already have a world!");
        } else if (args.length >= 2) {
            String biome = args[1]; // arid, darkness, lush, snowy, sweet, tropical
            String serverConnecting = getConnectServer();
            KTeleportAPI.teleportPlayerToServer(player, serverConnecting);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "create-realm");
            jsonObject.addProperty("player-uuid", player.getUniqueId().toString());
            jsonObject.addProperty("world-biome", biome);

            KCore.redisManager.publish(serverConnecting, new Gson().toJson(jsonObject));
        }
    }

    private void handleLoadCommand(Player player) {
        if (hasWorldAlready(player)) {
            String serverConnecting = getConnectServer();
            String worldName = getWorldName(player);
            KTeleportAPI.teleportPlayerToServer(player, serverConnecting);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "load-realm");
            jsonObject.addProperty("player-uuid", player.getUniqueId().toString());
            jsonObject.addProperty("world-name", worldName);

            KCore.redisManager.publish(serverConnecting, new Gson().toJson(jsonObject));
        }
    }

    private void handleResetCommand(Player player, String[] args) {
        if (hasWorldAlready(player) && args.length >= 2) {
            String biome = args[1]; // arid, darkness, lush, snowy, sweet, tropical
            String serverConnecting = getConnectServer();
            String worldName = getWorldName(player);
            KTeleportAPI.teleportPlayerToServer(player, serverConnecting);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "reset-realm");
            jsonObject.addProperty("player-uuid", player.getUniqueId().toString());
            jsonObject.addProperty("world-biome", biome);
            jsonObject.addProperty("world-name", worldName);

            KCore.redisManager.publish(serverConnecting, new Gson().toJson(jsonObject));
        }
    }

    private void handleInviteCommand(Player player, String[] args) {
        if (hasWorldAlready(player) && args.length >= 2) {
            String invitedName = args[1];
            String worldName = getWorldName(player);

            try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(
                    "SELECT id FROM realm_member " +
                            "WHERE realm_id = (SELECT id FROM realms WHERE world_id = " +
                            "(SELECT id FROM realm_slime_world WHERE name = ? LIMIT 1) LIMIT 1) " +
                            "AND user_id = (SELECT id FROM users WHERE username = ? LIMIT 1) LIMIT 1")) {
                preparedStatement.setString(1, worldName);
                preparedStatement.setString(2, invitedName);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    player.sendMessage("Player is already in the realm!");
                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "realm-invited");
                    jsonObject.addProperty("player-invited-name", invitedName);
                    jsonObject.addProperty("realm-to-member", worldName);
                    KCore.redisManager.publish("velocity", new Gson().toJson(jsonObject));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRemoveCommand(Player player, String[] args) {
        if (hasWorldAlready(player) && args.length >= 2) {
            String removedName = args[1];
            String worldName = getWorldName(player);

            try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(
                    "DELETE FROM realm_member WHERE realm_id = " +
                            "(SELECT id FROM realms WHERE world_id = " +
                            "(SELECT id FROM realm_slime_world WHERE name = ? LIMIT 1) LIMIT 1) " +
                            "AND user_id = (SELECT id FROM users WHERE username = ? LIMIT 1)")) {
                preparedStatement.setString(1, worldName);
                preparedStatement.setString(2, removedName);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    player.sendMessage("Player removed from the realm.");

                    try (PreparedStatement preparedStatement2 = KCore.databaseConnection.prepareStatement(
                            "SELECT realm_servers.server_id FROM realm_servers WHERE id = " +
                                    "(SELECT server_id FROM realms WHERE world_id = " +
                                    "(SELECT realm_slime_world.id FROM realm_slime_world WHERE name = ? LIMIT 1) LIMIT 1) LIMIT 1")) {
                        preparedStatement2.setString(1, worldName);
                        ResultSet resultSet = preparedStatement2.executeQuery();
                        if (resultSet.next()) {
                            String serverId = resultSet.getString("server_id");
                            String serverConnecting = "realm-" + serverId;

                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("action", "update-realm-member");
                            jsonObject.addProperty("world-name", worldName);

                            KCore.redisManager.publish(serverConnecting, new Gson().toJson(jsonObject));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAcceptCommand(Player player) {
        if (hasPlayerInvited.containsKey(player.getName())) {
            try (PreparedStatement invitedUserID = KCore.databaseConnection.prepareStatement(
                    "SELECT users.id FROM users WHERE username = ? LIMIT 1")) {
                invitedUserID.setString(1, player.getName());
                ResultSet invitedUserIDResultSet = invitedUserID.executeQuery();

                if (invitedUserIDResultSet.next()) {
                    int invitedUserIDInt = invitedUserIDResultSet.getInt("id");
                    try (PreparedStatement insertMemberIntoRealm = KCore.databaseConnection.prepareStatement(
                            "INSERT INTO realm_member (realm_id, user_id, role) " +
                                    "VALUES ((SELECT realms.id FROM realms WHERE world_id = " +
                                    "(SELECT realm_slime_world.id FROM realm_slime_world WHERE name = ? LIMIT 1) LIMIT 1), ?, 'trusted')")) {
                        insertMemberIntoRealm.setString(1, hasPlayerInvited.get(player.getName()));
                        insertMemberIntoRealm.setInt(2, invitedUserIDInt);
                        int rowsAffected = insertMemberIntoRealm.executeUpdate();

                        if (rowsAffected > 0) {
                            player.sendMessage("Accepted into the realm!");
                            updateRealmMember(player);
                            hasPlayerInvited.remove(player.getName());
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateRealmMember(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(
                "SELECT realm_servers.server_id FROM realm_servers WHERE id = " +
                        "(SELECT server_id FROM realms WHERE world_id = " +
                        "(SELECT realm_slime_world.id FROM realm_slime_world WHERE name = ? LIMIT 1) LIMIT 1) LIMIT 1")) {
            preparedStatement.setString(1, hasPlayerInvited.get(player.getName()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String serverId = resultSet.getString("server_id");
                String serverConnecting = "realm-" + serverId;
                String worldName = hasPlayerInvited.get(player.getName());

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "update-realm-member");
                jsonObject.addProperty("world-name", worldName);

                KCore.redisManager.publish(serverConnecting, new Gson().toJson(jsonObject));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        List<String> tabComplete = new ArrayList<>();

        if (args.length == 1) {
            tabComplete.addAll(Arrays.asList("create", "load", "reset", "invite", "accept", "remove", "member", "join"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("reset"))) {
            tabComplete.addAll(Arrays.asList("arid", "darkness", "lush", "snowy", "sweet", "tropical"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("join"))) {
            addPlayersToTabComplete(tabComplete);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            addTrustedPlayersToTabComplete(tabComplete, sender);
        }

        return tabComplete;
    }

    private void addPlayersToTabComplete(List<String> tabComplete) {
        KCore.playersOnline.forEach(playerUniqueID -> {
            try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("SELECT username FROM users WHERE uuid = ? LIMIT 1")) {
                preparedStatement.setString(1, playerUniqueID);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    tabComplete.add(resultSet.getString("username"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void addTrustedPlayersToTabComplete(List<String> tabComplete, CommandSender sender) {
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(
                "SELECT realm_member.user_id FROM realm_member WHERE realm_id = " +
                        "(SELECT id FROM realms WHERE realms.user_id = " +
                        "(SELECT id FROM users WHERE username = ? LIMIT 1) LIMIT 1) AND role = 'trusted'")) {
            preparedStatement.setString(1, sender.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                try (PreparedStatement preparedStatement1 = KCore.databaseConnection.prepareStatement(
                        "SELECT username FROM users WHERE id = ? LIMIT 1")) {
                    preparedStatement1.setInt(1, resultSet.getInt("user_id"));
                    ResultSet resultSet1 = preparedStatement1.executeQuery();
                    if (resultSet1.next()) {
                        tabComplete.add(resultSet1.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getWorldName(Player player) {
        String query = "SELECT rsw.name FROM realms r " +
                "JOIN realm_slime_world rsw ON r.world_id = rsw.id " +
                "JOIN users u ON r.user_id = u.id " +
                "WHERE u.uuid = ? LIMIT 1";

        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean hasWorldAlready(Player player) {
        String query = "SELECT r.* FROM realms r JOIN users u ON r.user_id = u.id WHERE u.uuid = ? LIMIT 1";

        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getConnectServer() {
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(
                "SELECT * FROM realm_servers WHERE player <= 20 ORDER BY current_memory_usage ASC, current_cpu_usage ASC, tps DESC LIMIT 1")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return "realm-" + resultSet.getString("server_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
