package org.nxdus.chat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class KChat extends JavaPlugin {

    @Override
    public void onEnable() {

        RegHandler(this);
        RegCommands(this);

        new PubSub();

        getLogger().info("K-Chat Plugin Enabled !");
    }

    @Override
    public void onDisable() {
        getLogger().info("K-Chat Plugin Disabled !");
    }

    private void RegHandler(KChat instance) {

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null || Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            getLogger().warning("PlaceholderAPI or Floodgate not found!");
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerChatListener(), instance);
    }

    private void RegCommands(KChat instance) {
        getCommand("chat-global").setExecutor(new PlayerChatCommand());
        getCommand("chat-ads").setExecutor(new PlayerChatCommand());
    }
}
