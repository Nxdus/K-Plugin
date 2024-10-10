package org.nxdus.TeleportVelocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.TeleportVelocity.KTeleport;
import org.nxdus.core.velocity.KCoreVelocity;

public class TeleportAPISubscribe {

    public TeleportAPISubscribe() {
        KCoreVelocity.redisManager.subscribe("teleport-api", (channel, message) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

                String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";
                if (action.equals("teleport-player-to-player")) {
                    String fromPlayer = jsonMessage.has("source-player") ? jsonMessage.get("source-player").getAsString() : "";
                    String toPlayer = jsonMessage.has("target-player") ? jsonMessage.get("target-player").getAsString() : "";

                    Player sourcePlayer = KTeleport.proxyServer.getPlayer(fromPlayer).orElseThrow();

                    RegisteredServer fromServer = KTeleport.proxyServer.getPlayer(fromPlayer).orElseThrow().getCurrentServer().orElseThrow().getServer();
                    RegisteredServer targetServer = KTeleport.proxyServer.getPlayer(toPlayer).orElseThrow().getCurrentServer().orElseThrow().getServer();

                    if (fromServer.equals(targetServer)) {
                        MessageSender.teleportToPlayers(targetServer, fromPlayer, toPlayer, true);
                    } else if (sourcePlayer.createConnectionRequest(targetServer).connectWithIndication().completeExceptionally(new Throwable())) {
                        MessageSender.teleportToPlayers(targetServer, fromPlayer, toPlayer, false);
                    }

                } else if (action.equals("teleport-player-to-server")) {
                    String fromPlayer = jsonMessage.has("source-player") ? jsonMessage.get("source-player").getAsString() : "";
                    String toServer = jsonMessage.has("target-server") ? jsonMessage.get("target-server").getAsString() : "";

                    Player sourcePlayer = KTeleport.proxyServer.getPlayer(fromPlayer).orElseThrow();
                    RegisteredServer targetServer = KTeleport.proxyServer.getServer(toServer).orElseThrow();

                    sourcePlayer.createConnectionRequest(targetServer).connectWithIndication().completeExceptionally(new Throwable());
                } else if (action.equals("teleport-player-to-world")) {
                    String fromPlayer = jsonMessage.has("source-player") ? jsonMessage.get("source-player").getAsString() : "";
                    String toServer = jsonMessage.has("target-server") ? jsonMessage.get("target-server").getAsString() : "";
                    String toWorld = jsonMessage.has("target-world") ? jsonMessage.get("target-world").getAsString() : "";

                    double coordsX = jsonMessage.has("coordinate-x") ? jsonMessage.get("coordinate-x").getAsDouble() : 0.0;
                    double coordsY = jsonMessage.has("coordinate-y") ? jsonMessage.get("coordinate-y").getAsDouble() : 0.0;
                    double coordsZ = jsonMessage.has("coordinate-z") ? jsonMessage.get("coordinate-z").getAsDouble() : 0.0;
                    float yaw = jsonMessage.has("yaw") ? jsonMessage.get("yaw").getAsFloat() : 0.0F;
                    float pitch = jsonMessage.has("pitch") ? jsonMessage.get("pitch").getAsFloat() : 0.0F;

                    Player sourcePlayer = KTeleport.proxyServer.getPlayer(fromPlayer).orElseThrow();
                    RegisteredServer fromServer = KTeleport.proxyServer.getPlayer(fromPlayer).orElseThrow().getCurrentServer().orElseThrow().getServer();
                    RegisteredServer targetServer = KTeleport.proxyServer.getServer(toServer).orElseThrow();


                    if (fromServer.equals(targetServer)) {
                        MessageSender.teleportToWorld(targetServer,fromPlayer,toWorld,coordsX,coordsY,coordsZ,yaw,pitch,true);
                    } else if (sourcePlayer.createConnectionRequest(targetServer).connectWithIndication().completeExceptionally(new Throwable())) {
                        MessageSender.teleportToWorld(targetServer,fromPlayer,toWorld,coordsX,coordsY,coordsZ,yaw,pitch,false);
                    }
                }

            } catch (Exception e) {
                System.out.println("Failed to process message: " + e.getMessage());
            }
        });
    }

}
