package org.nxdus.realms.paper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.core.paper.KCore;
import org.nxdus.core.paper.subscribe.PaperSubscribe;
import org.nxdus.realms.paper.ArmorHidden.ArmorToggleService;
import org.nxdus.realms.paper.Helper.GeneralCommandService;
import org.nxdus.realms.paper.Realms.RealmsService;
import org.nxdus.realms.paper.Realms.Listener.RealmsUnloadSchedule;
import org.nxdus.realms.paper.Realms.Utils.AdvancedSlimeUtil;
import org.nxdus.realms.paper.General.DogDontAttackWhiteService;
import org.nxdus.realms.paper.General.OnlyBuilderCanBuildService;
import org.nxdus.realms.paper.General.ShowArmArmorStandService;
import org.nxdus.core.shared.managers.ConfigManager;

import java.util.Timer;
import java.util.TimerTask;

public final class MainPaper extends JavaPlugin {
    public static MainPaper plugin;

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {

        plugin = this;

        // Main Services
        new GeneralCommandService(this);
        new RealmsService(this);
        new ForceLastLocation(this);

        // Others Services
        new ShowArmArmorStandService(this);
        new DogDontAttackWhiteService(this);

        if (KCore.configManager.getType() == ConfigManager.ConfigType.SPAWN) {
            new OnlyBuilderCanBuildService(this);
        }

        // ProtocolLib Services
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            protocolManager = ProtocolLibrary.getProtocolManager();

            new ArmorToggleService(this, protocolManager);
            // new SitPacketService(this, protocolManager);
        }

        if (getServer().getPluginManager().isPluginEnabled("SlimeWorldPlugin")) {
            getLogger().info("Using SlimeWorldPlugin <3");
            AdvancedSlimeUtil.register();
        }

        if (KCore.configManager.getType() == ConfigManager.ConfigType.REALM) {
            String json = PaperSubscribe.buildJson("create-server-realm", KCore.serverUUID);
            KCore.redisManager.publish("velocity", json);
            plugin.getLogger().info("[K-Realms] Send Velocity w/ " + json);

            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    String json = PaperSubscribe.buildJson("keep-alive", KCore.serverUUID);
                    KCore.redisManager.publish("velocity", json);
                }
            }, 0, 10000);
        }

    }

    @Override
    public void onDisable() {

        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
            getLogger().info("Remove ProtocolLib Listeners <3");
        }


        if (KCore.configManager.getType() != null && KCore.configManager.getType() == ConfigManager.ConfigType.REALM) {

            RealmsUnloadSchedule.onServerShutdown();

            String json = PaperSubscribe.buildJson("delete-server-realm", KCore.serverUUID);
            KCore.redisManager.publish("velocity", json);
            plugin.getLogger().info("[K-Realms] Send Velocity w/ " + json);
        }

    }

}
