package org.nxdus.toast;

import eu.endercentral.crazy_advancements.JSONMessage;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToastCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }

        if (strings.length < 3) {
            return false;
        }

        final String style = strings[0];

        try {
            AdvancementDisplay.AdvancementFrame.parse(style);
        } catch (final Throwable throwable) {
            sender.sendMessage("Invalid style: " + strings[0]);
            return true;
        }

        final String materialName = strings[1];

        try {
            Material.valueOf(materialName.toUpperCase());
        } catch (final Throwable throwable) {
            sender.sendMessage("Invalid material: " + materialName);
            return true;
        }

        StringBuilder message = new StringBuilder();

        for (int i = 2; i < strings.length; i++) {
            message.append(strings[i]).append(" ");
        }

        String finalString = ChatColor.translateAlternateColorCodes('&', message.toString());

        ItemStack itemStack = new ItemStack(Material.PAPER);
        itemStack.editMeta(itemMeta -> {
            itemMeta.setCustomModelData(1059);
        });

        ToastNotification notification = new ToastNotification(Material.getMaterial(materialName.toUpperCase()), finalString, AdvancementDisplay.AdvancementFrame.parse(style));
        notification.send(sender);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        final List<String> list = new ArrayList<>();

        switch (strings.length) {
            case 1:
                for (AdvancementDisplay.AdvancementFrame style : AdvancementDisplay.AdvancementFrame.values()) {
                    list.add(style.name().toLowerCase());
                }
                break;
            case 2:
                for (Material material : Material.values()) {
                    list.add(material.toString().toLowerCase());
                }
                break;
            case 3:
                list.add("Hello");
        }

        return list.stream().filter(completion -> completion.startsWith(strings[strings.length - 1])).collect(Collectors.toList());
    }
}
