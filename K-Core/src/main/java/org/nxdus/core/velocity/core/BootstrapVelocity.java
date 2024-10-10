package org.nxdus.core.velocity.core;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.core.shared.managers.DatabaseManager;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.core.shared.managers.RedisManager;

import org.nxdus.core.velocity.subscribe.VelocitySubscribe;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class BootstrapVelocity {

    private DatabaseManager databaseManager;

    private static RedisManager redisManager;
    private static Connection databaseConnection;
    private static ConfigManager configManager;

    private final Logger logger;

    public BootstrapVelocity(ProxyServer proxyServer, Logger logger) throws SQLException {
        this.logger = logger;

        ConnectToRedis();
        ConnectToDatabase();

        databaseConnection = databaseManager.getConnection();

        onProxyInitialization();
    }

    private void onProxyInitialization() {
        new VelocitySubscribe(redisManager, configManager, databaseConnection);

        logger.info("@ Proxy initialized");
    }

    @Subscribe(order = PostOrder.EARLY)
    private void onProxyShutdown(ProxyShutdownEvent event) throws SQLException {
        DisconnectDataBase();
        logger.info("@ Proxy shutdown");
    }

    private void ConnectToDatabase() {
        databaseManager = new DatabaseManager(configManager);
        logger.info("@ Success to connect to the database!");
    }

    private void ConnectToRedis() {
        configManager = new ConfigManager();

        if (configManager.getConfigAsBoolean("redis.enable")) {
            redisManager = new RedisManager(configManager);
            logger.info("@ Redis has enabled !");
        }
    }

    private void DisconnectDataBase() throws SQLException {
        databaseManager.closeConnection();
        logger.info("@ Disconnected Success");
    }

    public static Connection getConnection() {
        return databaseConnection;
    }

    public static RedisManager getRedisManager() {
        return redisManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
