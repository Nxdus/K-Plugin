package org.nxdus.mythiccrucible;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class PreventHackOp implements Listener {

    private final MainPaper plugin;

    public PreventHackOp(MainPaper plugin) {
        this.plugin = plugin;

        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        block.getWorld().getNearbyEntities(block.getLocation(), 5, 5, 5).forEach(entity -> {
            event.getPlayer().sendMessage(entity.getType().toString());
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                PersistentDataContainer container = entity.getPersistentDataContainer();

                if(container.has(FurnitureKeys.FURNITURE_TYPE)) {
                    event.getPlayer().sendMessage(FurnitureKeys.LIST[0] + ", Value: " + container.get(FurnitureKeys.FURNITURE_TYPE, PersistentDataType.STRING));
                    //container.set(FurnitureKeys.FURNITURE_ORIENTATION, PersistentDataType.FLOAT, 0F);
                }
                if(container.has(FurnitureKeys.FURNITURE_ORIENTATION)) {
                    event.getPlayer().sendMessage(FurnitureKeys.LIST[2] + ", Value: " + container.get(FurnitureKeys.FURNITURE_ORIENTATION, PersistentDataType.FLOAT));
                }
            }
        });

//        BlockState state = block.getState();
//
//        // ตรวจสอบว่าบล็อกนั้นเป็น TileState หรือไม่ (เช่น Chest, Furnace)
//        if (state instanceof TileState) {
//            TileState tileState = (TileState) state;
//            PersistentDataContainer container = tileState.getPersistentDataContainer();
//
//            // วนลูปเพื่อดูรายการ key-value ทั้งหมดใน PersistentDataContainer
//            for (NamespacedKey key : container.getKeys()) {
//                String value = container.get(key, PersistentDataType.STRING);
//
//                // แสดง key และ value
//                if (value != null) {
//                    event.getPlayer().sendMessage("Key: " + key.toString() + ", Value: " + value);
//                } else {
//                    event.getPlayer().sendMessage("Key: " + key.toString() + " has no value or is not a STRING type.");
//                }
//            }
//        } else {
//            event.getPlayer().sendMessage("This block does not have a PersistentDataContainer.");
//        }
    }
}
