package org.nxdus.realms.paper.Realms.Utils;

import com.infernalsuite.aswm.api.AdvancedSlimePaperAPI;
import com.infernalsuite.aswm.api.exceptions.*;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimeProperties;
import com.infernalsuite.aswm.api.world.properties.SlimeProperty;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.nxdus.core.paper.KCore;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdvancedSlimeUtil {

    public static AdvancedSlimePaperAPI Instance = AdvancedSlimePaperAPI.instance();
    public static SlimeLoader sqlLoaderPrototype; // init by onEnable
    public static SlimeLoader sqlLoaderRealm; // init by onEnable

    public static void register() {
        try {
            AdvancedSlimeUtil.sqlLoaderPrototype = new SlimeMysqlLoader(getTable("prototype"));
            AdvancedSlimeUtil.sqlLoaderRealm = new SlimeS3Loader();

            Bukkit.getLogger().info("Registered SlimePrototypeMysqlLoader / SlimeRealmMysqlLoader");
            Bukkit.getLogger().info(sqlLoaderRealm.toString());
            Bukkit.getLogger().info(sqlLoaderPrototype.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }

    public static String getTable(String type) {
        String table;

        if (type.equals("prototype")) {
            table = "realm_slime_world_prototype";
        } else if (type.equals("realm")) {
            table = "realm_slime_world";
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }

        return table;
    }

    public static boolean loadWorld(String typeLoader, String name) {

        try {

            SlimeLoader loader;

            if (typeLoader.equals("prototype")) {
                loader = AdvancedSlimeUtil.sqlLoaderPrototype;
            } else if (typeLoader.equals("realm")) {
                loader = AdvancedSlimeUtil.sqlLoaderRealm;
            } else {
                return false;
            }

            return loadWorld(loader, name, AdvancedSlimeUtil.getPropertyMap(getTable(typeLoader), name));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean loadWorld(SlimeLoader loader, String name, SlimePropertyMap slimePropertyMap) {

        try {
            WeakReference<SlimeWorld> world = new WeakReference<>(Instance.readWorld(loader, name, false, slimePropertyMap));
            Instance.loadWorld(world.get(), true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static SlimePropertyMap SlimePropertyMapUtil(int x, int y, int z, float yaw) {
        SlimePropertyMap properties = new SlimePropertyMap();

        properties.setValue(SlimeProperties.DIFFICULTY, "normal");
        properties.setValue(SlimeProperties.SPAWN_X, x);
        properties.setValue(SlimeProperties.SPAWN_Y, y);
        properties.setValue(SlimeProperties.SPAWN_Z, z);
        properties.setValue(SlimeProperties.SPAWN_YAW, yaw);
        properties.setValue(SlimeProperties.ALLOW_ANIMALS, true);
        properties.setValue(SlimeProperties.ALLOW_MONSTERS, true);
        properties.setValue(SlimeProperties.DRAGON_BATTLE, false);
        properties.setValue(SlimeProperties.PVP, true);
        properties.setValue(SlimeProperties.ENVIRONMENT, "normal");
        properties.setValue(SlimeProperties.WORLD_TYPE, "DEFAULT");

        return properties;
    }

    public static SlimePropertyMap getPropertyMap(String table, String worldName) {
        return (SlimePropertyMap) getPropertyMap(table, worldName, false);
    }

    public static String getPropertyMapAsString(String table, String worldName) {
        return (String) getPropertyMap(table, worldName, true);
    }

    public static Object getPropertyMap(String table, String worldName, boolean rawJson) {
        String propertyJson = "{}";
        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement("SELECT `property` FROM `" + table + "` WHERE `name` = ?;")) {
            statement.setString(1, worldName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    propertyJson = resultSet.getString("property");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (rawJson) {
            return propertyJson;  // Return raw JSON string if rawJson is true
        }

        return new SlimePropertyMap(CompoundMapConvert.to(propertyJson));  // Otherwise, return SlimePropertyMap
    }


    public static String randomPrototype(String type, String... rename) {
        String name = null;

        String selectQuery = "SELECT id, name, property FROM realm_slime_world_prototype WHERE status = 'ready' AND type = ? ORDER BY RAND() LIMIT 1";
        String updateQuery = rename.length > 0
                ? "UPDATE realm_slime_world_prototype SET status = 'used', name = ? WHERE id = ?"
                : "UPDATE realm_slime_world_prototype SET status = 'used' WHERE id = ?";

        try (PreparedStatement selectStatement = KCore.databaseConnection.prepareStatement(selectQuery)) {
            selectStatement.setString(1, type);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    name = (rename.length > 0) ? rename[0] : resultSet.getString("name");

                    // อัปเดต status เป็น 'used'
                    try (PreparedStatement updateStatement = KCore.databaseConnection.prepareStatement(updateQuery)) {
                        if (rename.length > 0) {
                            updateStatement.setString(1, rename[0]);
                            updateStatement.setInt(2, id);
                        } else {
                            updateStatement.setInt(1, id);
                        }
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }


}
