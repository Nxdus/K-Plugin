package org.nxdus.core.paper.subscribe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.core.shared.managers.RedisManager;

import java.util.UUID;

public class PaperSubscribe {

    public static String listenerChannel;

    public PaperSubscribe(RedisManager redisManager, ConfigManager configManager) {
        ConfigManager.ConfigType typeServer = configManager.getType();

        listenerChannel = typeServer.getValue();

        if(typeServer == ConfigManager.ConfigType.REALM) {
            listenerChannel = listenerChannel + "-" + KCore.serverUUID.toString();
        }

        redisManager.subscribe(listenerChannel, PaperSubscribe::onMessage);
    }

    public static String buildJson (String action, UUID serverUUID) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        String serverIp = System.getenv("SERVER_IP");
        String serverPort = System.getenv("SERVER_PORT");

        jsonObject.addProperty("action", action);
        jsonObject.addProperty("server-id", serverUUID.toString());
        jsonObject.addProperty("players", Bukkit.getServer().getOnlinePlayers().size());

        if (serverIp != null && serverPort != null) {
            jsonObject.addProperty("server-ip", serverIp);
            jsonObject.addProperty("server-port", Integer.parseInt(serverPort));
        }

        return gson.toJson(jsonObject);
    }

    private static void onMessage(String channel, String message) {

    }
}
