package org.nxdus.kcatch.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class RepairCancelListener implements Listener {

    @EventHandler
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        if (event.isRepair()) {

            boolean isCatcherItems = Arrays.stream(event.getInventory().getMatrix()).anyMatch(itemStack -> {
                if (itemStack == null) return false;

                ItemMeta itemMeta = itemStack.getItemMeta();

                return itemMeta != null && itemMeta.hasCustomModelData() && (itemMeta.getCustomModelData() == 555 || itemMeta.getCustomModelData() == 556);
            });

            if (isCatcherItems) event.getInventory().setResult(null);
        }
    }

}
