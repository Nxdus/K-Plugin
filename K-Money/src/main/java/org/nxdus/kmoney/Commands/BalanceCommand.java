package org.nxdus.kmoney.Commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.kmoney.Providers.HookProvider;


public class BalanceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("balance")) {

            if (strings.length > 1 && strings[0].equalsIgnoreCase("give") && player.hasPermission("k_money.commands.give")) {
                int amount = Integer.parseInt(strings[1]);

                if (amount > 0) {
                    HookProvider.economy.depositPlayer(player, amount);
                }

                player.sendMessage("You give money from you account : " + amount);

                return true;
            } else if (strings.length > 1 && strings[0].equalsIgnoreCase("take") && player.hasPermission("k_money.commands.take")) {
                int amount = Integer.parseInt(strings[1]);

                if (amount > 0) {
                    HookProvider.economy.withdrawPlayer(player, amount);
                }

                player.sendMessage("You take money from you account : " + amount);

                return true;
            } else if (player.hasPermission("k_money.commands.show_other") && strings.length > 0) {

                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(strings[0]);
                String finalMessage = targetPlayer.getName() + " has : " + HookProvider.economy.format(HookProvider.economy.getBalance(targetPlayer));
                player.sendMessage(finalMessage);

                return true;
            }

            if (HookProvider.economy.getBalance(player) > 0) {
                player.sendMessage("You has : " + HookProvider.economy.format(HookProvider.economy.getBalance(player)));
            } else {
                player.sendMessage("You has no money.");
            }
        }

        return true;
    }
}
