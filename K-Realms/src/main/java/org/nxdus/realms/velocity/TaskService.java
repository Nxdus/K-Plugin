package org.nxdus.realms.velocity;

import org.nxdus.core.velocity.KCoreVelocity;
import org.nxdus.core.velocity.core.BootstrapVelocity;
import org.nxdus.core.velocity.core.DynamicServerListener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;

public class TaskService {
    // ลบเซิร์ฟเวอร์ที่ไม่ได้ Keep Alive เกิน 15วินาที

    public TaskService() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Retrieve the IDs of servers to be deleted
                    String selectQuery = "SELECT server_id FROM realm_servers WHERE updated_at < NOW() - INTERVAL 15 SECOND";
                    PreparedStatement selectSQL = BootstrapVelocity.getConnection().prepareStatement(selectQuery);
                    ResultSet resultSet = selectSQL.executeQuery();

                    // Iterate over the result set and remove each server
                    while (resultSet.next()) {
                        String serverId = resultSet.getString("server_id");
                        DynamicServerListener.removeServer("realm-" + serverId);
                    }

                    // Delete the servers from the database
                    String deleteQuery = "DELETE FROM realm_servers WHERE updated_at < NOW() - INTERVAL 15 SECOND";
                    PreparedStatement deleteSQL = BootstrapVelocity.getConnection().prepareStatement(deleteQuery);
                    int execute = deleteSQL.executeUpdate();

                    KCoreVelocity.logger.info("@ Checked realm-server availability, {} records deleted", execute);
                } catch (Exception e) {
                    System.err.println("@ Failed to Keep-Alive: " + e.getMessage());
                }

                return;
            }
        }, 0, 15000);
    }

}
