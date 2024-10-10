package org.nxdus.realms.velocity.CommandsService;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.realms.velocity.MainVelocity;
import org.nxdus.realms.velocity.CommandsService.Commands.RedisCommand;
import org.slf4j.Logger;

public class CommandsManager {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final MainVelocity plugin;

    public CommandsManager(ProxyServer proxyServer, Logger logger, MainVelocity plugin) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.plugin = plugin;

        onProxyInitialization();
    }

    private void onProxyInitialization() {

        CommandManager commandManager = proxyServer.getCommandManager();

        commandManager.register(
                commandManager
                        .metaBuilder("vredis")
                        .aliases("vrd")
                        .plugin(plugin)
                        .build(), RedisCommand.createRedisCommand(proxyServer)
        );

        logger.info("@ CommandsManager initialized");
    }

}
