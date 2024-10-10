package org.nxdus.populators;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final MainPaper instance;
    private FileConfiguration config;

    public ConfigManager(MainPaper plugin) {
        this.instance = plugin;
        loadConfig();
    }

    public void loadConfig() {
        instance.saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(new File(instance.getDataFolder(), "config.yml"));
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public String getSetting(String path) {
        return config.getString(path);
    }

    public void saveConfig() {
        try {
            config.save(new File(instance.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
