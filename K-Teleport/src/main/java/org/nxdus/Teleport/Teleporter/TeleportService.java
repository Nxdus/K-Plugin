package org.nxdus.Teleport.Teleporter;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nxdus.Teleport.KTeleport;
import org.nxdus.Teleport.Teleporter.TeleportAcceptMenuServices.PlayerTeleportMenuService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeleportService implements Listener {

    private static HashMap<String, String> teleportRequests;
    private static HashMap<String, List<String>> changeWorldRequests;

    private final KTeleport plugin;

    public TeleportService(KTeleport plugin) {
        this.plugin = plugin;

        teleportRequests = new HashMap<>();
        changeWorldRequests = new HashMap<>();

        registerHandler();
    }

    private void registerHandler() {

        new PlayerTeleportMenuService(plugin);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDelayService(plugin), plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String _teleportRequests = teleportRequests.get(player.getName());
        List<String> _changeWorldRequests = changeWorldRequests.get(player.getName());

        if (_teleportRequests != null) {
            Player _targetPlayer = Bukkit.getPlayer(_teleportRequests);
            if (_targetPlayer != null && _targetPlayer.isOnline()) {
                player.teleport(_targetPlayer.getLocation());
                teleportRequests.remove(player.getName());
            }
        } else if (_changeWorldRequests != null) {
            World targetWorld = Bukkit.getWorld(_changeWorldRequests.get(0));
            double posX = Double.parseDouble(_changeWorldRequests.get(1));
            double posY = Double.parseDouble(_changeWorldRequests.get(2));
            double posZ = Double.parseDouble(_changeWorldRequests.get(3));
            float yaw = Float.parseFloat(_changeWorldRequests.get(4));
            float pitch = Float.parseFloat(_changeWorldRequests.get(5));

            if (targetWorld != null) {
                player.teleport(new Location(targetWorld, posX, posY, posZ, yaw, pitch));
                changeWorldRequests.remove(player.getName());
            }
        }
    }

    public static void receiveMessage(JsonObject jsonMessage) {
        String subAction = jsonMessage.has("sub-action") ? jsonMessage.get("sub-action").getAsString() : "";
        String fromPlayer = jsonMessage.has("source-player") ? jsonMessage.get("source-player").getAsString() : "";
        boolean inServer = jsonMessage.has("in-server") && jsonMessage.get("in-server").getAsBoolean();

        if (subAction.equalsIgnoreCase("to-player")) {
            String targetPlayer = jsonMessage.has("target-player") ? jsonMessage.get("target-player").getAsString() : "";
            playerTeleport(inServer, fromPlayer, targetPlayer);
        } else if (subAction.equalsIgnoreCase("to-world")) {
            String targetWorld = jsonMessage.has("target-world") ? jsonMessage.get("target-world").getAsString() : "";
            String posX = jsonMessage.has("coordinate-x") ? jsonMessage.get("coordinate-x").getAsString() : "";
            String posY = jsonMessage.has("coordinate-y") ? jsonMessage.get("coordinate-y").getAsString() : "";
            String posZ = jsonMessage.has("coordinate-z") ? jsonMessage.get("coordinate-z").getAsString() : "";
            String yaw = jsonMessage.has("yaw") ? jsonMessage.get("yaw").getAsString() : "";
            String pitch = jsonMessage.has("pitch") ? jsonMessage.get("pitch").getAsString() : "";
            worldTeleport(inServer, fromPlayer, new ArrayList<>(List.of(targetWorld, posX, posY, posZ, yaw, pitch)));
        }
    }

    public static void playerTeleport(boolean inServer, String fromPlayer, String targetPlayer) {
        if (inServer) {
            Player _fromPlayer = Bukkit.getPlayer(fromPlayer);
            Player _targetPlayer = Bukkit.getPlayer(targetPlayer);
            if (_fromPlayer != null && _targetPlayer != null && _targetPlayer.isOnline()) {
                _fromPlayer.teleportAsync(_targetPlayer.getLocation());
            }
        } else {
            teleportRequests.put(fromPlayer, targetPlayer);
        }
    }

    public static void worldTeleport(boolean inServer, String fromPlayer, List<String> worldDetail) {
        if (inServer) {
            Player _fromPlayer = Bukkit.getPlayer(fromPlayer);
            World targetWorld = Bukkit.getWorld(worldDetail.get(0));
            double posX = Double.parseDouble(worldDetail.get(1));
            double posY = Double.parseDouble(worldDetail.get(2));
            double posZ = Double.parseDouble(worldDetail.get(3));
            float yaw = Float.parseFloat(worldDetail.get(4));
            float pitch = Float.parseFloat(worldDetail.get(5));

            if (_fromPlayer != null) {
                _fromPlayer.teleportAsync(new Location(targetWorld, posX,posY,posZ,yaw,pitch));
            }

        } else {
            changeWorldRequests.put(fromPlayer, worldDetail);
        }
    }
}
