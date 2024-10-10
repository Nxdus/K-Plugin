package org.nxdus.kmoney.Top;

import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.core.paper.KCore;
import org.nxdus.kmoney.KMoney;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TopRunnable {

    private static KMoney instance;

    private static final List<PlayerBalanceSnapshot> topList = new ArrayList<>(Collections.emptyList());

    public TopRunnable(KMoney instance) {
        TopRunnable.instance = instance;

        runnableTopList();
    }

    private void runnableTopList() {
        new BukkitRunnable() {
            public void run() {
                try {

                    topList.clear();

                    PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("SELECT * FROM users");
                    ResultSet resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {

                        PlayerBalanceSnapshot snapshot = new PlayerBalanceSnapshot(
                          resultSet.getString("username"), resultSet.getDouble("balance")
                        );

                        topList.add(snapshot);
                    }

                    topList.sort((o1, o2) -> Double.compare(o2.balance(), o1.balance()));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskTimer(instance, 0, 20*60*60);
    }

    public static List<PlayerBalanceSnapshot> getTopList() {
        return topList;
    }
}
