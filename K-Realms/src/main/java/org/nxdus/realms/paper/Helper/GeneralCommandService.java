package org.nxdus.realms.paper.Helper;

import org.bukkit.Bukkit;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.realms.paper.Helper.Commands.*;

public class GeneralCommandService {
    private final MainPaper plugin;

    public GeneralCommandService(MainPaper plugin) {
        this.plugin = plugin;

        registerHandler();
    }

    private void registerHandler() {

        new CommandsYML(plugin);

        // Spawn Command
        plugin.getCommand("spawn").setExecutor(new SpawnCommand(plugin));
        plugin.getCommand("setspawn").setExecutor(new SetSpawnCommand());

        // Change Gammodes Command
        plugin.getCommand("gm").setExecutor(new ChangeGamemodes());

        // Movement Commands
        plugin.getCommand("fly").setExecutor(new MovementCommands());
        plugin.getCommand("speed").setExecutor(new MovementCommands());

        // Godmode Command
        plugin.getCommand("kgod").setExecutor(new GodModeCommand());

        // Hat Command
        plugin.getCommand("hat").setExecutor(new HatCommand());

        // Up Command
        plugin.getCommand("top").setExecutor(new UpCommand());

        // Item info Command
        plugin.getCommand("item-info").setExecutor(new ItemInfoCommand());

        Bukkit.reloadCommandAliases();
    }
}
