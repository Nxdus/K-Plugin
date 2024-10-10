package org.nxdus.TeleportVelocity.Listeners;

import org.json.JSONArray;
import org.nxdus.TeleportVelocity.KTeleport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldListListener {

    public static HashMap<String, List<String>> worldList = new HashMap<>();

    public WorldListListener(String serverId, String serverType, String worlds) {
        String server = "";
        if (!serverId.isEmpty()) server = serverType + "-" + serverId; else server = serverType;
        worldList.put(server,convertStringToList(worlds));

        KTeleport.logger.info("@ Worldlist add {}", serverType);
    }

    private List<String> convertStringToList(String s) {
        JSONArray jsonArray = new JSONArray(s);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

}
