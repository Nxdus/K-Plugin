package org.nxdus.realms.paper.Realms.Utils;

import org.bukkit.Bukkit;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateServerSchedule {

    public UpdateServerSchedule() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("UPDATE realm_servers SET tps = ?, current_cpu_usage = ?, current_memory_usage = ? WHERE server_id = ?");
                    preparedStatement.setDouble(1, BigDecimal.valueOf(Bukkit.getServer().getTPS()[0]).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    preparedStatement.setDouble(2, BigDecimal.valueOf(getCurrentCPUUsage()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    preparedStatement.setDouble(3, BigDecimal.valueOf(getMemoryUsagePercent()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                    preparedStatement.setString(4, KCore.serverUUID.toString());
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 15000);
    }

    private double getCurrentCPUUsage() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mBeanServer.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (!list.isEmpty()) {
                Attribute att = (Attribute) list.getFirst();
                Double value = (Double) att.getValue();

                // usually takes a couple of seconds before we get real values
                if (value == -1.0) return 0;
                // returns a percentage value with 1 decimal point precision
                return (value * 1000) / 10.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getMemoryUsagePercent() {
        try {
            Runtime runtime = Runtime.getRuntime();
            double usedMemory = (runtime.totalMemory() - runtime.freeMemory());
            return ((usedMemory / 1073741824) / 22) * 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
