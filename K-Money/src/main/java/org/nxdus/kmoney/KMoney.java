package org.nxdus.kmoney;

import org.bukkit.plugin.java.JavaPlugin;
import org.nxdus.kmoney.Commands.BalanceCommand;
import org.nxdus.kmoney.Commands.TopCommand;
import org.nxdus.kmoney.Commands.TransferCommand;
import org.nxdus.kmoney.Listener.ReceiverTransfers;
import org.nxdus.kmoney.Top.TopRunnable;
import org.nxdus.kmoney.Providers.HookProvider;

public final class KMoney extends JavaPlugin {

    @Override
    public void onEnable() {

        new HookProvider(this);

        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("transfer").setExecutor(new TransferCommand());
        getCommand("balance-top").setExecutor(new TopCommand());

        new ReceiverTransfers();
        new TopRunnable(this);

        getLogger().info("KMoney Plugin Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("KMoney Plugin Disabled");
    }
}
