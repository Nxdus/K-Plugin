package org.nxdus.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.nxdus.core.paper.KCore;


import java.util.Collection;

public class PlayerChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return false;
        }

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        String rawMessage = String.join(" ", strings);

        if (command.getName().equalsIgnoreCase("chat-global") && strings.length > 0) {

            if (player.isOp()) {
                jsonObject.addProperty("message", format(player, rawMessage, "global"));
                KCore.redisManager.publish("k-chat", gson.toJson(jsonObject));
                return true;
            }

            getGlobalCooldowns(player);

            boolean isCooldown = KCore.redisManager.getKey("chat.global.isCooldown." + player.getUniqueId()) != null
                    && KCore.redisManager.getKey("chat.global.isCooldown." + player.getUniqueId()).equalsIgnoreCase("true");

            if (!isCooldown) {
                jsonObject.addProperty("message", format(player, rawMessage, "global"));
                KCore.redisManager.publish("k-chat", gson.toJson(jsonObject));

                KCore.redisManager.setKeyEx("chat.global.isCooldown." + player.getUniqueId(), "true", 60 * 60 * 5);
                KCore.redisManager.setKeyEx("chat.global.cooldown." + player.getUniqueId(), String.valueOf(System.currentTimeMillis()), 60 * 60 * 5);
            }
        } else if (command.getName().equalsIgnoreCase("chat-ads") && strings.length > 0) {

            if (player.isOp()) {
                jsonObject.addProperty("message", format(player, rawMessage, "ads"));
                KCore.redisManager.publish("k-chat", gson.toJson(jsonObject));
                return true;
            }

            getAdsCooldowns(player);

            boolean isCooldown = KCore.redisManager.getKey("chat.ads.isCooldown." + player.getUniqueId()) != null
                    && KCore.redisManager.getKey("chat.ads.isCooldown." + player.getUniqueId()).equalsIgnoreCase("true");

            if (!isCooldown) {
                jsonObject.addProperty("message", format(player, rawMessage, "ads"));
                KCore.redisManager.publish("k-chat", gson.toJson(jsonObject));

                KCore.redisManager.setKeyEx("chat.ads.isCooldown." + player.getUniqueId(), "true", 60 * 60 * 5);
                KCore.redisManager.setKeyEx("chat.ads.cooldown." + player.getUniqueId(), String.valueOf(System.currentTimeMillis()), 60 * 60 * 5);
            }
        }

        return true;
    }

    private void getGlobalCooldowns(Player player) {

        long GlobalCooldown = Long.parseLong(KCore.settings.getString("chat.global.cooldown"));
        long currentTime = System.currentTimeMillis();

        long PlayerCooldown = KCore.redisManager.getKey("chat.global.cooldown." + player.getUniqueId()) == null ? currentTime : Long.parseLong(KCore.redisManager.getKey("chat.global.cooldown." + player.getUniqueId()));

        long currentCooldown = (currentTime - PlayerCooldown) / 1000;

        if (currentCooldown == 0) return;

        if (!(currentCooldown > GlobalCooldown)) {
            String currentCooldownString = KCore.settings.getString("chat.global.cooldown-message");
            currentCooldownString = currentCooldownString.replaceAll("<cooldown>", (GlobalCooldown - currentCooldown) + "");

            player.sendMessage(currentCooldownString);

            return;
        }

        KCore.redisManager.setKeyEx("chat.global.isCooldown." + player.getUniqueId(), "false", 60 * 60);
    }

    private void getAdsCooldowns(Player player) {

        long AdsCooldown = Long.parseLong(KCore.settings.getString("chat.ads.cooldown"));
        long currentTime = System.currentTimeMillis();

        long PlayerCooldown = KCore.redisManager.getKey("chat.ads.cooldown." + player.getUniqueId()) == null ? currentTime : Long.parseLong(KCore.redisManager.getKey("chat.ads.cooldown." + player.getUniqueId()));

        long currentCooldown = (currentTime - PlayerCooldown) / 1000;

        if (currentCooldown == 0) return;

        if (!(currentCooldown > AdsCooldown)) {
            String currentCooldownString = KCore.settings.getString("chat.ads.cooldown-message");
            currentCooldownString = currentCooldownString.replaceAll("<cooldown>", (AdsCooldown - currentCooldown) + "");

            player.sendMessage(currentCooldownString);

            return;
        }

        KCore.redisManager.setKeyEx("chat.ads.isCooldown." + player.getUniqueId(), "false", 60 * 60 * 5);
    }


    private static String format(Player sender, String message, String chatType) {

        String isPlayerBE = FloodgateApi.getInstance().isFloodgatePlayer(sender.getUniqueId()) ? "%oraxen_be%" : "%oraxen_pc%";
        String prefixPlayer = PlaceholderAPI.setPlaceholders(sender, "%luckperms_prefix%");

        String formatChat = "";

        if (chatType.equalsIgnoreCase("global")) {
            formatChat = KCore.settings.getString("chat.global.format");
        } else if (chatType.equalsIgnoreCase("ads")) {
            formatChat = KCore.settings.getString("chat.ads.format");
        }

        formatChat = formatChat.replaceAll("<device_emoji>", PlaceholderAPI.setPlaceholders(sender, isPlayerBE));
        formatChat = formatChat.replaceAll("<prefix>", PlaceholderAPI.setPlaceholders(sender, prefixPlayer));
        formatChat = PlaceholderAPI.setPlaceholders(sender, formatChat);
        formatChat = formatChat.replaceAll("<message>", PlayerChatListener.formatEmoji(message, sender));

        if(sender.hasPermission("k-chat.color")) {
            formatChat = ChatColor.translateAlternateColorCodes('&', formatChat);
        }

        return formatChat;
    }

    public static void senderMessage(String message) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for (Player onlinePlayer : onlinePlayers) {
            onlinePlayer.sendMessage(message);
        }

    }
}
