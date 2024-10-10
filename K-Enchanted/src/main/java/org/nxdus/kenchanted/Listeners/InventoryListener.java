package org.nxdus.kenchanted.Listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nxdus.kenchanted.Utils.CustomEnchantItems;
import org.nxdus.kenchanted.Utils.isType;

public class InventoryListener implements Listener {
    NamespacedKey hasSlot = new NamespacedKey("custom-enchant", "has-slot");

    @EventHandler
    private void onInventoryEvent(InventoryClickEvent event) {

        if (event.getInventory().getType() != InventoryType.CRAFTING) return;

        ItemStack itemStack = (event.getCursor().getType() == Material.AIR) ? event.getCurrentItem() : event.getCursor();
        Material itemType = (itemStack != null) ? itemStack.getType() : Material.AIR;

        if (itemType == Material.AIR) return;

        itemStack.editMeta(itemMeta -> {

            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            boolean hasSlotValue = container.has(hasSlot, PersistentDataType.INTEGER);

            if (!hasSlotValue) {
                if (isType.isArmor(itemType)) {
                    itemMeta.getPersistentDataContainer().set(hasSlot, PersistentDataType.INTEGER, 1);
                } else if (isType.isWeapon(itemType)) {
                    itemMeta.getPersistentDataContainer().set(hasSlot, PersistentDataType.INTEGER, 1);
                } else if (isType.isEquipment(itemType)) {
                    itemMeta.getPersistentDataContainer().set(hasSlot, PersistentDataType.INTEGER, 1);
                }
            }
        });

        CustomEnchantItems.UpdateSlotValue(itemStack);
    }
}
