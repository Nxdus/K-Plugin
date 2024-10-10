package org.nxdus.Teleport.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.nxdus.Teleport.Teleporter.PlayerDelayService;
import org.nxdus.Teleport.Teleporter.TeleportAcceptMenuServices.PlayerTeleportMenuService;
import org.nxdus.Teleport.Teleporter.TeleportService;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.shared.managers.ConfigManager;

public class PaperSubscribe {

    public static String listenerChannel;

    public PaperSubscribe() {
        ConfigManager.ConfigType typeServer = KCore.configManager.getType();

        listenerChannel = typeServer.getValue();

        if (typeServer == ConfigManager.ConfigType.REALM) {
            listenerChannel = listenerChannel + "-" + KCore.serverUUID.toString();
        }

        KCore.redisManager.subscribe(listenerChannel, PaperSubscribe::onMessage);
    }

    private static void onMessage(String channel, String message) {
        try {
            Gson gson = new Gson();
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";

            if (action.equalsIgnoreCase("teleport")) {
                TeleportService.receiveMessage(jsonMessage);
            } else if (action.equalsIgnoreCase("send-player-teleport-delay")) {
                PlayerDelayService.receiveMessage(jsonMessage);
            } else if (action.equalsIgnoreCase("send-player-teleport-accept-menu")) {
                PlayerTeleportMenuService.receiveMessage(jsonMessage);
            }


        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }

    }
}
