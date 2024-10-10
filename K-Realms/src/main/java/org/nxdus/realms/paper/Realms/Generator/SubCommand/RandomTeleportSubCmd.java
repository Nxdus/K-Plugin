package org.nxdus.realms.paper.Realms.Generator.SubCommand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nxdus.realms.paper.Realms.Utils.RandomTeleportUtil;

import java.util.ArrayList;
import java.util.List;

public class RandomTeleportSubCmd {
    public static void command(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cYou cannot use this command for CONSOLE");
            return;
        }

        if (args.length != 2) {
            sender.sendMessage("§cWorld name not specified.");
            return;
        }

        World world = Bukkit.getWorld(args[1]);

        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return;
        }

        Location location = RandomTeleportUtil.findRandomSafeLocation(world);
        player.teleport(location);
    }

    public static List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> worldNames = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worldNames.add(world.getName());
            }
            return worldNames;
        }

        return null;
    }
}
