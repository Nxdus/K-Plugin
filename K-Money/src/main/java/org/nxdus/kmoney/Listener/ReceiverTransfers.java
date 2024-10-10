package org.nxdus.kmoney.Listener;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.nxdus.core.paper.KCore;
import org.nxdus.kmoney.Providers.HookProvider;

public class ReceiverTransfers {

    public ReceiverTransfers() {
        KCore.redisManager.subscribe("transferred", ReceiverTransfers::onMessage);
    }

    private static void onMessage(String channel, String message) {

        Gson gson = new Gson();
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

        try {
            String receiver = jsonMessage.has("receiver") ? jsonMessage.get("receiver").getAsString() : "";
            String sender = jsonMessage.has("sender") ? jsonMessage.get("sender").getAsString() : "";
            String amount = jsonMessage.has("amount") ? jsonMessage.get("amount").getAsString() : "";

            Bukkit.getOnlinePlayers().forEach(player -> {
               if (player.getName().equals(receiver)) {

                   String transferReceived = KCore.translate.format("balance-transfer-received",
                           "amount", HookProvider.economy.format(Double.parseDouble(amount)),
                           "player_name", sender
                   );

                   player.sendMessage(transferReceived);
               }
            });

        } catch (Exception e) {
            System.out.println("Failed to process message: " + e.getMessage());
        }

    }

}
