package org.nxdus.realms.paper.Realms.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.realms.paper.Realms.RealmsCommand;

public class RealmsInvitedSubscribe {

    public RealmsInvitedSubscribe() {

        ConfigManager.ConfigType typeServer = KCore.configManager.getType();

        String listenerChannel = typeServer.getValue();

        if (typeServer == ConfigManager.ConfigType.REALM) {
            listenerChannel = listenerChannel + "-" + KCore.serverUUID.toString();
        }

        KCore.redisManager.subscribe(listenerChannel, (channel, message) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";

                if (action.equals("realm-invited")) {

                    String invitedName = jsonMessage.has("player-invited-name") ? jsonMessage.get("player-invited-name").getAsString() : "";
                    String worldName = jsonMessage.has("realm-to-member") ? jsonMessage.get("realm-to-member").getAsString() : "";

                    Bukkit.getPlayer(invitedName).sendMessage("/realms accept");

                    RealmsCommand.hasPlayerInvited.put(invitedName, worldName);
                }

            } catch (Exception e) { System.out.println("Failed to process message: " + e.getMessage()); }
        });
    }

}
