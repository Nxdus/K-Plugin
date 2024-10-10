package org.nxdus.core.velocity.subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.nxdus.core.velocity.KCoreVelocity;
import org.nxdus.core.velocity.core.DynamicServerListener;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.core.shared.managers.RedisManager;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class VelocitySubscribe {

    public VelocitySubscribe(RedisManager redisManager, ConfigManager configManager, Connection connection) {

        ConfigManager.ConfigType listenerChannel = configManager.getType();
        KCoreVelocity.logger.info("[K-Core] Redis Subcribe CHANNEL: {}", "velocity");

        redisManager.subscribe("velocity", (channel, message) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
                String action = jsonMessage.has("action") ? jsonMessage.get("action").getAsString() : "";
            } catch (Exception e) {
                System.err.println("Failed to process message: " + e.getMessage());
            }
        });

    }

}
