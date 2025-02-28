package org.nxdus.PlayerPinVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.PlayerPinVelocity.Subscribe.VelocitySubscribe;
import org.slf4j.Logger;

@Plugin(
        id = "k-playerpin", name = "K-PlayerPin", version = "1.0",
        dependencies = {
                @Dependency(id = "k-core", optional = true)
        }
)

public class KPlayerPin {

    public static ProxyServer proxyServer;
    public static Logger logger;

    @Inject
    public KPlayerPin(ProxyServer _proxyServer, Logger _logger) {
        proxyServer = _proxyServer;
        logger = _logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        new VelocitySubscribe();

        logger.info("K-PlayerPin velocity plugin enabled !");
    }

}
