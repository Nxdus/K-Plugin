package org.nxdus.Teleport.Teleporter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.Teleport.KTeleport;
import org.nxdus.core.paper.KCore;

import java.util.HashMap;

public class PlayerDelayService implements Listener {

    private static KTeleport plugin;
    private static final HashMap<String, Boolean> delayTeleport = new HashMap<>();

    public PlayerDelayService(KTeleport _plugin) {
        plugin = _plugin;
    }

    public static void receiveMessage(JsonObject jsonMessage) {
        String playerName = jsonMessage.has("player") ? jsonMessage.get("player").getAsString() : "";

        delayTeleport.put(playerName, true);
        sendDelayMessage(Bukkit.getPlayer(playerName));
    }

    private static void sendDelayMessage(Player player) {

        new BukkitRunnable() {

            int playerDelay = 5;

            @Override
            public void run() {
                player.sendTitle("Delay : " + playerDelay, "Teleporter", 10, 10, 10);

                playerDelay = playerDelay - 1;

                if (playerDelay == 0 && delayTeleport.get(player.getName()) != null) {
                    Gson gson = new Gson();
                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("action", "teleport-success");
                    jsonObject.addProperty("player", player.getName());

                    delayTeleport.remove(player.getName());
                    KCore.redisManager.publish("velocity", gson.toJson(jsonObject));

                    cancel();
                } else if (delayTeleport.get(player.getName()) == null) {
                    player.sendTitle("Cancel !", "Teleporter", 10, 10, 10);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onPlayerWalking(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location fromLoc = event.getFrom();
        Location toLoc = event.getTo();

        if (delayTeleport.containsKey(player.getName()) || (fromLoc.getBlockX() != toLoc.getBlockX() || fromLoc.getBlockY() != toLoc.getBlockY())) {
                delayTeleport.remove(player.getName());
        }
    }

}
