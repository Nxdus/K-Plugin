package org.nxdus.kmoney.Providers;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.nxdus.kmoney.KMoney;
import org.nxdus.kmoney.Providers.Handler.SqlEconomyHandler;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class AdapterEconomyVaultAPI implements Economy {

    private final KMoney instance;
    private final SqlEconomyHandler sqlEconomyHandler = new SqlEconomyHandler();

    public AdapterEconomyVaultAPI(KMoney instance) {
        this.instance = instance;
    }

    @Override
    public boolean isEnabled() {
        return instance.isEnabled();
    }

    @Override
    public String getName() {
        return instance.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double v) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(true);
        format.setRoundingMode(RoundingMode.HALF_EVEN);

        return format.format(v);
    }

    @Override
    public String currencyNamePlural() {
        return "$";
    }

    @Override
    public String currencyNameSingular() {
        return "$";
    }

    @Override
    public boolean hasAccount(String s) {
        return hasAccount(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        try {
            return sqlEconomyHandler.hasAccount(offlinePlayer.getUniqueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return hasAccount(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String s) {
        return getBalance(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        try {
            return sqlEconomyHandler.getBalance(offlinePlayer.getUniqueId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String s, double v) {
        return has(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        try {
            return sqlEconomyHandler.hasBalance(offlinePlayer.getUniqueId(), v);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(Bukkit.getOfflinePlayer(s1), v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return has(offlinePlayer, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        try {
            return sqlEconomyHandler.withdraw(offlinePlayer.getUniqueId(), v)
                    ? new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Successful")
                    : new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Failed to withdraw");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return depositPlayer(Bukkit.getOfflinePlayer(s), v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        try {
            return sqlEconomyHandler.deposit(offlinePlayer.getUniqueId(), v)
                    ? new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Successful")
                    : new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Failed to deposit");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(Bukkit.getOfflinePlayer(s1), v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
