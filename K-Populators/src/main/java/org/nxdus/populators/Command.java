package org.nxdus.populators;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(commandSender instanceof Player player) {

            if (strings.length == 1 && strings[0].equalsIgnoreCase("reload")) {
                MainPaper.getConfigManager().loadConfig();
                player.sendMessage("Configuration reloaded!");
            }

            Chunk chunk = player.getLocation().getChunk();
            MainPaper.plugin.Hacker(chunk);
            return true;
        }

        commandSender.sendMessage("§cYou must be a player to use this command!");

        return false;
    }
}
