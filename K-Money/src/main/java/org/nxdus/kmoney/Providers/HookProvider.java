package org.nxdus.kmoney.Providers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.nxdus.kmoney.KMoney;
import org.nxdus.kmoney.Placeholders.BalanceExpansion;

public class HookProvider {

    public static Economy economy;

    private final KMoney instance;

    public HookProvider(KMoney instance) {
        this.instance = instance;

        RegisterVaultHook();
    }

    private void RegisterVaultHook() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null && Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getServicesManager().register(
                    Economy.class,
                    new AdapterEconomyVaultAPI(instance),
                    instance,
                    ServicePriority.High
            );

            RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(Economy.class);

            if (rsp != null) economy = rsp.getProvider();

            new BalanceExpansion(instance).register();
        } else {
            instance.getLogger().warning("Vault is not enabled");
            Bukkit.getPluginManager().disablePlugin(instance);
        }
    }

}
