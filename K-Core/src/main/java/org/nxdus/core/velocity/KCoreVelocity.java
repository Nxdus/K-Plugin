package org.nxdus.core.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.core.velocity.Listener.inGameListener;
import org.nxdus.core.velocity.core.BootstrapVelocity;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.core.shared.managers.RedisManager;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

@Plugin(id = "k-core", name = "K-Core", version = "1.0")
public class KCoreVelocity {

    public static ProxyServer proxyServer;
    public static Logger logger;

    public static Connection databaseConnection;
    public static RedisManager redisManager;
    public static ConfigManager configManager;


    @Inject
    public KCoreVelocity(ProxyServer _proxyServer, Logger _logger) {
        proxyServer = _proxyServer;
        logger = _logger;
    }

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) throws SQLException {

        proxyServer.getEventManager().register(this, new BootstrapVelocity(proxyServer, logger));
        proxyServer.getEventManager().register(this, new inGameListener());

        logger.info("@ plugin has enabled !");

        configManager = BootstrapVelocity.getConfigManager();
        redisManager = BootstrapVelocity.getRedisManager();
        databaseConnection = BootstrapVelocity.getConnection();
    }

}
