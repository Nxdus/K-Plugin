package org.nxdus.kenchanted.Listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.kenchanted.KEnchanted;
import org.nxdus.kenchanted.Utils.CustomEnchantItems;

import java.util.Map;

public class EnchantTableListener implements Listener {

    private final KEnchanted instance;

    public EnchantTableListener(KEnchanted instance) {
        this.instance = instance;
    }

    @EventHandler
    private void onEnchantByEnchantingTable(EnchantItemEvent event) {

        NamespacedKey hasSlot = new NamespacedKey("custom-enchant", "has-slot");

        Player player = event.getEnchanter();
        ItemStack resultItem = event.getItem();
        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        int hasSlotValue = resultItem.getItemMeta().getPersistentDataContainer().get(hasSlot, PersistentDataType.INTEGER);


        if (enchants.size() > hasSlotValue) {
            player.sendMessage("You Have Only " + hasSlotValue + " Enchant Slots");
            event.setCancelled(true);
            return;
        }

        new BukkitRunnable() {
            public void run() {
                CustomEnchantItems.UpdateSlotValue(resultItem);
            }
        }.runTaskLater(instance, 5L);
    }

}
