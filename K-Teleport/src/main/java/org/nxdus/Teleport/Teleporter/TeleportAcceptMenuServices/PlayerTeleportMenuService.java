package org.nxdus.Teleport.Teleporter.TeleportAcceptMenuServices;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.nxdus.Teleport.KTeleport;

public class PlayerTeleportMenuService {

    private static KTeleport plugin;

    public PlayerTeleportMenuService(KTeleport _plugin) {
        plugin = _plugin;

        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(new CreateAcceptMenu(plugin), plugin);
    }

    public static void receiveMessage(JsonObject jsonMessage) {
        String playerName = jsonMessage.has("player") ? jsonMessage.get("player").getAsString() : "";

        CreateAcceptMenu.openInventory(Bukkit.getPlayer(playerName));
    }
}
