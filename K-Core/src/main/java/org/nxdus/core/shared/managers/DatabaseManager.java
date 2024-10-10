package org.nxdus.core.shared.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;
    private String connectionParams;
    private String connectionAuthUser;
    private String connectionAuthPass;

    public DatabaseManager(ConfigManager configManager) {
        if (configManager == null) {
            throw new IllegalArgumentException("ConfigManager cannot be null");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        connectionParams = "jdbc:mysql://" + configManager.getConfigAsString("mysql.host")
                + ":" + configManager.getConfigAsString("mysql.port") + "/"
                + configManager.getConfigAsString("mysql.database")
                + "?autoReconnect=true&useSSL=false&serverTimezone=UTC";

        connectionAuthUser = configManager.getConfigAsString("mysql.username");
        connectionAuthPass = configManager.getConfigAsString("mysql.password");

        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(connectionParams, connectionAuthUser, connectionAuthPass);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close the database connection", e);
            }
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reconnect to the database", e);
        }

        return connection;
    }
}
