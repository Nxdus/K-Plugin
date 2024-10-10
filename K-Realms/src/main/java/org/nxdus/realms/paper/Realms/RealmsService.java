package org.nxdus.realms.paper.Realms;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.core.shared.managers.ConfigManager;
import org.nxdus.realms.paper.Realms.Generator.GeneratorCommand;
import org.nxdus.realms.paper.Realms.Generator.GeneratorCore;
import org.nxdus.realms.paper.Realms.Listener.RealmsUnloadSchedule;
import org.nxdus.realms.paper.Realms.Listener.inRealmsListener;
import org.nxdus.realms.paper.Realms.Subscribe.RealmsCreateSubscribe;
import org.nxdus.realms.paper.Realms.Subscribe.RealmsInvitedSubscribe;
import org.nxdus.realms.paper.Realms.Utils.UpdateServerSchedule;
import org.nxdus.realms.paper.Realms.Utils.WorldBorderPearlBlock;

public class RealmsService implements Listener {
    private final MainPaper plugin;

    public RealmsService(MainPaper plugin) {
        this.plugin = plugin;
        plugin.getLogger().info(ChatColor.GREEN + "Realms Service Loaded");
        registerHandler();

        if (KCore.configManager.getType() == ConfigManager.ConfigType.GENERATOR) {
            new GeneratorCore(plugin);
        }

        if (KCore.configManager.getType() == ConfigManager.ConfigType.REALM) {
            new RealmsCreateSubscribe(plugin);
            new RealmsUnloadSchedule(plugin);
            new inRealmsListener(plugin);
            new UpdateServerSchedule();
        }

        new WorldBorderPearlBlock(plugin);
        new RealmsInvitedSubscribe();
    }

    private void registerHandler() {
        //plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("realms").setExecutor(new RealmsCommand(this.plugin));
        plugin.getCommand("realm-generator").setExecutor(new GeneratorCommand(this.plugin));
    }



}
