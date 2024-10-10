package org.nxdus.core.velocity.core;

import com.velocitypowered.api.proxy.server.ServerInfo;
import org.nxdus.core.velocity.KCoreVelocity;

import java.net.InetSocketAddress;

public class DynamicServerListener {
    public static void addServer(String name, String ip, int port) {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        ServerInfo serverInfo = new ServerInfo(name, address);
        KCoreVelocity.proxyServer.registerServer(serverInfo);
        KCoreVelocity.logger.info("@ Server added: {} ({}:{})", name, ip, port);
    }

    public static void removeServer(String name) {
        KCoreVelocity.proxyServer.getServer(name).ifPresent(serverInfo -> {
            KCoreVelocity.proxyServer.unregisterServer(serverInfo.getServerInfo());
            KCoreVelocity.logger.info("@ Server removed: {}", name);
        });
    }
}
