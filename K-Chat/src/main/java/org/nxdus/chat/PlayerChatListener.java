package org.nxdus.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.geysermc.floodgate.api.FloodgateApi;
import org.nxdus.core.paper.KCore;

import java.util.HashMap;
import java.util.IllegalFormatFlagsException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerChatListener implements Listener {

    HashMap<UUID, Integer> getMessageStack = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();

        if (sender.isOp()) {
            formattedChat(event, sender.getUniqueId(), sender);
            return;
        }

        getDefaultCooldown(sender.getUniqueId(), sender);

        boolean isCooldown = KCore.redisManager.getKey("chat.default.isCooldown." + sender.getUniqueId()) != null && KCore.redisManager.getKey("chat.default.isCooldown." + sender.getUniqueId()).equalsIgnoreCase("true");

        if (!isCooldown) {
            setMaxLimited(sender);
            formattedChat(event, sender.getUniqueId(), sender);
        } else {
            event.setCancelled(true);
        }

    }

    private void setMaxLimited(Player sender) {

        if (getMessageStack.getOrDefault(sender.getUniqueId(), 0) == 4) {
            KCore.redisManager.setKeyEx("chat.default.isCooldown." + sender.getUniqueId(), "true", 60 * 3);
            KCore.redisManager.setKeyEx("chat.default.cooldown." + sender.getUniqueId(), String.valueOf(System.currentTimeMillis()), 60 * 3);
        }

        getMessageStack.put(sender.getUniqueId(), getMessageStack.getOrDefault(sender.getUniqueId(), 0) + 1);
    }

    private void getDefaultCooldown(UUID senderUUID, Player player) {

        long DefaultCooldown = Long.parseLong(KCore.settings.getString("chat.default.cooldown")) * 1000;
        long currentTime = System.currentTimeMillis();

        long PlayerCooldown = KCore.redisManager.getKey("chat.default.cooldown." +senderUUID) == null ? currentTime
                : Long.parseLong(KCore.redisManager.getKey("chat.default.cooldown." + senderUUID));

        long currentCooldown = (currentTime - PlayerCooldown);

        if (currentCooldown == 0) return;

        if (!(currentCooldown > DefaultCooldown)) {
            String currentCooldownString = KCore.settings.getString("chat.default.cooldown-message");
            currentCooldownString = currentCooldownString.replaceAll("<cooldown>", ((DefaultCooldown - currentCooldown) / 1000) + "");

            player.sendMessage(currentCooldownString);

            return;
        }

        boolean isCooldown = KCore.redisManager.getKey("chat.default.isCooldown." + player.getUniqueId()) != null && KCore.redisManager.getKey("chat.default.isCooldown." + player.getUniqueId()).equalsIgnoreCase("true");

        if (getMessageStack.getOrDefault(player.getUniqueId(), 0) > 4 && isCooldown) getMessageStack.remove(player.getUniqueId());

        KCore.redisManager.setKeyEx("chat.default.isCooldown." + player.getUniqueId(), "false", 60 * 3);
    }

    public static String formatEmoji(String message, Player sender) {
        Pattern pattern = Pattern.compile(":(\\w+):");
        Matcher matcher = pattern.matcher(message);

        StringBuilder formatChat = new StringBuilder(message);

        while (matcher.find()) {
            String emojiName = matcher.group(1);
            String placeholder = "%oraxen_" + emojiName + "%";

            int start = matcher.start();
            int end = matcher.end();
            formatChat.replace(start, end, placeholder);

            String parsedPlaceholder = PlaceholderAPI.setPlaceholders(sender, placeholder);
            formatChat.replace(start, start + placeholder.length(), parsedPlaceholder);

            matcher = pattern.matcher(formatChat);
        }

        return formatChat.toString();
    }

    private void formattedChat(AsyncPlayerChatEvent event,UUID senderUUID, Player sender) {

        String isPlayerBE;
        String prefixPlayer;

        try {
            isPlayerBE = FloodgateApi.getInstance().isFloodgatePlayer(senderUUID) ? "%oraxen_be%" : "%oraxen_pc%";
            prefixPlayer = PlaceholderAPI.setPlaceholders(sender, "%luckperms_prefix%");

            String rawMessage = event.getMessage();

            String formatChat = "<device_emoji> <prefix> %player_name% &r:%luckperms_suffix% %2$s";

            formatChat = formatChat.replaceAll("<device_emoji>", PlaceholderAPI.setPlaceholders(sender, isPlayerBE));
            formatChat = formatChat.replaceAll("<prefix>", PlaceholderAPI.setPlaceholders(sender, prefixPlayer));
            formatChat = PlaceholderAPI.setPlaceholders(sender, formatChat);

            if(sender.hasPermission("k-chat.color")) {
                formatChat = ChatColor.translateAlternateColorCodes('&', formatChat);
            }

            String formattedMessage = formatChat;

            event.setFormat(formattedMessage);
            event.setMessage(formatEmoji(rawMessage, sender));

            String senderWorld = sender.getWorld().getName();
            event.getRecipients().removeIf(r -> !r.getWorld().getName().equals(senderWorld));
        } catch (IllegalFormatFlagsException ignored) {}
    }

}
