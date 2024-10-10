package org.nxdus.core.paper.core;

import org.bukkit.ChatColor;
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

public class Translate {

    private final KCore plugin;
    private final Map<String, String> translateCache;

    private class CommandManager implements CommandExecutor {
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

            if (args.length == 0) {
                sender.sendMessage("§cUsage: /a-translate <set|get|remove|reload> [key] [value]");
                return false;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "set":
                    if (args.length < 3) {
                        sender.sendMessage("§cUsage: /a-translate set <key> <value>");
                        return false;
                    }
                    set(args[1], args[2]);
                    sender.sendMessage("§aTranslation set: " + args[1] + " -> " + args[2]);
                    return true;

                case "get":
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /a-translate get <key>");
                        return false;
                    }
                    String value = get(args[1]);
                    if (value != null) {
                        sender.sendMessage("§e" + args[1] + " => " + value);
                    } else {
                        sender.sendMessage("§cNo translation found for key: " + args[1]);
                    }
                    return true;

                case "format":
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /a-translate get <key>");
                        return false;
                    }
                    String valueFormat = format(args[1]);
                    sender.sendMessage("§e" + args[1] + " => " + valueFormat);
                    return true;

                case "remove":
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage: /a-translate remove <key>");
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

                    translateCache.forEach((k, v) -> {
                        sender.sendMessage(k + " => " + v);
                    });
                    return true;

                default:
                    sender.sendMessage("§cUnknown subcommand. Usage: /a-translate <set|get|remove|reload> [key] [value]");
                    return false;
            }

        }
    }

    public Translate(KCore _plugin) {
        plugin = _plugin;
        translateCache = new HashMap<>();

        registerHandler();
    }


    private void registerHandler() {
        plugin.getCommand("a-translate").setExecutor(new CommandManager());
    }
    

    public String format(String key) {
        return format(key, new Object[0]);
    }

    public String format(String key, Object... args) {
        String message = get(key);
        for (int i = 0; i < args.length; i += 2) {
            String placeholder = "<" + args[i].toString() + ">";
            String value = args[i + 1].toString();
            message = message.replace(placeholder, value);
        }

        if(message != null) {
            if (!message.startsWith("!")) {
                message = get("prefix") + message;
            } else {
                message = message.substring(1);
            }
        }else {
            plugin.getLogger().info(message);
        }
//        PlaceholderAPI.setPlaceholders()
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String prefix(String message) {
        return ChatColor.translateAlternateColorCodes('&',get("prefix") + message);
    }

    public void reload() {
        translateCache.clear();
        try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("SELECT `key`, `value` FROM `translates`");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                translateCache.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error reloading translations from database", e);
        }
    }

    public void set(String key, String value) {
        try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("INSERT INTO `translates` (`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)")) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.executeUpdate();
            translateCache.put(key, value);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting translation", e);
        }
    }

    public String get(String key) {
        String value = translateCache.get(key);
        if (value == null) {
            try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("SELECT `value` FROM `translates` WHERE `key` = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        value = rs.getString("value");
                        translateCache.put(key, value);
                    }else {
                        value = key;
                    }
                }
            } catch (SQLException e) {
                value = key;
            }
        }
        return value;
    }

    public boolean has(String key) {
        return translateCache.containsKey(key) || get(key) != null;
    }

    public void remove(String key) {
        try (PreparedStatement stmt = KCore.databaseConnection.prepareStatement("DELETE FROM `translates` WHERE `key` = ?")) {
            stmt.setString(1, key);
            stmt.executeUpdate();
            translateCache.remove(key);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing translation", e);
        }
    }

}
