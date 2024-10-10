package org.nxdus.kenchanted.Listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nxdus.kenchanted.Utils.CustomEnchantItems;

public class PrepareAnvilListener implements Listener {

    private final NamespacedKey slotKey = new NamespacedKey("custom-enchant", "has-slot");
    private final NamespacedKey addSlotKey = new NamespacedKey("custom-enchant", "add-slot");

    @EventHandler
    private void onPrepareAnvil(PrepareAnvilEvent event) {
        Player player = (Player) event.getView().getPlayer();
        ItemStack primaryItem = event.getInventory().getFirstItem();
        ItemStack secondaryItem = event.getInventory().getSecondItem();

        if (primaryItem != null && secondaryItem != null) {
            PersistentDataContainer primaryContainer = primaryItem.getItemMeta().getPersistentDataContainer();
            PersistentDataContainer secondaryContainer = secondaryItem.getItemMeta().getPersistentDataContainer();

            boolean primaryItemHasSlot = primaryContainer.has(slotKey, PersistentDataType.INTEGER);
            boolean secondaryItemHasAddSlotValue = secondaryContainer.has(addSlotKey, PersistentDataType.INTEGER);

            if (primaryItemHasSlot && secondaryItemHasAddSlotValue) {
                addMoreSlots(event, primaryItem, primaryContainer, secondaryContainer);
                return;
            }

            if (shouldCancelResult(event, primaryItem, primaryItemHasSlot)) {
                event.setResult(null);
            } else {
                updateResultItem(event);
            }
        }
    }

    private void addMoreSlots(PrepareAnvilEvent event, ItemStack primaryItem, PersistentDataContainer primaryContainer, PersistentDataContainer secondaryContainer) {
        int currentSlotValue = primaryContainer.get(slotKey, PersistentDataType.INTEGER);
        int additionalSlotValue = secondaryContainer.get(addSlotKey, PersistentDataType.INTEGER);

        ItemStack resultItem = primaryItem.clone();
        ItemMeta resultMeta = resultItem.getItemMeta();

        if (resultMeta != null) {
            resultMeta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, currentSlotValue + additionalSlotValue);
            resultItem.setItemMeta(resultMeta);
        }

        event.getInventory().setMaximumRepairCost(0);
        event.getInventory().setRepairCost(0);
        event.getInventory().setRepairCostAmount(1);

        CustomEnchantItems.UpdateSlotValue(resultItem);
        event.setResult(resultItem);
    }

    private boolean shouldCancelResult(PrepareAnvilEvent event, ItemStack primaryItem, boolean primaryItemHasSlot) {
        ItemStack resultItem = event.getInventory().getResult();

        return resultItem != null && primaryItemHasSlot && resultItem.getItemMeta().getEnchants().size() > primaryItem.getItemMeta().getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER);
    }

    private void updateResultItem(PrepareAnvilEvent event) {
        ItemStack resultItem = event.getInventory().getResult();

        if (resultItem != null) {
            CustomEnchantItems.UpdateSlotValue(resultItem);
            event.setResult(resultItem);
        }
    }

}
