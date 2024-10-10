package org.nxdus.realms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.realms.velocity.CommandsService.CommandsManager;
import org.nxdus.realms.velocity.Subscribe.VelocitySubscribe;
import org.slf4j.Logger;

import java.sql.SQLException;

@Plugin(id = "k-realms", name = "K-Realms", version = "1.0",
    dependencies = {
        @Dependency(id = "k-core", optional = true)
    }
)
public class MainVelocity {

    public static ProxyServer proxyServer;
    public static Logger logger;

    @Inject
    public MainVelocity(ProxyServer _proxyServer, Logger _logger) {
        proxyServer = _proxyServer;
        logger = _logger;
    }

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) throws SQLException {

        new VelocitySubscribe();
        new TaskService();

        proxyServer.getEventManager().register(this, new CommandsManager(proxyServer, logger, this));

        logger.info("@ plugin has enabled !");
    }
}
