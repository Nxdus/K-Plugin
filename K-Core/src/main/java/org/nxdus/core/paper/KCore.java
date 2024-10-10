package org.nxdus.core.paper;

import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.core.paper.Listener.PlayerJoinListener;
import org.nxdus.core.paper.core.BootstrapPaper;
import org.nxdus.core.paper.core.PlayerDatabase;
import org.nxdus.core.paper.core.Settings;
import org.nxdus.core.paper.core.Translate;
import org.nxdus.core.paper.subscribe.PlayerOnlineSubscribe;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.core.shared.managers.RedisManager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class KCore extends JavaPlugin {


    public static UUID serverUUID = UUID.randomUUID();
    private static BootstrapPaper bootstrapPaper;

    public static Translate translate;
    public static Settings settings;


    public static Connection databaseConnection;
    public static RedisManager redisManager;
    public static ConfigManager configManager;

    public static List<String> playersOnline = new ArrayList<>();

    @Override
    public void onEnable() {
        KCore plugin = this;

        bootstrapPaper = new BootstrapPaper(this);

        configManager = BootstrapPaper.getConfigManager();
        databaseConnection = BootstrapPaper.databaseManager.getConnection();
        getLogger().info("Database connected successfully!");

        redisManager = BootstrapPaper.getRedisManager();
        translate = new Translate(plugin);
        settings = new Settings(plugin);

        new PlayerDatabase(this);
        new PlayerOnlineSubscribe();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        this.getLogger().info("KCore Plugin Enabled w/ " + configManager.getType());
    }


    @Override
    public void onDisable() {
        if(bootstrapPaper != null) {
            bootstrapPaper.disconnectFromRedis();
            bootstrapPaper.disconnectFromDatabase();
        }
    }


}
