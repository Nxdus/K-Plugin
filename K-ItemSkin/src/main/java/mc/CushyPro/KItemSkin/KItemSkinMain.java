package mc.CushyPro.KItemSkin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class KItemSkinMain extends JavaPlugin {

    private static KItemSkinMain instance;
    private StoreData config;

    public static KItemSkinMain getInstance() {
        return instance;
    }

    public StoreData getStoreConfig() {
        return config;
    }

    public static File getFile(String file) {
        return new File(instance.getDataFolder(), file);
    }

    @Override
    public void onEnable() {
        instance = this;
        LifecycleEventManager<Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register("k-itemskin", "used for commands", new CommandManager());
        });
        config = new StoreData();
        config.loadConfig();
    }

    @Override
    public void onDisable() {

    }


}
