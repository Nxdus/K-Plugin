package org.nxdus.kenchanted.Utils;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomEnchantItems {

    public static void UpdateSlotValue(ItemStack itemStack) {
        NamespacedKey hasSlotKey = new NamespacedKey("custom-enchant", "has-slot");
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (container.has(hasSlotKey, PersistentDataType.INTEGER)) {
            int slotValue = container.get(hasSlotKey, PersistentDataType.INTEGER);
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
            Map<Enchantment, Integer> enchantList = itemMeta.getEnchants();
            List<String> enchanted = new ArrayList<>();

            enchantList.forEach((enchantment, level) -> {
                String enchantmentName = formatEnchantmentName(enchantment);
                enchanted.add(enchantmentName + " " + CovertNumber.convertToRoman(level));
            });

            updateLore(lore, slotValue, enchantList.size(), enchanted);

            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
    }

    private static String formatEnchantmentName(Enchantment enchantment) {
        String[] parts = enchantment.getKey().getKey().replace("_", " ").toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    public static void updateLore(List<String> lore, int slotValue, int enchantCount, List<String> enchanted) {
        int availableSlot = 0;
        int enchantIndex = 0;
        boolean correctLore = lore.stream().anyMatch(line -> line.contains("Enchant Slots: "));

        if (correctLore) {
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("Enchant Slots: ")) {
                    lore.set(i, ChatColor.WHITE + "Enchant Slots: " + ChatColor.GOLD + "(" + enchantCount + "/" + slotValue + ")");
                } else if (lore.get(i).contains("+")) {
                    if (enchantIndex < enchantCount) {
                        lore.set(i, ChatColor.GOLD + "+ " + enchanted.get(enchantIndex));
                        enchantIndex++;
                    } else {
                        availableSlot++;
                    }
                }
            }

            while (slotValue > availableSlot + enchantIndex) {
                lore.add(ChatColor.GRAY + "+ Available Slot");
                availableSlot++;
            }
        } else {
            lore.clear();
            lore.add("");
            lore.add(ChatColor.WHITE + "Enchant Slots: " + ChatColor.GOLD + "(0/" + slotValue + ")");
            lore.add("");
            for (int i = 0; i < slotValue; i++) {
                lore.add(ChatColor.GRAY + "+ Available Slot");
            }
        }
    }

}
