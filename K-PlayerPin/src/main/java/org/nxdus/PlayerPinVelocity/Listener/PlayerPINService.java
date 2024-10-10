package org.nxdus.PlayerPinVelocity.Listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.nxdus.PlayerPinVelocity.KPlayerPin;
import org.nxdus.PlayerPinVelocity.Subscribe.MessageSender;
import org.nxdus.core.velocity.KCoreVelocity;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerPINService {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final KPlayerPin plugin;


    public PlayerPINService(ProxyServer proxyServer, Logger logger, KPlayerPin plugin) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.plugin = plugin;

        clearSessionLogin();
    }

    private void clearSessionLogin() {
        proxyServer.getScheduler().buildTask(plugin, () -> {

            try (PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement(
                    "SELECT id FROM users_session_login WHERE create_at < NOW() - INTERVAL 1 HOUR"
            )) {

                preparedStatement.execute();
                ResultSet resultSet = preparedStatement.getResultSet();

                List<Integer> idsToDelete = new ArrayList<>();

                while (resultSet.next()) {
                    idsToDelete.add(resultSet.getInt("id"));
                }

                if (!idsToDelete.isEmpty()) {
                    PreparedStatement deleteStatement = KCoreVelocity.databaseConnection.prepareStatement("DELETE FROM users_session_login WHERE id = ?");

                    for (Integer id : idsToDelete) {
                        deleteStatement.setInt(1, id);
                        deleteStatement.addBatch();
                        logger.warn("@ Deleting session login: {}", id);
                    }

                    deleteStatement.executeBatch();

                } else {
                    logger.warn("@ No login found");
                }

            } catch (SQLException e) { throw new RuntimeException(e); }


            logger.warn("@ Clearing session login");
        }).repeat(10L, TimeUnit.SECONDS).schedule();
    }

    @Subscribe(order = PostOrder.FIRST)
    private void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer server = event.getServer();

        String subAction = getPlayerPIN(player) == null ? "create-player-pin" : "get-player-pin";
        MessageSender.sendPlayerPinService(server, subAction , player.getUsername(), getPlayerPIN(player), getPlayerSession(player));
    }

    private String getPlayerPIN(Player player) {

        try (PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement("SELECT users.pin FROM users WHERE uuid = ?");) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

            if (resultSet.next()) {
                return resultSet.getString("pin");
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return null;
    }

    public static void updatePlayerPIN(String UUID, String playerPIN) {
        try (PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement("UPDATE users SET pin = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, playerPIN);
            preparedStatement.setString(2, UUID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private boolean getPlayerSession(Player player) {

        try (PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement("SELECT * FROM users_session_login WHERE player_uuid = ?");) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return false;
    }

    public static void addPlayerSession(String UUID) {
        try (PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement("INSERT INTO users_session_login (player_uuid) VALUES (?)")) {
            preparedStatement.setString(1, UUID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
