package org.nxdus.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.nxdus.core.paper.KCore;

public class PubSub {

    public PubSub() {
        KCore.redisManager.subscribe("k-chat", PubSub::onMessage);
    }

    private static void onMessage(String channel, String message) {
        try {
            Gson gson = new Gson();
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

            String sendMessage = jsonMessage.has("message") ? jsonMessage.get("message").getAsString() : "";
            PlayerChatCommand.senderMessage(sendMessage);
        } catch (Exception e) {
            System.out.println("Failed to process message: " + e.getMessage());
        }
    }

}
