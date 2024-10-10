package org.nxdus.realms.paper.Realms.Subscribe;

import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.SlimeWorldInstance;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.nxdus.TeleportAPI.KTeleportAPI;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.realms.paper.Realms.Utils.AdvancedSlimeUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RealmsCreateSubscribe implements Listener {

    private final MainPaper instance;

    private final Map<String, String> createRealm = new HashMap<>();
    private final Map<String, String> loadRealm = new HashMap<>();
    private final Map<String, List<String>> resetRealm = new HashMap<>();

    public RealmsCreateSubscribe(MainPaper instance) {
        this.instance = instance;
        instance.getServer().getPluginManager().registerEvents(this, instance);
        subscribeToRedis();
    }

    private void subscribeToRedis() {
        KCore.redisManager.subscribe("realm-" + KCore.serverUUID.toString(), (channel, message) -> {
            try {
                processMessage(message);
            } catch (Exception e) {
                logError("Failed to process message", e);
            }
        });
    }

    private void processMessage(String message) {
        Gson gson = new Gson();
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
        String action = jsonMessage.get("action").getAsString();

        if ("create-realm".equals(action)) {
            createRealm.put(jsonMessage.get("player-uuid").getAsString(), jsonMessage.get("world-biome").getAsString());
        } else if ("load-realm".equals(action)) {
            loadRealm.put(jsonMessage.get("player-uuid").getAsString(), jsonMessage.get("world-name").getAsString());
        } else if ("reset-realm".equals(action)) {
            resetRealm.put(jsonMessage.get("player-uuid").getAsString(), List.of(jsonMessage.get("world-biome").getAsString(), jsonMessage.get("world-name").getAsString()));
        } else if ("update-realm-member".equals(action)) {
            handlerUpdateRealmMember(jsonMessage.get("world-name").getAsString());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUniqID = player.getUniqueId().toString();

        if (createRealm.containsKey(playerUniqID)) {
            handleCreateRealm(player, playerUniqID);
        } else if (loadRealm.containsKey(playerUniqID)) {
            handleLoadRealm(player, playerUniqID);
        } else if (resetRealm.containsKey(playerUniqID)) {
            handleResetRealm(player, playerUniqID);
        }
    }

    private void handleCreateRealm(Player player, String playerUniqID) {
        String worldBiome = createRealm.get(playerUniqID);
        String rename = UUID.randomUUID().toString();
        String prototypeWorld = AdvancedSlimeUtil.randomPrototype(worldBiome, rename);

        if (prototypeWorld == null) {
            player.sendMessage("§cPrototype " + worldBiome + " not found, Please generate it.");
            return;
        }

        String propertyMapJson = AdvancedSlimeUtil.getPropertyMapAsString(AdvancedSlimeUtil.getTable("prototype"), prototypeWorld);
        migrateWorldAsync(player, prototypeWorld, worldBiome, propertyMapJson, playerUniqID);
        createRealm.remove(playerUniqID);
    }

    private void handleCreateRealm(Player player, String worldBiome, String playerUniqID) {
        String rename = UUID.randomUUID().toString();
        String prototypeWorld = AdvancedSlimeUtil.randomPrototype(worldBiome, rename);

        if (prototypeWorld == null) {
            player.sendMessage("§cPrototype " + worldBiome + " not found, Please generate it.");
            return;
        }

        String propertyMapJson = AdvancedSlimeUtil.getPropertyMapAsString(AdvancedSlimeUtil.getTable("prototype"), prototypeWorld);
        migrateWorldAsync(player, prototypeWorld, worldBiome, propertyMapJson, playerUniqID);
    }

    private void handleLoadRealm(Player player, String playerUniqID) {
        String worldName = loadRealm.get(playerUniqID);
        if (Bukkit.getWorld(worldName) == null) {
            loadWorldAndTeleport(player, worldName, playerUniqID);
        }
        loadRealm.remove(playerUniqID);
    }

    private void handleResetRealm(Player player, String playerUniqID) {
        List<String> resetData = resetRealm.get(playerUniqID);
        String worldBiome = resetData.get(0);
        String worldName = resetData.get(1);

        String rswIDQuery = "SELECT id FROM realm_slime_world WHERE name = ? LIMIT 1";

        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(rswIDQuery)) {
            preparedStatement.setString(1, worldName);
            ResultSet rswIDResultSet = preparedStatement.executeQuery();

            if (rswIDResultSet.next()) {

                WeakReference<World> worldRef = new WeakReference<>(Bukkit.getWorld(worldName));

                if (worldRef.get() != null) {

                    for (Player inWorldPlayer : worldRef.get().getPlayers()) {
                        inWorldPlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
                    }

                    if (unloadWorld(worldName)) {
                        Runtime.getRuntime().gc();
                        System.out.println("Unload world successfully !!!!");
                    }
                }

                int rswID = rswIDResultSet.getInt("id");

                // ลบข้อมูลจากตาราง realms ที่เชื่อมโยงกับ realm_slime_world
                String realmsIDQuery = "SELECT id FROM realms WHERE world_id = ? LIMIT 1";
                try (PreparedStatement preparedStatement2 = KCore.databaseConnection.prepareStatement(realmsIDQuery)) {
                    preparedStatement2.setInt(1, rswID);
                    ResultSet realmsIDResultSet = preparedStatement2.executeQuery();

                    if (realmsIDResultSet.next()) {
                        int realmsID = realmsIDResultSet.getInt("id");

                        // ลบจาก realm_member
                        String realmMemberDeleteQuery = "DELETE FROM realm_member WHERE realm_id = ?";
                        try (PreparedStatement preparedStatement3 = KCore.databaseConnection.prepareStatement(realmMemberDeleteQuery)) {
                            preparedStatement3.setInt(1, realmsID);
                            int deleteRealmMembers = preparedStatement3.executeUpdate();
                            if (deleteRealmMembers > 0) {
                                System.out.println("Deleted realm members successfully!");
                            }
                        }

                        // ลบจาก realms
                        String realmsDeleteQuery = "DELETE FROM realms WHERE id = ?";
                        try (PreparedStatement preparedStatement4 = KCore.databaseConnection.prepareStatement(realmsDeleteQuery)) {
                            preparedStatement4.setInt(1, realmsID);
                            int deleteRealms = preparedStatement4.executeUpdate();
                            if (deleteRealms > 0) {
                                System.out.println("Deleted realms successfully!");
                            }
                        }
                    }
                }

                // ลบจาก realm_slime_world
                String realmSlimeWorldDeleteQuery = "DELETE FROM realm_slime_world WHERE id = ?";
                try (PreparedStatement preparedStatement5 = KCore.databaseConnection.prepareStatement(realmSlimeWorldDeleteQuery)) {
                    preparedStatement5.setInt(1, rswID);
                    int deleteRealmSlimeWorld = preparedStatement5.executeUpdate();
                    if (deleteRealmSlimeWorld > 0) {
                        System.out.println("Deleted realm_slime_world successfully!");
                    }
                }

                // ลบ realm ออกจาก S3
                AdvancedSlimeUtil.sqlLoaderRealm.deleteWorld(worldName);
            }
        } catch (SQLException | UnknownWorldException | IOException e) {
            e.printStackTrace();
        }

        resetRealm.remove(playerUniqID);
        handleCreateRealm(player, worldBiome, playerUniqID);
    }

    private boolean unloadWorld(String worldName) {
        WeakReference<World> world = new WeakReference<>(Bukkit.getWorld(worldName));

        if (world.get() == null) {
            System.out.println("World " + worldName + " does not exist or is already unloaded.");
            return false;
        }

        // Ensure no players are left in the world
        if (!world.get().getPlayers().isEmpty()) {
            System.out.println("Teleport player out of world !");
            world.get().getPlayers().forEach(p -> p.teleport(Bukkit.getWorld("world").getSpawnLocation()));
        }

        // Remove all entities safely before unloading
        if (!world.get().getEntities().isEmpty()) {
            System.out.println("Removed Entity");
            world.get().getEntities().forEach(Entity::remove);
        }

        Arrays.stream(world.get().getLoadedChunks())
                .forEach(chunk -> {
                    System.out.println("Unloaded chunk: " + chunk.getX() + ", " + chunk.getZ());
                    chunk.unload(false);
                });

        // Unload the world chunks and the world itself
        return instance.getServer().unloadWorld(world.get(), true);
    }

    private void handlerUpdateRealmMember(String prototypeWorld) {
        WeakReference<World> ownWorldInstance = new WeakReference<>(Bukkit.getWorld(prototypeWorld));

        try {
            getRegionManager(ownWorldInstance).getRegion("__global__").getMembers().clear();
            for (String memberName : getRealmMembers(prototypeWorld)) {
                addPlayerToRegion(ownWorldInstance, memberName, "__global__", false);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void migrateWorldAsync(Player ownPlayer, String prototypeWorld, String worldBiome, String propertyMapJson, String playerUniqID) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                AdvancedSlimeUtil.Instance.migrateWorld(prototypeWorld, AdvancedSlimeUtil.sqlLoaderPrototype, AdvancedSlimeUtil.sqlLoaderRealm);
                saveRealmToDatabase(prototypeWorld, propertyMapJson);
                ownPlayer.sendMessage("§aPrototype " + worldBiome + " has been migrated, " + prototypeWorld);
                loadWorldAndTeleport(ownPlayer, prototypeWorld, playerUniqID);
            } catch (Exception e) {
                logError("Failed to migrate world", e);
                ownPlayer.sendMessage("§cFailed to migrate world: " + e.getMessage());
            }
        });
    }

    private void saveRealmToDatabase(String prototypeWorld, String propertyMapJson) throws SQLException {
        String insertRealmQuery = "INSERT INTO `realm_slime_world` (`id`, `name`, `property`, `created_at`, `updated_at`) " +
                "VALUES (NULL, ?, ?, current_timestamp(), current_timestamp())";
        try (PreparedStatement insertRealmStmt = KCore.databaseConnection.prepareStatement(insertRealmQuery)) {
            insertRealmStmt.setString(1, prototypeWorld);
            insertRealmStmt.setString(2, propertyMapJson);
            insertRealmStmt.executeUpdate();
        }
    }

    private void loadWorldAndTeleport(Player ownPlayer, String prototypeWorld, String playerUniqID) {
        Bukkit.getScheduler().runTask(instance, () -> {
            try {
                if (AdvancedSlimeUtil.loadWorld("realm", prototypeWorld)) {
                    ownPlayer.sendMessage("§aWorld " + prototypeWorld + " has loaded");
                    JsonArray ownWorld = getWorldSpawnData(prototypeWorld);
                    if (ownWorld != null) {
                        int realmSize = getRealmSize(ownPlayer);
                        WeakReference<World> ownWorldInstance = new WeakReference<>(Bukkit.getWorld(prototypeWorld));

                        setUpWorld(ownPlayer, prototypeWorld, ownWorld, playerUniqID, realmSize, ownWorldInstance);
                    } else {
                        ownPlayer.sendMessage("§cFailed to load spawn data.");
                    }
                } else {
                    ownPlayer.sendMessage("§cUnknown world: " + prototypeWorld);
                }
            } catch (Exception e) {
                logError("Failed to load world", e);
                ownPlayer.sendMessage("§cFailed to load world: " + e.getMessage());
            }
        });
    }

    private void setUpWorld(Player ownPlayer, String prototypeWorld, JsonArray ownWorld, String playerUniqID, int realmSize, WeakReference<World> ownWorldInstance) throws SQLException {
        WorldBorder worldBorder = ownWorldInstance.get().getWorldBorder();
        worldBorder.setCenter(777, 777);
        worldBorder.setSize(realmSize);

        createGlobalRegionIfNotExists(ownWorldInstance);
        setWorldFlags(ownWorldInstance);

        getRegionManager(ownWorldInstance).getRegion("__global__").getOwners().clear();
        addPlayerToRegion(ownWorldInstance, ownPlayer.getName(), "__global__", true);

        getRegionManager(ownWorldInstance).getRegion("__global__").getMembers().clear();
        for (String memberName : getRealmMembers(prototypeWorld)) {
            addPlayerToRegion(ownWorldInstance, memberName, "__global__", false);
        }

        teleportPlayer(ownPlayer, ownWorld, prototypeWorld, playerUniqID);
        saveRealmOwner(prototypeWorld, playerUniqID);
    }

    private void setWorldFlags(WeakReference<World> ownWorldInstance) {
        setWorldFlag(ownWorldInstance, Flags.INTERACT, StateFlag.State.DENY);
        setWorldFlag(ownWorldInstance, Flags.BLOCK_BREAK, StateFlag.State.DENY);
        setWorldFlag(ownWorldInstance, Flags.BLOCK_PLACE, StateFlag.State.DENY);
    }

    public List<String> getRealmMembers(String prototypeWorld) throws SQLException {
        String query = "SELECT u.username FROM users u " +
                "JOIN realm_member rm ON u.id = rm.user_id " +
                "JOIN realms r ON rm.realm_id = r.id " +
                "JOIN realm_slime_world rsw ON r.world_id = rsw.id " +
                "WHERE rsw.name = ? AND rm.role = 'trusted'";

        List<String> memberUUIDs = new ArrayList<>();
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, prototypeWorld);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.println(resultSet.getString("username"));
                    memberUUIDs.add(resultSet.getString("username"));
                }
            }
        }
        return memberUUIDs;
    }

    public int getRealmSize(Player player) {
        int realmSize = -1;
        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
            String permission = permInfo.getPermission();
            if (permission.startsWith("realm.size.")) {
                realmSize = getLargestRealmSize(realmSize, permission);
            }
        }
        return realmSize;
    }

    private int getLargestRealmSize(int realmSize, String permission) {
        try {
            int size = Integer.parseInt(permission.substring(11));
            if (size == 350 || size == 500 || size == 700) {
                realmSize = Math.max(realmSize, size);
            }
        } catch (NumberFormatException e) {
            logError("Invalid permission format: " + permission, e);
        }
        return realmSize;
    }

    private void createGlobalRegionIfNotExists(WeakReference<World> world) {
        RegionManager regionManager = getRegionManager(world);

        if (regionManager != null) {
            ProtectedRegion globalRegion = regionManager.getRegion("__global__");
            if (globalRegion == null) {
                createGlobalRegion(world, regionManager);
            } else {
                System.out.println("Global region already exists in world: " + world.get().getName());
            }
        }
    }

    private void createGlobalRegion(WeakReference<World> world, RegionManager regionManager) {
        BlockVector3 min = BlockVector3.at(world.get().getWorldBorder().getCenter().getX() - world.get().getWorldBorder().getSize() / 2, 0, world.get().getWorldBorder().getCenter().getZ() - world.get().getWorldBorder().getSize() / 2);
        BlockVector3 max = BlockVector3.at(world.get().getWorldBorder().getCenter().getX() + world.get().getWorldBorder().getSize() / 2, world.get().getMaxHeight(), world.get().getWorldBorder().getCenter().getZ() + world.get().getWorldBorder().getSize() / 2);
        ProtectedRegion globalRegion = new ProtectedCuboidRegion("__global__", min, max);
        regionManager.addRegion(globalRegion);
        saveRegionChanges(regionManager);
    }

    private void saveRegionChanges(RegionManager regionManager) {
        try {
            regionManager.save();
        } catch (StorageException e) {
            logError("Failed to save region changes", e);
        }
    }

    private RegionManager getRegionManager(WeakReference<World> world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world.get()));
    }

    public void setWorldFlag(WeakReference<World> world, StateFlag flag, StateFlag.State state) {
        RegionManager regionManager = getRegionManager(world);

        if (regionManager != null) {
            ProtectedRegion globalRegion = regionManager.getRegion("__global__");
            if (globalRegion != null) {
                globalRegion.setFlag(flag, state);
                globalRegion.setFlag(flag.getRegionGroupFlag(), RegionGroup.NONE);
                saveRegionChanges(regionManager);
            }
        }
    }

    public void addPlayerToRegion(WeakReference<World> world, String playerName, String regionName, boolean isOwner) {
        RegionManager regionManager = getRegionManager(world);

        if (regionManager != null) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                addPlayerToRegionGroup(playerName, isOwner, region);
                saveRegionChanges(regionManager);
            }
        }
    }

    private void addPlayerToRegionGroup(String playerName, boolean isOwner, ProtectedRegion region) {

        if (isOwner) {
            region.getOwners().addPlayer(playerName);
        } else {
            region.getMembers().addPlayer(playerName);
        }
    }

    private JsonArray getWorldSpawnData(String prototypeWorld) {
        try {
            Object propertyMap = AdvancedSlimeUtil.getPropertyMap("realm_slime_world", prototypeWorld, true);
            return JsonParser.parseString(propertyMap.toString()).getAsJsonArray();
        } catch (Exception e) {
            logError("Error parsing spawn data", e);
            return null;
        }
    }

    private void saveRealmOwner(String prototypeWorld, String playerUniqID) throws SQLException {
        if (isAlreadyOwner(prototypeWorld, playerUniqID)) {
            System.out.println("Already owner!");
            return;
        }

        String insertRealmMemberQuery = "INSERT INTO `realm_member` (`realm_id`, `user_id`, `role`) " +
                "VALUES ((SELECT r.id FROM realms r WHERE r.world_id = (SELECT rsw.id FROM realm_slime_world rsw WHERE rsw.name = ? LIMIT 1) LIMIT 1), " +
                "(SELECT id FROM users WHERE uuid = ? LIMIT 1), 'owner')";
        try (PreparedStatement insertRealmMemberStmt = KCore.databaseConnection.prepareStatement(insertRealmMemberQuery)) {
            insertRealmMemberStmt.setString(1, prototypeWorld);
            insertRealmMemberStmt.setString(2, playerUniqID);
            insertRealmMemberStmt.executeUpdate();
        }
    }

    private boolean isAlreadyOwner(String prototypeWorld, String playerUniqID) throws SQLException {
        String checkQuery = "SELECT 1 FROM realm_member rm " +
                "JOIN realms r ON rm.realm_id = r.id " +
                "JOIN realm_slime_world rsw ON r.world_id = rsw.id " +
                "JOIN users u ON rm.user_id = u.id " +
                "WHERE rsw.name = ? AND u.uuid = ? AND rm.role = 'owner'";

        try (PreparedStatement checkOwnRealm = KCore.databaseConnection.prepareStatement(checkQuery)) {
            checkOwnRealm.setString(1, prototypeWorld);
            checkOwnRealm.setString(2, playerUniqID);
            try (ResultSet resultSet = checkOwnRealm.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void teleportPlayer(Player ownPlayer, JsonArray ownWorld, String prototypeWorld, String playerUniqID) {
        AtomicDouble spawnX = new AtomicDouble();
        AtomicDouble spawnY = new AtomicDouble();
        AtomicDouble spawnZ = new AtomicDouble();
        AtomicReference<Float> spawnYaw = new AtomicReference<>((float) 0);

        setSpawnCoordinates(ownWorld, spawnX, spawnY, spawnZ, spawnYaw);
        saveAndTeleportPlayer(ownPlayer, prototypeWorld, playerUniqID, spawnX, spawnY, spawnZ, spawnYaw);
    }

    private void setSpawnCoordinates(JsonArray ownWorld, AtomicDouble spawnX, AtomicDouble spawnY, AtomicDouble spawnZ, AtomicReference<Float> spawnYaw) {
        ownWorld.forEach(data -> {
            JsonObject initData = data.getAsJsonObject();
            String key = initData.get("key").getAsString();
            switch (key) {
                case "spawnX":
                    spawnX.set(initData.get("value").getAsDouble());
                    break;
                case "spawnY":
                    spawnY.set(initData.get("value").getAsDouble());
                    break;
                case "spawnZ":
                    spawnZ.set(initData.get("value").getAsDouble());
                    break;
                case "spawnYaw":
                    spawnYaw.set(initData.get("value").getAsFloat());
                    break;
            }
        });
    }

    private void saveAndTeleportPlayer(Player ownPlayer, String prototypeWorld, String playerUniqID, AtomicDouble spawnX, AtomicDouble spawnY, AtomicDouble spawnZ, AtomicReference<Float> spawnYaw) {
        try (PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement(
                "SELECT u.id AS user_id, w.id AS world_id, s.id AS server_id " +
                        "FROM users u " +
                        "JOIN realm_slime_world w ON w.name = ? " +
                        "JOIN realm_servers s ON s.server_id = ? " +
                        "WHERE u.uuid = ? LIMIT 1")) {

            preparedStatement.setString(1, prototypeWorld);
            preparedStatement.setString(2, KCore.serverUUID.toString());
            preparedStatement.setString(3, playerUniqID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int userID = resultSet.getInt("user_id");
                    int worldID = resultSet.getInt("world_id");
                    int serverID = resultSet.getInt("server_id");

                    if (isRealmEntryExists(playerUniqID, prototypeWorld)) {
                        updateRealmEntry(serverID, playerUniqID, prototypeWorld);
                    } else {
                        insertRealmEntry(userID, worldID, serverID, spawnX, spawnY, spawnZ, spawnYaw);
                    }

                    KTeleportAPI.teleportPlayerToWorld(ownPlayer, "realm-" + KCore.serverUUID.toString(), prototypeWorld, spawnX.get(), spawnY.get(), spawnZ.get(), spawnYaw.get(), 0.0F);
                }
            }
        } catch (SQLException e) {
            logError("Failed to save spawn data to database", e);
            ownPlayer.sendMessage("Failed to save spawn data to database.");
        }
    }

    private boolean isRealmEntryExists(String playerUniqID, String prototypeWorld) throws SQLException {
        String checkQuery = "SELECT id FROM realms WHERE user_id = (SELECT u.id FROM users u WHERE u.uuid = ?) " +
                "AND world_id = (SELECT w.id FROM realm_slime_world w WHERE w.name = ?) LIMIT 1";
        try (PreparedStatement checkStmt = KCore.databaseConnection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, playerUniqID);
            checkStmt.setString(2, prototypeWorld);

            try (ResultSet checkResultSet = checkStmt.executeQuery()) {
                return checkResultSet.next();
            }
        }
    }

    private void updateRealmEntry(int serverID, String playerUniqID, String prototypeWorld) throws SQLException {
        String updateQuery = "UPDATE realms SET server_id = ? WHERE user_id = (SELECT u.id FROM users u WHERE u.uuid = ?) " +
                "AND world_id = (SELECT w.id FROM realm_slime_world w WHERE w.name = ?) LIMIT 1";
        try (PreparedStatement updateStatement = KCore.databaseConnection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, serverID);
            updateStatement.setString(2, playerUniqID);
            updateStatement.setString(3, prototypeWorld);
            updateStatement.executeUpdate();
        }
    }

    private void insertRealmEntry(int userID, int worldID, int serverID, AtomicDouble spawnX, AtomicDouble spawnY, AtomicDouble spawnZ, AtomicReference<Float> spawnYaw) throws SQLException {
        String insertQuery = "INSERT INTO realms (user_id, world_id, server_id, spawn) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStatement = KCore.databaseConnection.prepareStatement(insertQuery)) {
            insertStatement.setInt(1, userID);
            insertStatement.setInt(2, worldID);
            insertStatement.setInt(3, serverID);
            insertStatement.setString(4, String.format("%f,%f,%f,%f", spawnX.get(), spawnY.get(), spawnZ.get(), spawnYaw.get()));
            insertStatement.executeUpdate();
        }
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }
}