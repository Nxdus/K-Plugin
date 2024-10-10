package org.nxdus.TeleportVelocity.Commands;

import com.velocitypowered.api.proxy.ProxyServer;
import org.nxdus.TeleportVelocity.KTeleport;
import org.slf4j.Logger;

public class CommandManager {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final KTeleport plugin;

    public CommandManager(ProxyServer proxyServer, Logger logger, KTeleport plugin) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.plugin = plugin;

        onProxyInitialization();
    }

    private void onProxyInitialization() {

        com.velocitypowered.api.command.CommandManager commandManager = proxyServer.getCommandManager();

        commandManager.register(
                commandManager
                        .metaBuilder("teleport")
                        .aliases("tp")
                        .plugin(plugin)
                        .build(), TeleportCommand.createTeleportCommand(proxyServer)
        );

        new TeleportRequestCommands(proxyServer, plugin);

        commandManager.register(
                commandManager
                        .metaBuilder("tpa")
                        .plugin(plugin)
                        .build(), TeleportRequestCommands.createTpaCommand()
        );

        commandManager.register(
                commandManager
                        .metaBuilder("tpahere")
                        .plugin(plugin)
                        .build(), TeleportRequestCommands.createTpahereCommand()
        );

        commandManager.register(
                commandManager
                        .metaBuilder("tpaccept")
                        .plugin(plugin)
                        .build(), TeleportRequestCommands.createTpacceptCommand()
        );

        logger.info("@ CommandsManager initialized");
    }
}
