package org.nxdus.kmoney.Providers.Handler;

import org.nxdus.core.paper.KCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlEconomyHandler {

    private final Connection connection = KCore.databaseConnection;

    public boolean hasAccount(UUID uuid) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
        preparedStatement.setString(1, uuid.toString());

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next();
    }

    public boolean hasBalance(UUID uuid, double amount) throws SQLException {
        return getBalance(uuid) >= amount;
    }

    public boolean withdraw(UUID uuid, double amount) throws SQLException {
        return setBalance(uuid, getBalance(uuid) - amount);
    }

    public boolean deposit(UUID uuid, double amount) throws SQLException {
        return setBalance(uuid, getBalance(uuid) + amount);
    }

    public double getBalance(UUID uuid) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT users.balance FROM users WHERE uuid = ?");
        preparedStatement.setString(1, uuid.toString());

        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? resultSet.getDouble("balance") : 0.0;
    }

    public boolean setBalance(UUID uuid, double balance) throws SQLException {
        if (!hasAccount(uuid) || balance < 0) return false;

        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET balance = ? WHERE uuid = ?");
        preparedStatement.setDouble(1, balance);
        preparedStatement.setString(2, uuid.toString());

        return preparedStatement.executeUpdate() > 0;
    }

}
