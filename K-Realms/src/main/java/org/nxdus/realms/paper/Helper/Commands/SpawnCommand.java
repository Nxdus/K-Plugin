package org.nxdus.realms.paper.Helper.Commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nxdus.TeleportAPI.KTeleportAPI;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;

import java.sql.PreparedStatement;

public class SpawnCommand implements CommandExecutor {

    private final MainPaper instance;

    public SpawnCommand(MainPaper instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be executed by a player");
            return true;
        }

        String[] coords = KCore.settings.getString("realms.spawn.coordinate").split(" ");

        KTeleportAPI.teleportPlayerToWorld(player, "realm-spawn", coords[0], Double.parseDouble(coords[1]), Double.parseDouble(coords[2]), Double.parseDouble(coords[3]), Float.parseFloat(coords[4]), Float.parseFloat(coords[5]));

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            try {
                PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("UPDATE users SET last_location = null WHERE uuid = ?");
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20L * 3L);

        return false;
    }
}
