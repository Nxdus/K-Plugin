package org.nxdus.kmoney.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.kmoney.Top.PlayerBalanceSnapshot;
import org.nxdus.kmoney.Top.TopRunnable;

import java.util.List;

public class TopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        List<PlayerBalanceSnapshot> list = TopRunnable.getTopList();

        int index = 0;
        if (strings.length > 0) {index = Integer.parseInt(strings[0]) - 1;}

        if (index < 0 || index > list.size()) {return false;}

        player.sendMessage(list.get(index).username() + " " + list.get(index).balance());

        return true;
    }
}
