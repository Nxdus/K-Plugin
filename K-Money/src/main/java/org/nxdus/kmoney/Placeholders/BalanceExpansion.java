package org.nxdus.kmoney.Placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nxdus.kmoney.KMoney;
import org.nxdus.kmoney.Providers.HookProvider;

public class BalanceExpansion extends PlaceholderExpansion {

    private final KMoney instance;

    public BalanceExpansion(KMoney instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return instance.getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return instance.getDescription().getName();
    }

    @Override
    public @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.equalsIgnoreCase("balance")) {
            return String.valueOf(HookProvider.economy.format(HookProvider.economy.getBalance(player)));
        }

        return null;
    }
}
