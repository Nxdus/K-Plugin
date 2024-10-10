package org.nxdus.realms.velocity.Subscribe;

import com.google.gson.JsonObject;
import org.nxdus.core.velocity.KCoreVelocity;
import org.nxdus.core.velocity.core.DynamicServerListener;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RealmManageServer {

    public static void RealmManageServerMessage(String action, JsonObject jsonMessage) {

        String serverId = jsonMessage.has("server-id") ? jsonMessage.get("server-id").getAsString() : "";
        String serverIp = jsonMessage.has("server-ip") ? jsonMessage.get("server-ip").getAsString() : "";
        int serverPort = jsonMessage.has("server-port") ? jsonMessage.get("server-port").getAsInt() : 0;
        int totalPlayers = jsonMessage.has("players") ? jsonMessage.get("players").getAsInt() : 0;

        try {
            if ("create-server-realm".equals(action)) {
                PreparedStatement SQL = KCoreVelocity.databaseConnection.prepareStatement("INSERT INTO `realm_servers`(`server_id`, `ip`, `port`) VALUES (?,?,?)");
                SQL.setString(1, serverId);
                SQL.setString(2, serverIp);
                SQL.setInt(3, serverPort);
                SQL.executeUpdate();
                DynamicServerListener.addServer("realm-" + serverId, serverIp, serverPort);
            } else if ("delete-server-realm".equals(action)) {
                PreparedStatement SQL = KCoreVelocity.databaseConnection.prepareStatement("DELETE FROM `realm_servers` WHERE `realm_servers`.`server_id`= ?");
                SQL.setString(1, serverId);
                SQL.executeUpdate();
                DynamicServerListener.removeServer("realm-" + serverId);
            } else if ("keep-alive".equals(action)) {
                PreparedStatement SQL = KCoreVelocity.databaseConnection.prepareStatement("UPDATE `realm_servers` SET `player` = ?, `updated_at` = now() WHERE `realm_servers`.`server_id` = ?");
                SQL.setInt(1, totalPlayers);
                SQL.setString(2, serverId);
                SQL.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
