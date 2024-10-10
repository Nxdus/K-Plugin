package org.nxdus.Teleport;

import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.Teleport.Subscribe.PaperSubscribe;
import org.nxdus.Teleport.Teleporter.SendWorldService;
import org.nxdus.Teleport.Teleporter.TeleportService;

public final class KTeleport extends JavaPlugin {

    @Override
    public void onEnable() {

        new PaperSubscribe();

        new SendWorldService();
        new TeleportService(this);

        getLogger().info("K-Teleport plugin enabled !");
    }

    @Override
    public void onDisable() {

        getLogger().info("K-Teleport plugin disabled !");
    }
}
