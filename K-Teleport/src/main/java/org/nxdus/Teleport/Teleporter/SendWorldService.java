package org.nxdus.Teleport.Teleporter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.shared.managers.ConfigManager;

import java.util.List;

public class SendWorldService {

    private List<World> worldList = List.of();

    public SendWorldService() {
        sendWorldList();
    }

    private void sendWorldList() {

        List<World> worlds = Bukkit.getWorlds();
        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        if (!(worldList.equals(worlds)) || worlds.isEmpty()) worldList = worlds;

        for (World world : worldList) {
            jsonArray.add(world.getName());
        }

        ConfigManager.ConfigType serverType = KCore.configManager.getType();

        jsonObject.addProperty("action", "send-world");
        jsonObject.addProperty("worlds", jsonArray.toString());
        jsonObject.addProperty("server-type", serverType.getValue());

        if (serverType == ConfigManager.ConfigType.REALM) {
            jsonObject.addProperty("server-id", KCore.serverUUID.toString());
        }

        KCore.redisManager.publish("velocity", gson.toJson(jsonObject));

    }

}
