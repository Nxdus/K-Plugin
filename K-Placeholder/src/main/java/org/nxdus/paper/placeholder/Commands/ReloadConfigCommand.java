package org.nxdus.paper.placeholder.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.paper.placeholder.Config.ConfigManager;

public class ReloadConfigCommand implements CommandExecutor {

    private final ConfigManager configManager;

    public ReloadConfigCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player player) {
            configManager.loadConfig();
            player.sendMessage("Reloaded !");
            return true;
        }

        return false;
    }
}
