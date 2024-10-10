package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.TeleportAPI.KTeleportAPI;
import org.nxdus.core.paper.KCore;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetSpawnCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return false;
        }

        Location loc = player.getLocation();

        try {

            PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("UPDATE settings SET value = ? WHERE `key` = ?");
            preparedStatement.setString(2, "realms.spawn.coordinate");
            preparedStatement.setString(1, loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + loc.getYaw() + " " + loc.getPitch());
            preparedStatement.executeUpdate();
            preparedStatement.close();

            KCore.settings.reload();

            player.sendMessage("Set spawn coordinate !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return true;
    }
}
