package org.nxdus.PlayerPinVelocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.PlayerPinVelocity.Listener.PlayerPINService;
import org.nxdus.core.velocity.KCoreVelocity;

public class VelocitySubscribe {

    public VelocitySubscribe() {
        KCoreVelocity.redisManager.subscribe("velocity", (channel, message) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";

                if ("update-player-pin".equalsIgnoreCase(action)) {
                    String playerUUID = jsonMessage.has("player-uuid") ? jsonMessage.get("player-uuid").getAsString() : "";
                    String playerPIN = jsonMessage.has("player-pin") ? jsonMessage.get("player-pin").getAsString() : "";
                    PlayerPINService.updatePlayerPIN(playerUUID, playerPIN);
                } else if ("update-player-session".equalsIgnoreCase(action)) {
                    String playerUUID = jsonMessage.has("player-uuid") ? jsonMessage.get("player-uuid").getAsString() : "";
                    PlayerPINService.addPlayerSession(playerUUID);
                }

            } catch (Exception e) {
                System.out.println("Failed to process message: " + e.getMessage());
            }
        });
    }

}
