package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nxdus.core.paper.KCore;

public class HatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return true;
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        ItemStack itemInHelmet = player.getInventory().getHelmet();

        if (itemInMainHand.getType() != Material.AIR) {
            player.getInventory().setHelmet(itemInMainHand);
            player.getInventory().setItemInMainHand(itemInHelmet);
            player.sendMessage(KCore.translate.prefix("&aYou are now wearing the item in your hand as a hat!"));
        } else {
            player.sendMessage(KCore.translate.prefix("&cYou need to hold an item to put it on your head!"));
        }

        return true;
    }
}
