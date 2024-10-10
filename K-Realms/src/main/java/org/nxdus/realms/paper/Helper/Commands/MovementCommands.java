package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nxdus.core.paper.KCore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MovementCommands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("fly")) {
            PreparedStatement statement = null;
            try {
                statement = KCore.databaseConnection.prepareStatement("SELECT users.toggle_visible_armor FROM users WHERE users.uuid = ?");
                statement.setString(1, player.getUniqueId().toString());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    player.sendMessage("toggle_visible_armor:  " + resultSet.getString("toggle_visible_armor"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.sendMessage("Stop flying");
            } else {
                player.setAllowFlight(true);
                player.sendMessage("Start flying");
            }
        } else if (command.getName().equalsIgnoreCase("speed")) {

            if (strings.length == 0) {
                commandSender.sendMessage("You must specify a speed.");
                return true;
            }

            float speedValue = Float.parseFloat(strings[0]) / 100;

            if (!(speedValue <= 1.0F && speedValue >= -1.0F)) {
                player.sendMessage("Speed must be between 0 and 100");
            } else if (player.isFlying()) {
                player.setFlySpeed(speedValue);
                player.sendMessage("Set fly speed to " + speedValue);
            } else {
                player.setWalkSpeed(speedValue);
                player.sendMessage("Set walk speed to " + speedValue);
            }

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
