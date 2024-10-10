package org.nxdus.core.paper.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nxdus.core.paper.KCore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Settings {

    private final KCore plugin;
    private final Map<String, String> SettingCache;


    public Settings(KCore plugin) {
        this.plugin = plugin;
        SettingCache = new HashMap<>();

        registerHandler();
    }


    private void registerHandler() {
        plugin.getCommand("a-settings").setExecutor(new CommandManager());
    }
    
    private class CommandManager implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

            if (args.length == 0) {
                sender.sendMessage("§cUsage: /a-settings <set|get|remove|reload> [key] [value]");
                return false;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "set":
                    if (args.length < 3) {
                        sender.sendMessage("§cUsage: /a-settings set <key> <value>");
                        return false;
                    }
                    set(args[1], args[2]);
                    sender.sendMessage("§aTranslation set: " + args[1] + " -> " + args[2]);
                    return true;

                case "get":
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /a-settings get <key>");
                        return false;
                    }
                    String value = getString(args[1]);
                    if (value != null) {
                        sender.sendMessage("§e" + args[1] + " => " + value);
                    } else {
                        sender.sendMessage("§cNo translation found for key: " + args[1]);
                    }
                    return true;

                case "remove":
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /a-settings remove <key>");
                        return false;
                    }
                    remove(args[1]);
                    sender.sendMessage("§aTranslation removed: " + args[1]);
                    return true;


                case "reload":
                    reload();
                    sender.sendMessage("§aTranslations reloaded!");
                    return true;

                case "cache":

                    SettingCache.forEach((k, v) -> {
                        sender.sendMessage(k + " => " + v);
                    });
                    return true;

                default:
                    sender.sendMessage("§cUnknown subcommand. Usage: /a-settings <set|get|remove|reload> [key] [value]");
                    return false;
            }

        }
    }

    public String format(String key) {
        return format(key, new Object[0]);
    }

    public String format(String key, Object... args) {
        String message = getString(key);
        for (int i = 0; i < args.length; i += 2) {
            String placeholder = "<" + args[i].toString() + ">";
            String value = args[i + 1].toString();
            message = message.replace(placeholder, value);
        }

        return message;
    }

    public void reload() {
        SettingCache.clear();
        try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("SELECT `key`, `value` FROM `settings`");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SettingCache.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error reloading translations from database", e);
        }
    }

    public void set(String key, String value) {
        try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("INSERT INTO `settings` (`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)")) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
            SettingCache.put(key, value);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting translation", e);
        }
    }

    public String getString(String key) {
        String value = SettingCache.get(key);
        if (value == null) {
            try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("SELECT `value` FROM `settings` WHERE `key` = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        value = rs.getString("value");
                        SettingCache.put(key, value);
                    }
                }
            } catch (SQLException e) {
                // plugin.getLogger().log(Level.SEVERE, "Error getting translation", e);
                value = key;
            }
        }
        return value;
    }

    public boolean getBoolean(String key) {
        String value = SettingCache.get(key);

        if (value == null) {
            try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("SELECT `value` FROM `settings` WHERE `key` = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        value = rs.getString("value");
                        SettingCache.put(key, value);
                    }
                }
            } catch (SQLException e) {
                value = null;
            }
        }

        return Boolean.parseBoolean(value);
    }

    public Number getNumber(String key) {
        String value = SettingCache.get(key);

        if (value == null) {
            try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("SELECT `value` FROM `settings` WHERE `key` = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        value = rs.getString("value");
                        SettingCache.put(key, value);
                    }
                }
            } catch (SQLException e) {
                value = null;
            }
        }

        return Double.parseDouble(value);
    }

    public boolean has(String key) {
        return SettingCache.containsKey(key) || getString(key) != null;
    }

    public void remove(String key) {
        try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("DELETE FROM `settings` WHERE `key` = ?")) {
            stmt.setString(1, key);
            stmt.executeUpdate();
            SettingCache.remove(key);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing translation", e);
        }
    }

}
