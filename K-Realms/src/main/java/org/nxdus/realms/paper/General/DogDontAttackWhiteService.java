package org.nxdus.realms.paper.General;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.nxdus.realms.paper.MainPaper;

public class DogDontAttackWhiteService implements Listener {
    private final MainPaper plugin;

    public DogDontAttackWhiteService(MainPaper plugin) {
        this.plugin = plugin;

        registerHandler();
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();

        // ตรวจสอบว่าตัวที่เป็นหมาและเป้าหมายเป็นแกะสีดำ
        if (entity instanceof Wolf && target instanceof Sheep) {
            Wolf wolf = (Wolf) entity;
            Sheep sheep = (Sheep) target;

            if (sheep.getColor() == DyeColor.BLACK) {
                wolf.setAngry(true);
            }else {
                wolf.setAngry(false);
                event.setTarget(null);
            }
        }
    }
}
