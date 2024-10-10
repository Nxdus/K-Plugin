package org.nxdus.TeleportVelocity;


import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.TeleportVelocity.Commands.CommandManager;
import org.nxdus.TeleportVelocity.Subscribe.TeleportAPISubscribe;
import org.nxdus.TeleportVelocity.Subscribe.VelocitySubscribe;
import org.slf4j.Logger;


@Plugin(
        id = "k-teleport", name = "K-Teleport", version = "1.0",
        dependencies = {
                @Dependency(id = "k-core", optional = true)
        }
)

public class KTeleport {

    public static ProxyServer proxyServer;
    public static Logger logger;

    @Inject
    public KTeleport(ProxyServer _server, Logger _logger) {
        proxyServer = _server;
        logger = _logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        new VelocitySubscribe();
        new TeleportAPISubscribe();

        proxyServer.getEventManager().register(this, new CommandManager(proxyServer, logger, this));

        logger.info("K-Teleport velocity plugin enabled !");
    }

}
