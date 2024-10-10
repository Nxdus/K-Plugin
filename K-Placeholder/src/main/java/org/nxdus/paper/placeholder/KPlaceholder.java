package org.nxdus.paper.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.paper.placeholder.Commands.ReloadConfigCommand;
import org.nxdus.paper.placeholder.Config.ConfigManager;
import org.nxdus.paper.placeholder.Expansion.BedrockOffsetExpansion;
import org.nxdus.paper.placeholder.Expansion.CustomExpansion;
import org.nxdus.paper.placeholder.Expansion.OffsetExpansion;

public final class KPlaceholder extends JavaPlugin {

    public static KPlaceholder instance;
    private static ConfigManager configManager;

    private OffsetExpansion offsetExpansion;
    private CustomExpansion customExpansion;
    private BedrockOffsetExpansion bedrockOffsetExpansion;

    @Override
    public void onEnable() {

        instance = this;

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        getCommand("k-reload").setExecutor(new ReloadConfigCommand(configManager));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {

            offsetExpansion = new OffsetExpansion(this);
            offsetExpansion.register();

            customExpansion = new CustomExpansion(this);
            customExpansion.register();

            bedrockOffsetExpansion = new BedrockOffsetExpansion(this);
            bedrockOffsetExpansion.register();

        }

        getLogger().info("KPlaceholder plugin enabled !");
    }

    @Override
    public void onDisable() {

        configManager.writeConfig();

        if (offsetExpansion != null) {
            offsetExpansion.unregister();
        }

        if (customExpansion != null) {
            customExpansion.unregister();
        }

        if (bedrockOffsetExpansion != null) {
            bedrockOffsetExpansion.unregister();
        }

        getLogger().info("KPlaceholder plugin disabled !");
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
