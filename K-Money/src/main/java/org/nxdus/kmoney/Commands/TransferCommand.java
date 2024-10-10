package org.nxdus.kmoney.Commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nxdus.core.paper.KCore;
import org.nxdus.kmoney.Providers.HookProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransferCommand implements CommandExecutor, TabCompleter {

    private final Connection connection = KCore.databaseConnection;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }

        if (strings.length >= 1 && strings[0].isEmpty()) {
            commandSender.sendMessage(KCore.translate.get("balance-must-specify-player"));
            return true;
        } else if (strings.length >= 2 && strings[1].isEmpty()) {
            commandSender.sendMessage(KCore.translate.get("balance-must-specify-amount"));
            return true;
        } else if (strings.length == 0) {
            commandSender.sendMessage("Usage: /transfer <player> <amount>");
            return true;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(strings[0]);

        if (targetPlayer.getName().equals(player.getName())) {
            commandSender.sendMessage(KCore.translate.get("balance-you-cant-transfer-to-you-self"));
            return true;
        }

        double amount = Double.parseDouble(strings[1]);

        if (HookProvider.economy.has(player, amount)) {
            HookProvider.economy.withdrawPlayer(player, amount);
            HookProvider.economy.depositPlayer(targetPlayer, amount);

            String transferSuccess = KCore.translate.format("balance-transfer-success",
                    "amount", amount,
                    "player_name", targetPlayer.getName()
            );

            player.sendMessage(transferSuccess);
            sendTranscriptions(targetPlayer.getName(), player.getName(), String.valueOf(amount));
        } else {
            player.sendMessage(KCore.translate.get("balance-do-have-enough"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> list = new ArrayList<>();

        if (strings.length == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> {
              if (!player.getName().equalsIgnoreCase(commandSender.getName())) {
                  list.add(player.getName());
              }
            });
        }

        return list;
    }

    private void sendTranscriptions(String receiver, String sender, String amount) {
        if (createdTranscriptions(receiver, sender, amount)) {
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("receiver", receiver);
            jsonObject.addProperty("sender", sender);
            jsonObject.addProperty("amount", amount);

            KCore.redisManager.publish("transferred", gson.toJson(jsonObject));
        }
    }

    private boolean createdTranscriptions(String receiver, String sender, String amount) {
        try {

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users_transcriptions (from_player, to_player, amount) VALUES (?,?,?)");

            preparedStatement.setString(1,sender);
            preparedStatement.setString(2,receiver);
            preparedStatement.setDouble(3, Double.parseDouble(amount));

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };
}
