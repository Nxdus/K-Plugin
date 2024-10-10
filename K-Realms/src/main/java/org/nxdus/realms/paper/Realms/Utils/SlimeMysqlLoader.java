package org.nxdus.realms.paper.Realms.Utils;

import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import com.infernalsuite.aswm.loaders.UpdatableLoader;
import org.bukkit.Bukkit;
import org.nxdus.core.paper.KCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SlimeMysqlLoader extends UpdatableLoader {

    private String Table;
    private final Logger LOGGER = LoggerFactory.getLogger(SlimeMysqlLoader.class);

    private int CURRENT_DB_VERSION = 1;

    private String CREATE_VERSIONING_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `database_version` (`id` INT NOT NULL AUTO_INCREMENT, `version` INT(11), PRIMARY KEY(id));";
    private String INSERT_VERSION_QUERY = "INSERT INTO `database_version` (`id`, `version`) VALUES (1, ?) ON DUPLICATE KEY UPDATE `id` = ?;";
    private String GET_VERSION_QUERY = "SELECT `version` FROM `database_version` WHERE `id` = 1;";


    private String ALTER_LOCKED_COLUMN_QUERY;
    private String CREATE_WORLDS_TABLE_QUERY;
    private String SELECT_WORLD_QUERY;
    private String UPDATE_WORLD_QUERY;
    private String DELETE_WORLD_QUERY;
    private String LIST_WORLDS_QUERY;


    public SlimeMysqlLoader(String table) throws SQLException {

        this.Table = table;

        this.ALTER_LOCKED_COLUMN_QUERY = "ALTER TABLE `" + this.Table + "` CHANGE COLUMN `locked` `locked` BIGINT NOT NULL DEFAULT 0;";
        this.CREATE_WORLDS_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `" + this.Table + "` (`id` INT NOT NULL AUTO_INCREMENT, `name` VARCHAR(255) UNIQUE, `locked` BIGINT, `world` MEDIUMBLOB, PRIMARY KEY(id));";
        this.SELECT_WORLD_QUERY = "SELECT `world` FROM `" + this.Table + "` WHERE `name` = ?;";
        this.UPDATE_WORLD_QUERY = "INSERT INTO `" + this.Table + "` (`name`, `world`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `world` = ?;";
        this.DELETE_WORLD_QUERY = "DELETE FROM `" + this.Table + "` WHERE `name` = ?;";
        this.LIST_WORLDS_QUERY = "SELECT `name` FROM `" + this.Table + "`;";


        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.CREATE_WORLDS_TABLE_QUERY)) {
            statement.execute();
        }

        // Create versioning table
        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.CREATE_VERSIONING_TABLE_QUERY)) {
            statement.execute();
        }

        Bukkit.getLogger().info("Registered: " + table);
    }

    @Override
    public void update() throws IOException, NewerDatabaseException {
        try {
            int version;

            try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.GET_VERSION_QUERY);
                 ResultSet set = statement.executeQuery()) {
                version = set.next() ? set.getInt(1) : -1;
            }

            if (version > CURRENT_DB_VERSION) {
                throw new NewerDatabaseException(CURRENT_DB_VERSION, version);
            }

            if (version < CURRENT_DB_VERSION) {
                LOGGER.warn("Your SWM MySQL database is outdated. The update process will start in 10 seconds.");
                LOGGER.warn("Note that this update might make your database incompatible with older SWM versions.");
                LOGGER.warn("Make sure no other servers with older SWM versions are using this database.");
                LOGGER.warn("Shut down the server to prevent your database from being updated.");

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException ignored) {
                    LOGGER.info("Update process aborted.");
                    return;
                }

                // Update to v1: alter locked column to store a long
                try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.ALTER_LOCKED_COLUMN_QUERY)) {
                    statement.executeUpdate();
                }

                // Insert/update database version table
                try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.INSERT_VERSION_QUERY)) {
                    statement.setInt(1, CURRENT_DB_VERSION);
                    statement.setInt(2, CURRENT_DB_VERSION);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.SELECT_WORLD_QUERY)) {
            statement.setString(1, worldName);
            ResultSet set = statement.executeQuery();

            if (!set.next()) {
                throw new UnknownWorldException(worldName);
            }

            return set.getBytes("world");
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean worldExists(String worldName) throws IOException {
        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.SELECT_WORLD_QUERY)) {
            statement.setString(1, worldName);
            ResultSet set = statement.executeQuery();

            return set.next();
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public List<String> listWorlds() throws IOException {
        List<String> worldList = new ArrayList<>();

        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.LIST_WORLDS_QUERY)) {
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                worldList.add(set.getString("name"));
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }

        return worldList;
    }

    @Override
    public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {

        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.UPDATE_WORLD_QUERY)) {
            statement.setString(1, worldName);
            statement.setBytes(2, serializedWorld);
            statement.setBytes(3, serializedWorld);
            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void deleteWorld(String worldName) throws IOException, UnknownWorldException {
        try (PreparedStatement statement = KCore.databaseConnection.prepareStatement(this.DELETE_WORLD_QUERY)) {
            statement.setString(1, worldName);

            if (statement.executeUpdate() == 0) {
                throw new UnknownWorldException(worldName);
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

}