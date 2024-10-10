package org.nxdus.realms.paper.Realms.Generator.SubCommand;

import com.infernalsuite.aswm.api.world.SlimeWorld;
import org.bukkit.command.CommandSender;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.Realms.Utils.AdvancedSlimeUtil;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.nxdus.realms.paper.Realms.Generator.GeneratorCore.prototypeList;

public class DeleteWorldSubCmd {
    public static void command(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /realm-generator delete <world-name>");
            return;
        }

        try {
            AdvancedSlimeUtil.sqlLoaderRealm.deleteWorld(args[1]);
            sender.sendMessage("§aWorld " + args[1] + " has been deleted, ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    public static List<String> tab(CommandSender sender, String[] args) {
        return null;
    }
}
