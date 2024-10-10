package org.nxdus.PlayerPin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nxdus.core.paper.KCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerPINService implements Listener {

    private final KPlayerPin plugin;

    public static HashMap<UUID, String> playersPIN = new HashMap<>();

    private static final List<UUID> requestOpenPinMenu = new ArrayList<>();

    public PlayerPINService(KPlayerPin plugin) {
        this.plugin = plugin;
        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerJoin(PlayerJoinEvent event) {
        // Open Menu ?

//        if (requestOpenPinMenu.contains(event.getPlayer().getUniqueId())) {
//            CreatePinMenu inventory = new CreatePinMenu();
//            plugin.getServer().getPluginManager().registerEvents(new PlayerPinMenuListener(plugin, inventory.getInventory(), event.getPlayer(), playersPIN.get(event.getPlayer().getUniqueId())), plugin);
//
//            requestOpenPinMenu.remove(event.getPlayer().getUniqueId());
//        }
    }

    public static void receiveMessage(JsonObject jsonMessage) {
        String subAction = jsonMessage.has("sub-action") ? jsonMessage.get("sub-action").getAsString() : "";
        String playerUsername = jsonMessage.has("player") ? jsonMessage.get("player").getAsString() : "";

        Player player = Bukkit.getPlayer(playerUsername);

        if (subAction.equals("get-player-pin")) {
            String playerPIN = jsonMessage.has("player-pin") ? jsonMessage.get("player-pin").getAsString() : "";
            boolean playerSession = jsonMessage.has("is-first-login") && jsonMessage.get("is-first-login").getAsBoolean();

            if (!playersPIN.containsKey(player.getUniqueId())) playersPIN.put(player.getUniqueId(), playerPIN);

            if (!playerSession) requestOpenPinMenu.add(player.getUniqueId());

        } else if (subAction.equals("create-player-pin")) {
            requestOpenPinMenu.add(player.getUniqueId());
        }
    }

    private static final Gson gson = new Gson();

    public static void updatePlayerPIN(String playerUUID, String playerPIN) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "update-player-pin");
        jsonObject.addProperty("player-uuid", playerUUID);
        jsonObject.addProperty("player-pin", playerPIN);

        KCore.redisManager.publish("velocity", gson.toJson(jsonObject));
    }

    public static void updatePlayerSession(String playerUUID) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "update-player-session");
        jsonObject.addProperty("player-uuid", playerUUID);

        KCore.redisManager.publish("velocity", gson.toJson(jsonObject));
    }
}
