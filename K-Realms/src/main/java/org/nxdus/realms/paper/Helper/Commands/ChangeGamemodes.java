package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChangeGamemodes implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (strings.length == 0) {
            player.sendMessage("You need to specify a game mode.");
            return true;
        }

        String changeToGamemode = strings[0];

        if (changeToGamemode.equalsIgnoreCase("survival") || changeToGamemode.equalsIgnoreCase("s") || changeToGamemode.equalsIgnoreCase("0")) {
            player.setGameMode(GameMode.SURVIVAL);
        } else if (changeToGamemode.equalsIgnoreCase("creative") || changeToGamemode.equalsIgnoreCase("c") || changeToGamemode.equalsIgnoreCase("1")) {
            player.setGameMode(GameMode.CREATIVE);
        } else if (changeToGamemode.equalsIgnoreCase("adventure") || changeToGamemode.equalsIgnoreCase("a") || changeToGamemode.equalsIgnoreCase("2")) {
            player.setGameMode(GameMode.ADVENTURE);
        } else if (changeToGamemode.equalsIgnoreCase("spectator") || changeToGamemode.equalsIgnoreCase("sp") || changeToGamemode.equalsIgnoreCase("3")) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        player.sendMessage("You have changed the game mode to " + player.getGameMode().name().toLowerCase());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of("survival", "creative", "adventure", "spectator");
    }
}
