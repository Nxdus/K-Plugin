package org.nxdus.core.paper.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.paper.subscribe.PaperSubscribe;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.core.shared.managers.DatabaseManager;
import org.nxdus.core.shared.managers.RedisManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class BootstrapPaper implements CommandExecutor {

    private static ConfigManager configManager;
    private static RedisManager redisManager;

    public static DatabaseManager databaseManager;
    public static Connection databaseConnection;

    private final KCore plugin;

    public BootstrapPaper(KCore plugin) {
        this.plugin = plugin;

        try {
            connectToRedis();
        } catch (IOException | URISyntaxException e) {
            plugin.getLogger().warning(e.getMessage());
        }
        try {
            connectToDatabase();
        } catch (SQLException e) {
            plugin.getLogger().warning(e.getMessage());
        }

        registerHandler();
    }

    private void registerHandler() {
        plugin.getCommand("a-redis").setExecutor(this);
    }

    private void connectToRedis() throws IOException, URISyntaxException {
        plugin.getLogger().info("Connecting to Redis");

        configManager = new ConfigManager();
        if (configManager.getConfigAsBoolean("redis.enable")) {
            redisManager = new RedisManager(configManager);
            plugin.getLogger().info("[K-Realms] Redis has enabled!");
            new PaperSubscribe(redisManager, configManager);
        }
    }

    private void connectToDatabase() throws SQLException {
        databaseManager = new DatabaseManager(configManager);
        databaseConnection = databaseManager.getConnection(); // ใช้ getConnection ที่มีการตรวจสอบการเชื่อมต่อ
        plugin.getLogger().info("Successfully connected to the database!");
    }

    public void disconnectFromDatabase() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
            plugin.getLogger().info("Successfully disconnected from the database!");
        }
    }

    public void disconnectFromRedis() {
//        if (configManager.getType() != null && configManager.getType() == ConfigManager.ConfigType.REALM) {
//            String json = PaperSubscribe.buildJson("delete-server-realm", KCore.serverUUID);
//            redisManager.publish("velocity", json);
//            plugin.getLogger().info("[K-Realms] Send Velocity w/ " + json);
//        }
    }


    public static ConfigManager.ConfigType getConfigType() {
        return configManager.getType();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (strings.length < 1) {
            commandSender.sendMessage("§cUsage: /a-redis list-sub");
            commandSender.sendMessage("§cUsage: /a-redis unsub <channel>");
            commandSender.sendMessage("§cUsage: /a-redis publish <channel> <message>");
            return false;
        }

        String method = strings[0];

        if (method.equals("list-sub")) {
            String ListChannel = String.join(", ", KCore.redisManager.pubSubList.keySet());
            commandSender.sendMessage("§aList Subscribed Channels: §e" + ListChannel);

            return false;
        }



        if (method.equals("publish")) {
            if (strings.length < 3) {
                commandSender.sendMessage("§cUsage: /a-redis publish <channel> <message>");
                return false;
            }
            String channel = strings[1];
            String message = strings[2];
            if (!channel.isEmpty() || !message.isEmpty()) {
                KCore.redisManager.publish(channel, message);
                commandSender.sendMessage("§aSuccessfully published channel " + channel + " to message " + message);
                return true;
            }
            commandSender.sendMessage("§cUsage: /a-redis publish <channel> <message>");
            return false;
        }

        if (method.equals("unsub")) {
            if (strings.length < 2) {
                commandSender.sendMessage("§cUsage: /a-redis unsub <channel>");
                return false;
            }
            String channel = strings[1];
            if (!channel.isEmpty()) {
                if (KCore.redisManager.pubSubList.containsKey(channel)) {
                    KCore.redisManager.unsubscribe(channel);
                    commandSender.sendMessage("§aSuccessfully unsubscribed channel " + channel);
                    return true;
                }
                commandSender.sendMessage("§cUsage: /a-redis unsub <channel>");
                return true;
            }
        }

        return false;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static RedisManager getRedisManager() {
        return redisManager;
    }

}
