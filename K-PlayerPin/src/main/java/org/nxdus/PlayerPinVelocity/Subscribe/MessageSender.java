package org.nxdus.PlayerPinVelocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.core.velocity.KCoreVelocity;

public class MessageSender {

    private static final Gson gson = new Gson();

    public static void sendPlayerPinService(RegisteredServer server,String subAction, String player, String playerPIN, boolean isFirstLogin) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action", "send-player-pin");
        jsonObject.addProperty("sub-action", subAction);
        jsonObject.addProperty("player", player);
        jsonObject.addProperty("player-pin", playerPIN);
        jsonObject.addProperty("is-first-login", isFirstLogin);

        KCoreVelocity.redisManager.publish(server.getServerInfo().getName(), gson.toJson(jsonObject));
    }

}
