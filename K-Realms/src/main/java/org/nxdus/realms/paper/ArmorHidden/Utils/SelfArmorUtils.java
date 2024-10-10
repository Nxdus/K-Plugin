package org.nxdus.realms.paper.ArmorHidden.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SelfArmorUtils {

    public enum ArmorType{
        HELMET(5), CHEST(6), LEGGS(7), BOOTS(8);

        private final int value;

        public static ArmorType getType(int value){
            for(int i = 0; i < values().length; i++){
                if(values()[i].getValue() == value) return values()[i];
            }
            return null;
        }

        public int getValue(){
            return value;
        }

        ArmorType(int i){
            this.value = i;
        }
    }

    public static ItemStack getArmor(ArmorType type, PlayerInventory inv){
        switch (type){
            case HELMET: if(inv.getHelmet()!=null) return inv.getHelmet().clone();
                break;
            case CHEST: if(inv.getChestplate()!=null) return inv.getChestplate().clone();
                break;
            case LEGGS: if(inv.getLeggings()!=null) return inv.getLeggings().clone();
                break;
            case BOOTS: if(inv.getBoots()!=null) return inv.getBoots().clone();
                break;
        }
        return new ItemStack(Material.AIR);
    }

    public static ItemStack getHiddenArmor(ItemStack itemStack){

        if (itemStack.getType() == Material.AIR) return new ItemStack(Material.AIR);

        ItemMeta itemMeta = itemStack.getItemMeta().clone();

        List<String> lore = new ArrayList<>();

        if (itemMeta.hasLore()) {
            lore = itemMeta.getLore();
        }

        if (!itemMeta.hasDisplayName()) {

            String[] itemDisplayName = itemStack.getType().toString().replace("_", " ").toLowerCase().split("");
            StringBuilder finalDisplayName = new StringBuilder();

            for (int i = 0; i < itemDisplayName.length; i++) {
                String letter = itemDisplayName[i];
                if (i == 0) {
                    finalDisplayName.append(letter.toUpperCase());
                } else if (i > 1 && itemDisplayName[i-1].equals(" ")) {
                    finalDisplayName.append(letter.toUpperCase());
                } else {
                    finalDisplayName.append(letter);
                }
            }

            if (itemMeta.hasEnchants()) {
                itemMeta.setDisplayName(ChatColor.AQUA + finalDisplayName.toString());
            } else {
                itemMeta.setDisplayName(ChatColor.WHITE + finalDisplayName.toString());
            }
        }

        lore.add("");

        short maxDurability = itemStack.getType().getMaxDurability();
        int Durability = ( (Damageable) itemStack.getItemMeta()).getDamage();

        lore.add(ChatColor.RED + "" + ChatColor.UNDERLINE + "Armor is invisible");
        lore.add("");
        lore.add(ChatColor.WHITE + "Durability: " + (maxDurability - Durability) + " / " + maxDurability);

        itemMeta.setLore(lore);
        itemMeta.setCustomModelData(getCustomModelDataId(itemStack));
        itemStack.setItemMeta(itemMeta);
        itemStack.setType(Material.STONE_BUTTON);

        return itemStack;
    }

    private static int getCustomModelDataId(ItemStack itemStack){
        String itemType = itemStack.getType().toString();

        if (itemType.startsWith("NETHERITE_")) {
            if (itemType.contains("HELMET")) {
                return 1016;
            } else if (itemType.contains("CHESTPLATE")) {
                return 1017;
            } else if (itemType.contains("LEGGINGS")) {
                return 1018;
            } else if (itemType.contains("BOOTS")) {
                return 1019;
            }

        } else if (itemType.startsWith("DIAMOND_")) {

            if (itemType.contains("HELMET")) {
                return 1012;
            } else if (itemType.contains("CHESTPLATE")) {
                return 1013;
            } else if (itemType.contains("LEGGINGS")) {
                return 1014;
            } else if (itemType.contains("BOOTS")) {
                return 1015;
            }

        } else if (itemType.startsWith("GOLDEN_")) {

            if (itemType.contains("HELMET")) {
                return 1008;
            } else if (itemType.contains("CHESTPLATE")) {
                return 1009;
            } else if (itemType.contains("LEGGINGS")) {
                return 1010;
            } else if (itemType.contains("BOOTS")) {
                return 1011;
            }

        } else if (itemType.startsWith("IRON_")) {

            if (itemType.contains("HELMET")) {
                return 1004;
            } else if (itemType.contains("CHESTPLATE")) {
                return 1005;
            } else if (itemType.contains("LEGGINGS")) {
                return 1006;
            } else if (itemType.contains("BOOTS")) {
                return 1007;
            }

        } else if (itemType.startsWith("LEATHER_")) {

            if (itemType.contains("HELMET")) {
                return 1000;
            } else if (itemType.contains("CHESTPLATE")) {
                return 1001;
            } else if (itemType.contains("LEGGINGS")) {
                return 1002;
            } else if (itemType.contains("BOOTS")) {
                return 1003;
            }

        } else if (itemType.startsWith("CHAINMAIL_")) {

            if (itemType.contains("HELMET")) {
                return 1020;
            } else if (itemType.contains("CHESTPLATE")) {
                return 1021;
            } else if (itemType.contains("LEGGINGS")) {
                return 1022;
            } else if (itemType.contains("BOOTS")) {
                return 1023;
            }

        } else if (itemType.startsWith("TURTLE_")) {
            return 1024;
        }

        return 0;
    }

}
