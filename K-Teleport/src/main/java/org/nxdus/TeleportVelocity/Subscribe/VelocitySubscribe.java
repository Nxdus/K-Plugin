package org.nxdus.TeleportVelocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.TeleportVelocity.Commands.TeleportRequestCommands;
import org.nxdus.TeleportVelocity.KTeleport;
import org.nxdus.TeleportVelocity.Listeners.WorldListListener;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.velocity.KCoreVelocity;

import java.sql.PreparedStatement;

public class VelocitySubscribe {

    public VelocitySubscribe() {

        KCoreVelocity.redisManager.subscribe("velocity", (channel, message) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";

                String serverId = jsonMessage.has("server-id") ? jsonMessage.get("server-id").getAsString() : "";
                String serverType = jsonMessage.has("server-type") ? jsonMessage.get("server-type").getAsString() : "";

                if ("teleport-success".equalsIgnoreCase(action)) {
                    String playerName = jsonMessage.has("player") ? jsonMessage.get("player").getAsString() : "";
                    TeleportRequestCommands.PlayerDelaySuccess(playerName);
                } else if ("teleport-accept".equalsIgnoreCase(action)) {
                    String playerName = jsonMessage.has("player") ? jsonMessage.get("player").getAsString() : "";
                    TeleportRequestCommands.TeleportAccept(playerName);
                } else if ("send-world".equals(action)) {
                    String worlds = jsonMessage.has("worlds") ? jsonMessage.get("worlds").getAsString() : "";
                    new WorldListListener(serverId, serverType, worlds);
                }

            } catch (Exception e) {
                System.out.println("Failed to process message: " + e.getMessage());
            }
        });

    }

}
