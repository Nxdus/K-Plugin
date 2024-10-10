package org.nxdus.kenchanted;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.kenchanted.Commands.CustomEnchantCommand;
import org.nxdus.kenchanted.Listeners.EnchantTableListener;
import org.nxdus.kenchanted.Listeners.InventoryListener;
import org.nxdus.kenchanted.Listeners.PrepareAnvilListener;

public final class KEnchanted extends JavaPlugin {

    @Override
    public void onEnable() {

        RegHandler(this);
        RegCommands(this);

        getLogger().info("K-Enchanted Plugin Enabled !");
    }

    @Override
    public void onDisable() {
        getLogger().info("K-Enchanted Plugin Disabled !");
    }

    private void RegHandler(KEnchanted instance) {
        getServer().getPluginManager().registerEvents(new InventoryListener(), instance);
        getServer().getPluginManager().registerEvents(new EnchantTableListener(instance), instance);
        getServer().getPluginManager().registerEvents(new PrepareAnvilListener(), instance);
        getLogger().info("K-Enchanted Listener Registered !");
    }

    private void RegCommands(KEnchanted instance) {

        LifecycleEventManager<Plugin> commandsManger = instance.getLifecycleManager();

        commandsManger.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("custom-enchant", "", new CustomEnchantCommand());
        });

        getLogger().info("K-Enchanted Commands Registered !");
    }
}
