package org.nxdus.Teleport.Teleporter.TeleportAcceptMenuServices;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.Teleport.KTeleport;
import org.nxdus.core.paper.KCore;

public class CreateAcceptMenu implements Listener {

    public static Inventory inventory;

    private static KTeleport plugin;

    public CreateAcceptMenu(KTeleport plugin) {
        CreateAcceptMenu.plugin = plugin;

        inventory = Bukkit.createInventory(null, 27, "Accept Menu");
        inventory.setItem(13, new ItemStack(Material.GREEN_WOOL));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        int clickedSlot = e.getSlot();

        if (clickedSlot == 13) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("action", "teleport-accept");
            jsonObject.addProperty("player", player.getName());

            KCore.redisManager.publish("velocity", gson.toJson(jsonObject));

            player.closeInventory();
        }
    }

    public static void openInventory(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inventory);
            }
        }.runTaskLater(plugin, 10L);
    }

}
