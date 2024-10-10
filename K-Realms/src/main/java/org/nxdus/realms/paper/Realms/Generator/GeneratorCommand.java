package org.nxdus.realms.paper.Realms.Generator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.realms.paper.Realms.Generator.SubCommand.DeleteWorldSubCmd;
import org.nxdus.realms.paper.Realms.Generator.SubCommand.GeneratorSubCmd;
import org.nxdus.realms.paper.Realms.Generator.SubCommand.MigrateSubCmd;
import org.nxdus.realms.paper.Realms.Generator.SubCommand.RandomTeleportSubCmd;

import java.util.ArrayList;
import java.util.List;

public class GeneratorCommand implements CommandExecutor, TabCompleter {

    private final MainPaper plugin;

    public GeneratorCommand(MainPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if(!sender.isOp()) {
            sender.sendMessage(KCore.translate.prefix("dont-have-permission"));
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage:");
            sender.sendMessage(" /realm-generator rtp <world-name>");
            sender.sendMessage(" /realm-generator start");
            sender.sendMessage(" /realm-generator stop");
            sender.sendMessage(" /realm-generator force <prototype-biome>");
            sender.sendMessage(" /realm-generator load <type-loader> <name> <y>");
            sender.sendMessage(" /realm-generator read <name>");
            sender.sendMessage(" /realm-generator migrate <biome>");
            sender.sendMessage(" /realm-generator unload <name>");
            return false;
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("rtp")) {
            RandomTeleportSubCmd.command(sender, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("migrate")) {
            MigrateSubCmd.command(sender, args);
            return true;
        }

        if (subCommand.equalsIgnoreCase("delete")) {
            DeleteWorldSubCmd.command(sender, args);
            return true;
        }

        GeneratorSubCmd.command(sender, args);

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("rtp");
            subCommands.add("start");
            subCommands.add("stop");
            subCommands.add("read");
            subCommands.add("migrate");
            subCommands.add("load");
            subCommands.add("unload");
            subCommands.add("delete");
            subCommands.add("force");
            return subCommands;
        }

        if (args[0].equalsIgnoreCase("rtp")) {
            return RandomTeleportSubCmd.tab(sender, args);
        }

        if (args[0].equalsIgnoreCase("migrate")) {
            return MigrateSubCmd.tab(sender, args);
        }

        return GeneratorSubCmd.tab(sender, args);
    }

}
