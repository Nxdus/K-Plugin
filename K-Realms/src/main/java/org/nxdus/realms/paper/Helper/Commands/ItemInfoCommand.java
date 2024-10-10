package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ItemInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.AIR) {
            player.sendMessage(ChatColor.GREEN + "Item in hand:");
            player.sendMessage(ChatColor.YELLOW + "Type: " + itemInHand.getType().name());
            player.sendMessage(ChatColor.YELLOW + "Amount: " + itemInHand.getAmount());
            ItemMeta itemMeta = itemInHand.getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    player.sendMessage(ChatColor.YELLOW + "Name: " + itemMeta.getDisplayName());
                }
                if (itemMeta.hasLore()) {
                    player.sendMessage(ChatColor.YELLOW + "Lore: " + itemMeta.getLore());
                }
                if (itemMeta.hasCustomModelData()) {
                    player.sendMessage(ChatColor.YELLOW + "Custom Model ID: " + itemMeta.getCustomModelData());
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are not holding any item.");
        }

        return true;
    }
}
