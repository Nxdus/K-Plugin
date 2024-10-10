package org.nxdus.kcatch;

import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.kcatch.Listener.CatchEntityListener;
import org.nxdus.kcatch.Listener.RepairCancelListener;
import org.nxdus.kcatch.Listener.SummonEntityListener;
import org.nxdus.kcatch.Reciped.CustomRecipe;

public final class KCatch extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(new CatchEntityListener(), this);
        getServer().getPluginManager().registerEvents(new SummonEntityListener(), this);
        getServer().getPluginManager().registerEvents(new RepairCancelListener(), this);

        new CustomRecipe();

        getLogger().info("K-Catch Plugin Enabled !");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("K-Catch Plugin Disabled !");
    }
}
