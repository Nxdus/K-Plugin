package org.nxdus.kwatcher;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class KWatcher extends JavaPlugin {

    @Override
    public void onEnable() {

        watcher();

        getLogger().info("KWatcher is enabled");
    }

    @Override
    public void onDisable() {

        getLogger().info("KWatcher is disabled");
    }

    Plugin KCore = Bukkit.getServer().getPluginManager().getPlugin("K-Core");

    private void watcher() {
        List<String> KGroups = List.of("K-Chat", "K-Realms");

        new BukkitRunnable() {
           @Override
           public void run() {
               if (KCore != null && !KCore.isEnabled()) {
                   getLogger().warning("Watcher : reloaded !");
                   KCore = Bukkit.getServer().getPluginManager().getPlugin("K-Core");
                   for (String KPlugin : KGroups) {
                       Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload " + KPlugin);
                   }
               }
           }
        }.runTaskTimer(this, 0, 20L);
    }
}
