package org.nxdus.realms.velocity.Subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.nxdus.core.velocity.KCoreVelocity;
import org.nxdus.realms.velocity.MainVelocity;
import org.nxdus.core.shared.managers.ConfigManager;

public class VelocitySubscribe {

    public VelocitySubscribe() {

        ConfigManager.ConfigType listenerChannel = KCoreVelocity.configManager.getType();
        MainVelocity.logger.info("[K-Realms] Redis Subscribe CHANNEL: {}", "velocity");

        KCoreVelocity.redisManager.subscribe("velocity", (channel, message) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";

                RealmManageServer.RealmManageServerMessage(action, jsonMessage);
                RealmPlayerManage.RealmPlayerManageMessage(action, jsonMessage);

            } catch (Exception e) {
                System.err.println("Failed to process message: " + e.getMessage());
            }
        });

    }

}
