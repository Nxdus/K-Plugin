package org.nxdus.realms.paper.Realms.Generator.SubCommand;

import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
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

public class MigrateSubCmd {
    public static void command(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /realm-generator migrate <biome-name>");
            return;
        }

        String biomeType = args[1].toLowerCase();

        if (!Arrays.asList(prototypeList).contains(biomeType)) {
            sender.sendMessage("§cWe support the biome: " + String.join(", ", prototypeList) + ".");
            return;
        }

        String rename = UUID.randomUUID().toString();
        String prototypeWorld = AdvancedSlimeUtil.randomPrototype(biomeType, rename);

        if(prototypeWorld == null) {
            sender.sendMessage("§cPrototype " + biomeType + " not found, Please Generator it");
        }

        String propertyMapJson = AdvancedSlimeUtil.getPropertyMapAsString(AdvancedSlimeUtil.getTable("prototype"), prototypeWorld);

        try {
            AdvancedSlimeUtil.Instance.migrateWorld(prototypeWorld, AdvancedSlimeUtil.sqlLoaderPrototype, AdvancedSlimeUtil.sqlLoaderRealm);

            String QueryCreated = "INSERT INTO `realm_slime_world` (`id`, `name`, `property`, `created_at`, `updated_at`) VALUES (NULL, ?, ?, current_timestamp(), current_timestamp())";
            try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(QueryCreated)) {
                statement.setString(1, prototypeWorld);
                statement.setString(2, propertyMapJson);
                statement.executeUpdate();
            } catch (SQLException ex) {
                throw new IOException(ex);
            }

            sender.sendMessage("§aPrototype " + biomeType + " has been migrated, " + prototypeWorld);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    public static List<String> tab(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return List.of(prototypeList);
        }

        return null;
    }
}
