package org.nxdus.realms.paper.Helper.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GodModeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return true;
        }

        player.setInvulnerable(!player.isInvulnerable());
        player.sendMessage("God mode is " + (player.isInvulnerable() ? "enabled" : "disabled"));
        return true;
    }
}
