package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        World world = player.getWorld();
        Location location = player.getLocation();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = world.getHighestBlockYAt(x, z);

        Location topLocation = new Location(world, x + 0.5, y + 1, z + 0.5);
        player.teleport(topLocation);

        return true;
    }
}
