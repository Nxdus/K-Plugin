package org.nxdus.TeleportVelocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.core.velocity.KCoreVelocity;

public class MessageSender {

    private static final Gson gson = new Gson();

    public static void teleportToPlayers(RegisteredServer server, String fromPlayer, String toPlayer, boolean inServer) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "teleport");
        jsonObject.addProperty("sub-action", "to-player");
        jsonObject.addProperty("source-player", fromPlayer);
        jsonObject.addProperty("target-player", toPlayer);
        jsonObject.addProperty("in-server", inServer);
        KCoreVelocity.redisManager.publish(server.getServerInfo().getName(), gson.toJson(jsonObject));
    }

    public static void teleportToWorld(RegisteredServer server, String fromPlayer, String targetWorld, double coordsX, double coordsY, double coordsZ, float yaw, float pitch, boolean inServer) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "teleport");
        jsonObject.addProperty("sub-action", "to-world");
        jsonObject.addProperty("source-player", fromPlayer);
        jsonObject.addProperty("target-world", targetWorld);
        jsonObject.addProperty("coordinate-x", coordsX);
        jsonObject.addProperty("coordinate-y", coordsY);
        jsonObject.addProperty("coordinate-z", coordsZ);
        jsonObject.addProperty("yaw", yaw);
        jsonObject.addProperty("pitch", pitch);
        jsonObject.addProperty("in-server", inServer);
        KCoreVelocity.redisManager.publish(server.getServerInfo().getName(), gson.toJson(jsonObject));
    }

    public static void sendPlayerTeleportDelay(RegisteredServer server, String player) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "send-player-teleport-delay");
        jsonObject.addProperty("player", player);

        KCoreVelocity.redisManager.publish(server.getServerInfo().getName(), gson.toJson(jsonObject));
    }

    public static void sendPlayerTeleportAcceptMenu(RegisteredServer server, String player) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "send-player-teleport-accept-menu");
        jsonObject.addProperty("player", player);

        KCoreVelocity.redisManager.publish(server.getServerInfo().getName(), gson.toJson(jsonObject));
    }

}
