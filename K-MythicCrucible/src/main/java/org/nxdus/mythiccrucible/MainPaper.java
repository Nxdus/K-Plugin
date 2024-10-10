package org.nxdus.mythiccrucible;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainPaper extends JavaPlugin {

    private MainPaper plugin = this;
    public static Plugin mythicCruciblePlugin;


    @Override
    public void onEnable() {
        plugin = this;

        mythicCruciblePlugin = plugin.getServer().getPluginManager().getPlugin("MythicCrucible");
        if (mythicCruciblePlugin != null) {
            new PreventPickItems(this);
            // new PreventHackOp(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
