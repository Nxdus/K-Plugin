package org.nxdus.PlayerPin.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class CreatePinMenu {

    public static Inventory inventory;

    public CreatePinMenu() {
        createInventory();
    }

    private void createInventory() {
        inventory = Bukkit.createInventory(null, 36, "PIN Menu");
        initializeItems();
    }

    private void initializeItems() {
        List<Integer> slotsNumber = Arrays.asList(3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32);
        int number = 1;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotsNumber.contains(i)) {
                switch (i) {
                    case 30 -> inventory.setItem(i, new ItemStack(Material.GREEN_WOOL));
                    case 31 -> inventory.setItem(i, new ItemStack(Material.FILLED_MAP));
                    case 32 -> inventory.setItem(i, new ItemStack(Material.RED_WOOL));
                    default -> inventory.setItem(i, new ItemStack(Material.MAP, number++));
                }
            } else {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

}
