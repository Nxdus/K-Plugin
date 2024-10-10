package org.nxdus.PlayerPin.Listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.PlayerPin.KPlayerPin;
import org.nxdus.PlayerPin.PlayerPINService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerPinMenuListener implements Listener {

    private final KPlayerPin plugin;
    private final Inventory inventory;

    private final List<String> inputPIN = new ArrayList<>(4);
    private final List<String> tempPIN = new ArrayList<>(4);

    private String playerPIN = "";
    private int handleWrongPIN = 0;

    public PlayerPinMenuListener(KPlayerPin plugin, Inventory inventory, Player player, String playerPIN) {
        this.plugin = plugin;
        this.inventory = inventory;

        openInventory(player, playerPIN);

    }

    private void openInventory(Player player, String playerPIN) {
        this.playerPIN = playerPIN == null ? "" : playerPIN;

        new BukkitRunnable() {
            public void run() {
                player.openInventory(inventory);
            }
        }.runTaskLater(plugin, 10L);
    }

    // Handle Event Input PIN
    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        int clickedSlot = e.getSlot();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) e.getWhoClicked();

        switch (clickedSlot) {
            case 30 -> handleConfirm(player);
            case 31 -> handleZeroClick();
            case 32 -> clearPinSlots();
            default -> handleNumberClick(clickedSlot);
        }

        updatePinDisplay(clickedItem);
    }

    private void handleConfirm(Player player) {
        String joinedInputPIN = String.join("", inputPIN);

        if (playerPIN.isEmpty()) {
            registerPlayerPIN(player);
        } else if (playerPIN.equals(joinedInputPIN)) {
            confirmPIN(player);
        } else {
            handleWrongPINEntry(player);
        }
    }

    // Register PIN
    private void registerPlayerPIN(Player player) {
        if (tempPIN.isEmpty()) {
            tempPIN.addAll(inputPIN);
            player.sendMessage(ChatColor.GREEN + "You have entered the first pin!");
        } else if (tempPIN.equals(inputPIN)) {
            player.sendMessage(ChatColor.GREEN + "You have entered the confirm pin!");

            playerPIN = String.join("", tempPIN);

            PlayerPINService.updatePlayerPIN(player.getUniqueId().toString(), playerPIN);
        } else {
            player.sendMessage(ChatColor.RED + "You have entered the wrong confirm pin!");
        }
    }

    // Login PIN
    private void confirmPIN(Player player) {
        player.sendMessage(ChatColor.GREEN + "Confirmation");
        PlayerPINService.updatePlayerSession(player.getUniqueId().toString());

        player.closeInventory();
    }

    // Wrong PIN Handle
    private void handleWrongPINEntry(Player player) {
        if (handleWrongPIN < 3) {
            player.sendMessage(ChatColor.RED + "Wrong PIN!");
            clearPinSlots();
            handleWrongPIN++;
        } else {
            player.kickPlayer(ChatColor.RED + "Wrong PIN!");
        }
    }

    // Handle Input Number
    private void handleZeroClick() {
        if (inputPIN.size() < 4) {
            inputPIN.add("0");
        }
    }

    private void handleNumberClick(int clickedSlot) {
        List<Integer> slotsNumber = Arrays.asList(3, 4, 5, 12, 13, 14, 21, 22, 23, 30, 31, 32);

        for (int i = 0; i < slotsNumber.size(); i++) {
            if (clickedSlot == slotsNumber.get(i) && inputPIN.size() < 4) {
                inputPIN.add(String.valueOf(i + 1));
                break;
            }
        }
    }

    // Display PIN
    private void clearPinSlots() {
        inputPIN.clear();
        inventory.setItem(33, new ItemStack(Material.AIR));
        inventory.setItem(24, new ItemStack(Material.AIR));
        inventory.setItem(15, new ItemStack(Material.AIR));
        inventory.setItem(6, new ItemStack(Material.AIR));
    }

    private void updatePinDisplay(ItemStack clickedItem) {
        switch (inputPIN.size()) {
            case 1 -> inventory.setItem(6, clickedItem);
            case 2 -> inventory.setItem(15, clickedItem);
            case 3 -> inventory.setItem(24, clickedItem);
            case 4 -> {
                if (inventory.getItem(33) == null) inventory.setItem(33, clickedItem);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory) && inputPIN.size() != 4) {
            new BukkitRunnable() {
                public void run() {
                    e.getPlayer().openInventory(inventory);
                }
            }.runTaskLater(plugin, 1L);
        } else if (inputPIN.size() == 4) {
            clearPinSlots();
        }
    }
}