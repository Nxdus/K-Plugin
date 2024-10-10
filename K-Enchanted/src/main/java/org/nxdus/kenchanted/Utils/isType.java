package org.nxdus.kenchanted.Utils;

import org.bukkit.Material;

public class isType {

    public static boolean isArmor(Material material) {
        return switch (material) {
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS, CHAINMAIL_HELMET,
                 CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS, IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS,
                 IRON_BOOTS, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS, GOLDEN_HELMET,
                 GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS, NETHERITE_HELMET, NETHERITE_CHESTPLATE,
                 NETHERITE_LEGGINGS, NETHERITE_BOOTS -> true;
            default -> false;
        };
    }

    public static boolean isWeapon(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD, DIAMOND_SWORD, NETHERITE_SWORD, WOODEN_AXE,
                 STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE -> true;
            default -> false;
        };
    }

    public static boolean isEquipment(Material material) {
        return switch (material) {
            case WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE,
                 WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL, WOODEN_HOE,
                 STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE, SHEARS, FISHING_ROD, FLINT_AND_STEEL, BOW,
                 CROSSBOW, SHIELD -> true;
            default -> false;
        };
    }

}
