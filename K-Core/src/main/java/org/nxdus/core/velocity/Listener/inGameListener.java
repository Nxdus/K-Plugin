package org.nxdus.core.velocity.Listener;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import org.nxdus.core.velocity.KCoreVelocity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class inGameListener {

    @Subscribe
    public void onPlayerConnected(ServerConnectedEvent event) {

        try {
            PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement("UPDATE users SET in_game = ? WHERE uuid = ?");
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, event.getPlayer().getUniqueId().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "add");
        jsonObject.addProperty("player-unique-id", event.getPlayer().getUniqueId().toString());

        KCoreVelocity.redisManager.publish("players-online", gson.toJson(jsonObject));

    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {

        try {
            PreparedStatement preparedStatement = KCoreVelocity.databaseConnection.prepareStatement("UPDATE users SET in_game = ? WHERE uuid = ?");
            preparedStatement.setBoolean(1, false);
            preparedStatement.setString(2, event.getPlayer().getUniqueId().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "remove");
        jsonObject.addProperty("player-unique-id", event.getPlayer().getUniqueId().toString());

        KCoreVelocity.redisManager.publish("players-online", gson.toJson(jsonObject));

    }

}
