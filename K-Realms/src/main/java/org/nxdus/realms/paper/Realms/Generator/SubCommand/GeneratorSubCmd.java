package org.nxdus.realms.paper.Realms.Generator.SubCommand;

import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.Realms.Generator.GeneratorCore;
import org.nxdus.realms.paper.Realms.Utils.AdvancedSlimeUtil;
import org.nxdus.realms.paper.Realms.Utils.CompoundMapConvert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorSubCmd {
    public static boolean command(CommandSender sender, String[] args) {

        if (!sender.hasPermission("realms.generator")) {
            sender.sendMessage(KCore.translate.prefix("dont-have-permission"));
            return false;
        }

        String action = args[0];

        if (action.equalsIgnoreCase("start")) {
            sender.sendMessage("§aStarting Realms Generator...");
            GeneratorCore.StartGenerator();
            return true;
        }

        if (action.equalsIgnoreCase("stop")) {
            GeneratorCore.StopGenerator();
            sender.sendMessage("§cStopped Realms Generator...");
            return true;
        }

        if (action.equalsIgnoreCase("read")) {
            if (args.length != 2) {
                sender.sendMessage("§cUsage: /realm-generator read <name>");
                return false;
            }

            String worldName = args[1];
            SlimeWorld world = AdvancedSlimeUtil.Instance.getLoadedWorld(worldName);

            String json = CompoundMapConvert.from(world.getPropertyMap().getProperties());

            System.out.println("original: "+ world.getPropertyMap().getProperties().values());
            System.out.println("toJson: "+ json);
            System.out.println("fromJson: "+ CompoundMapConvert.to(json).values());
            System.out.println("After: "+ new SlimePropertyMap(CompoundMapConvert.to(json)).getProperties().values());

            return false;
        }

        if (action.equalsIgnoreCase("load")) {
            if (args.length != 3) {
                sender.sendMessage("§cUsage: /realm-generator load <type> <name>");
                return false;
            }

            String typeLoader = args[1];
            String[] types = {"prototype", "realm"};

            if (!Arrays.asList(types).contains(typeLoader)) {
                sender.sendMessage("§cType Support: prototype/realm");
                return false;
            }

            String name = args[2];

            if(AdvancedSlimeUtil.loadWorld(typeLoader, name)) {
                sender.sendMessage("§aWorld " + name + " has loaded");
                return true;
            }

            sender.sendMessage("§cUnknown world: " + name);
            return false;
        }

        if (action.equalsIgnoreCase("unload")) {
            String worldName = args[1];
            Bukkit.unloadWorld(worldName, true);
            return true;
        }

        if (action.equalsIgnoreCase("force")) {
            if (args.length != 2) {
                sender.sendMessage("§cUsage: /realm-generator force <prototype-biome>");
                return false;
            }

            String prototypeBiome = args[1];

            try {
                long startTime = System.currentTimeMillis();
                sender.sendMessage("§6Generating prototype '" + prototypeBiome);
                GeneratorCore.createPrototype(prototypeBiome, 0, worldName -> {
                    long duration = System.currentTimeMillis() - startTime;
                    if (worldName != null) {
                        sender.sendMessage("§aSuccessfully created prototype '" + prototypeBiome + "' in " + duration + "ms.");
                        sender.sendMessage("§aWorld Name: " + worldName);
                    } else {
                        sender.sendMessage("§cFailed to create prototype '" + prototypeBiome + "' in " + duration + "ms.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        sender.sendMessage("§cUnknown Sub-Command");
        return false;
    }

    public static List<String> tab(CommandSender sender, String[] args) {

        if (args[0].equalsIgnoreCase("force")) {
            return Arrays.asList(GeneratorCore.prototypeList);
        }

        if (args[0].equalsIgnoreCase("load")) {
            if(args.length == 2) {
                return List.of("prototype", "realm");
            }
        }

        if (args[0].equalsIgnoreCase("read")) {
            return AdvancedSlimeUtil.Instance.getLoadedWorlds().stream().map(SlimeWorld::getName).toList();
        }

        if (args[0].equalsIgnoreCase("unload")) {
            List<String> worldNames = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worldNames.add(world.getName());
            }
            return worldNames;
        }

        return null;
    }
}
