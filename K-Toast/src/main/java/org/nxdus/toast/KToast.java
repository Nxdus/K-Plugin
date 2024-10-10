package org.nxdus.toast;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class KToast extends JavaPlugin {

    public static KToast instance;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("toast").setExecutor(new ToastCommand());
        getCommand("toast").setTabCompleter(new ToastCommand());
    }

    @Override
    public void onDisable() {}

    public static KToast getInstance() {
        return instance;
    }
}
