package org.nxdus.realms.velocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.core.velocity.KCoreVelocity;

public class RealmPlayerManage {

    public static void RealmPlayerManageMessage(String action, JsonObject jsonMessage) {

        if (action.equalsIgnoreCase("realm-invited")) {

            String invitedName = jsonMessage.has("player-invited-name") ? jsonMessage.get("player-invited-name").getAsString() : "";
            String worldName = jsonMessage.has("realm-to-member") ? jsonMessage.get("realm-to-member").getAsString() : "";

            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "realm-invited");
            jsonObject.addProperty("player-invited-name", invitedName);
            jsonObject.addProperty("realm-to-member", worldName);

            RegisteredServer invitedServer = KCoreVelocity.proxyServer.getPlayer(invitedName).orElseThrow().getCurrentServer().orElseThrow().getServer();
            KCoreVelocity.redisManager.publish(invitedServer.getServerInfo().getName(), gson.toJson(jsonObject));

        }

    }

}
