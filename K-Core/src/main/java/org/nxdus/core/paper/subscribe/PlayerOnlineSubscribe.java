package org.nxdus.core.paper.subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.nxdus.core.paper.KCore;

public class PlayerOnlineSubscribe {

    public PlayerOnlineSubscribe() {
        KCore.redisManager.subscribe("players-online", (channel, message) -> {
            Gson gson = new Gson();
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";
            String playerUniqueID = jsonMessage.has("player-unique-id") ? jsonMessage.get("player-unique-id").getAsString() : "";

            if (action.equals("add")) {
                KCore.playersOnline.add(playerUniqueID);
            } else if (action.equals("remove")) {
                KCore.playersOnline.remove(playerUniqueID);
            }

        });
    }

}
