package org.nxdus.TeleportAPI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.core.paper.KCore;

public final class KTeleportAPI extends JavaPlugin {

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    static Gson gson = new Gson();

    public static void teleportPlayerToPlayer(Player sourcePlayer, Player targetPlayer) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "teleport-player-to-player");
        jsonObject.addProperty("source-player", sourcePlayer.getName());
        jsonObject.addProperty("target-player", sourcePlayer.getName());

        KCore.redisManager.publish("teleport-api", gson.toJson(jsonObject));
    }

    public static void teleportPlayerToServer(Player sourcePlayer, String serverName) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "teleport-player-to-server");
        jsonObject.addProperty("source-player", sourcePlayer.getName());
        jsonObject.addProperty("target-server", serverName);

        KCore.redisManager.publish("teleport-api", gson.toJson(jsonObject));
    }

    public static void teleportPlayerToWorld(Player sourcePlayer,String serverName, String worldName, double coordsX, double coordsY, double coordsZ, float yaw, float pitch) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "teleport-player-to-world");
        jsonObject.addProperty("source-player", sourcePlayer.getName());
        jsonObject.addProperty("target-server", serverName);
        jsonObject.addProperty("target-world", worldName);
        jsonObject.addProperty("coordinate-x", coordsX);
        jsonObject.addProperty("coordinate-y", coordsY);
        jsonObject.addProperty("coordinate-z", coordsZ);
        jsonObject.addProperty("yaw", yaw);
        jsonObject.addProperty("pitch", pitch);

        KCore.redisManager.publish("teleport-api", gson.toJson(jsonObject));
    }
}
