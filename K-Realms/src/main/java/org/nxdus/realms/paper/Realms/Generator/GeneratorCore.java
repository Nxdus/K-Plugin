package org.nxdus.realms.paper.Realms.Generator;

import com.fastasyncworldedit.core.util.TaskManager;
import com.infernalsuite.aswm.api.exceptions.UnknownWorldException;
import com.infernalsuite.aswm.api.exceptions.WorldAlreadyExistsException;
import com.infernalsuite.aswm.api.world.SlimeWorld;
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.realms.paper.Realms.Utils.AdvancedSlimeUtil;
import org.nxdus.realms.paper.Realms.Utils.CompoundMapConvert;
import org.nxdus.realms.paper.Realms.Utils.CopyPasteUtil;
import org.nxdus.realms.paper.Realms.Utils.RandomTeleportUtil;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GeneratorCore {

    private final MainPaper plugin;
    private static final AtomicBoolean runningGenerator = new AtomicBoolean(false);
    public final static String[] prototypeList = {"arid", "lush", "snowy", "tropical", "darkness", "sweet"};
    private static final boolean unloadAfterFinish = true;

    public GeneratorCore(MainPaper plugin) {
        this.plugin = plugin;

        registerHandler();
    }

    private void registerHandler() {
        plugin.getLogger().info(ChatColor.GREEN + "Realms Generator Service Loaded");
         StartGenerator();
    }

    public static void StartGenerator() {
        runningGenerator.set(true);
        loopFunction(0);
    }

    public static void StopGenerator() {
        runningGenerator.set(false);
    }

    public static void loopFunction(int i) {

        if (!runningGenerator.get()) {
            Bukkit.getLogger().warning("Realms Generator Service Not Running !!");
            Bukkit.getLogger().warning("Realms Generator Service Not Running !!");
            Bukkit.getLogger().warning("Realms Generator Service Not Running !!");
            return;
        }

        if (i >= prototypeList.length) {
            Bukkit.getServer().shutdown();
            //loopFunction(0);
            return;
        }

        String prototype = prototypeList[i];

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!runningGenerator.get()) {
                    Bukkit.getLogger().warning("Realms Generator Service Not Running !!");
                    Bukkit.getLogger().warning("Realms Generator Service Not Running !!");
                    Bukkit.getLogger().warning("Realms Generator Service Not Running !!");
                    return;
                }
                System.out.println("Prototype: " + prototypeList[i] + ", i = " + i);
                GeneratorCore.createPrototype(prototype, i, worldName -> {
                    if(worldName != null) {
                        loopFunction(i + 1);
                    }
                });
            }
        }.runTaskLater(MainPaper.plugin, 100L);
    }

    public static void createPrototype(String prototype, int id, Consumer<String> callback) {

        String worldName = "prototype-" + prototype + "-" + UUID.randomUUID();
        String worldOriginName = "world_manow_" + prototype;

        MainPaper.plugin.getLogger().info("§6[" + id + "] Generating world with prototype: " + prototype);

        int radius = KCore.settings.getNumber("realms.prototype.default.size").intValue();
        int centerX = KCore.settings.getNumber("realms.prototype.default.center.x").intValue();
        int centerZ = KCore.settings.getNumber("realms.prototype.default.center.z").intValue();

        try {
            MainPaper.plugin.getLogger().info("§6[" + id + "] Generating world '" + prototype + "' with size " + radius + ".");
            GeneratorCore.createPrototype(worldOriginName, worldName, radius, centerX, centerZ, success -> {
                if (success) {
                    try {
                        PreparedStatement statement = KCore.databaseConnection.prepareStatement("UPDATE `realm_slime_world_prototype` SET `status` = ?, `type` = ? WHERE `realm_slime_world_prototype`.`name` = ?");
                        statement.setString(1, "ready");
                        statement.setString(2, prototype);
                        statement.setString(3, worldName);
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    callback.accept(worldName);
                } else {
                    callback.accept(null);
                }
            });
        } catch (GeneratorCore.ErrorCodeException e) {
            MainPaper.plugin.getLogger().info("§cError: " + e.getErrorCode().getMessage() + " (Code: " + e.getErrorCode().getCode() + ")");
            callback.accept(null);
        }

    }

    public static void createPrototype(String worldOriginalName, String worldName, int radius, int centerX, int centerZ, Consumer<Boolean> callback) throws ErrorCodeException {
        World worldOriginal = Bukkit.getWorld(worldOriginalName);

        if (worldOriginal == null) {
            throw new ErrorCodeException(ErrorCode.ORIGINAL_WORLD_NOT_FOUND);
        }

        SlimeWorld slimeWorld;
        Location location = RandomTeleportUtil.findRandomSafeLocation(worldOriginal);

        System.out.println(location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());

        SlimePropertyMap slimePropertyMap = AdvancedSlimeUtil.SlimePropertyMapUtil(centerX, location.getBlockY(), centerZ, 0);

        String slimePropertyMapJson = CompoundMapConvert.from(slimePropertyMap.getProperties());

        try {
            slimeWorld = AdvancedSlimeUtil.Instance.createEmptyWorld(worldName, false, slimePropertyMap, AdvancedSlimeUtil.sqlLoaderPrototype);
            AdvancedSlimeUtil.Instance.loadWorld(slimeWorld, true);
            AdvancedSlimeUtil.Instance.saveWorld(slimeWorld);

            PreparedStatement statement = KCore.databaseConnection.prepareStatement("UPDATE realm_slime_world_prototype SET property = ? WHERE name = ?");
            statement.setString(1, slimePropertyMapJson);
            statement.setString(2, worldName);
            statement.executeUpdate();
        } catch (IOException | SQLException e) {
            throw new ErrorCodeException(ErrorCode.UNKNOWN);
        }

        World worldToCopy = Bukkit.getWorld(worldName);
        if (worldToCopy == null) {
            throw new ErrorCodeException(ErrorCode.WORLD_CANNOT_LOADED);
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleportAsync(worldToCopy.getSpawnLocation());
        });

        com.sk89q.worldedit.world.World worldOriginalAdapt = BukkitAdapter.adapt(worldOriginal);
        com.sk89q.worldedit.world.World worldToCopyAdapt = BukkitAdapter.adapt(worldToCopy);

        TaskManager.taskManager().async(() -> {
            long startTime = System.currentTimeMillis();
            CopyPasteUtil.useAsync(worldOriginalAdapt, worldToCopyAdapt, radius, location.getBlockX(), location.getBlockY(), location.getBlockZ(), centerX, location.getBlockY(), centerZ)
                    .thenAccept((success) -> {
                        long duration = System.currentTimeMillis() - startTime;
                        if (success) {
                            try {

                                Bukkit.getScheduler().runTask(MainPaper.plugin, () -> {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
                                });

                                AdvancedSlimeUtil.Instance.saveWorld(slimeWorld);
                                System.out.println("Saved Stage 1");
                                Thread.sleep(3000L);
                                AdvancedSlimeUtil.Instance.saveWorld(slimeWorld);
                                System.out.println("Saved Stage 2");
                                Thread.sleep(2000L);
                                AdvancedSlimeUtil.Instance.saveWorld(slimeWorld);
                                System.out.println("Saved Stage 3");
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                                callback.accept(false);
                                return;
                            }

                            if (unloadAfterFinish) {
                                new BukkitRunnable() {
                                    public void run() {
                                        MainPaper.plugin.getServer().unloadWorld(worldName, true);
                                    }
                                }.runTask(MainPaper.plugin);
                            }
                            Bukkit.getServer().getLogger().info("§aSuccessfully created world '" + worldName + "' in " + duration + "ms. w/ unload");
                            callback.accept(true);
                            return;
                        }
                        callback.accept(false);

                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        callback.accept(false);
                        return null;
                    });
        });
    }


    public enum ErrorCode {
        UNKNOWN(1000, "Unknown"),
        WORLD_ALREADY(1001, "World already exists"),
        ORIGINAL_WORLD_NOT_FOUND(1002, "Original World not found"),
        WORLD_CANNOT_LOADED(1003, "World could not be loaded"),
        FAILED_TO_COPY_PASTE(1004, "Failed to copy and paste the region into the world");

        private final int code;
        private final String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public static ErrorCode fromCode(int code) {
            for (ErrorCode errorCode : ErrorCode.values()) {
                if (errorCode.getCode() == code) {
                    return errorCode;
                }
            }
            return null; // or throw an exception
        }
    }

    public static class ErrorCodeException extends Exception {
        private final ErrorCode errorCode;

        public ErrorCodeException(ErrorCode errorCode) {
            super(errorCode.getMessage());
            this.errorCode = errorCode;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }
    }
}