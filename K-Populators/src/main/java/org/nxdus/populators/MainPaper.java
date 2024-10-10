package org.nxdus.populators;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class MainPaper extends JavaPlugin implements Listener {

    public static MainPaper plugin;

    private final Random random = new Random();

    private static ConfigManager configManager;

    // ไว้ทำในอนาคต Generate ตามถ้ำ

    @Override
    public void onEnable() {
        plugin = this;

        configManager = new ConfigManager(this);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        getCommand("populates").setExecutor(new Command());

        plugin.getLogger().info(ChatColor.GREEN + "Populates Generator has enabled");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();

        ConfigurationSection settingsSection = configManager.getSection("settings");

        if (settingsSection != null) {
            for (String key : settingsSection.getKeys(false)) {
                ConfigurationSection flowerSection = settingsSection.getConfigurationSection(key);
                if (flowerSection != null) {
                    List<String> worlds = flowerSection.getStringList("worlds");

                    if (event.isNewChunk()) {

                        worlds.forEach(worldName -> {
                            if (worldName.equalsIgnoreCase(world.getName())) {
                                Chunk chunk = event.getChunk();

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Hacker(chunk);
                                    }
                                }.run();

                            }
                        });
                    }
                }
            }
        }
    }

    public void Hacker(Chunk chunk) {

        ConfigurationSection settingsSection = configManager.getSection("settings");

        if (settingsSection == null) {
            return; // ไม่มีการตั้งค่า
        }

        World world = chunk.getWorld();
        int chunkX = chunk.getX() * 16;
        int chunkZ = chunk.getZ() * 16;

        // สร้าง thread แยกสำหรับการคำนวณ
        new BukkitRunnable() {
            @Override
            public void run() {

                for (String key : settingsSection.getKeys(false)) {
                    ConfigurationSection flowerSection = settingsSection.getConfigurationSection(key);
                    if (flowerSection == null) {
                        continue; // ข้ามไปถ้าไม่มีการตั้งค่า
                    }

                    List<String> oraxenIds = flowerSection.getStringList("oraxenIds");
                    int amount = flowerSection.getInt("amount");
                    int radius = flowerSection.getInt("radius");
                    int chance = flowerSection.getInt("chance");
                    int minHeight = flowerSection.getInt("min_height");
                    int maxHeight = flowerSection.getInt("max_height");

                    Set<String> bottomBlocks = new HashSet<>(flowerSection.getStringList("bottom_blocks"));
                    Set<String> replaceableBlocks = new HashSet<>(flowerSection.getStringList("replaceable_blocks"));
                    Set<String> biomes = new HashSet<>(flowerSection.getStringList("biomes"));

                    if (!rollDiceWithChance(chance)) {
                        continue; // หากการทอยไม่ผ่านก็ข้ามไป
                    }

                    Set<String> usedLocations = new HashSet<>();

                    for (int i = 0; i < amount; i++) {
                        int baseX = chunkX + random.nextInt(16);
                        int baseZ = chunkZ + random.nextInt(16);

                        // หาความสูงที่เหมาะสมใน thread แยก
                        int validHeight = findValidHeight(world, baseX, baseZ, minHeight, maxHeight, biomes, bottomBlocks);

                        if (validHeight == -1) {
                            continue;
                        }

                        for (String oraxenId : oraxenIds) {
                            int offsetX = gaussianRandom(radius);
                            int offsetZ = gaussianRandom(radius);
                            int newX = baseX + offsetX;
                            int newZ = baseZ + offsetZ;

                            String locationKey = newX + "," + validHeight + "," + newZ;

                            if (!usedLocations.add(locationKey)) {
                                continue; // ถ้าตำแหน่งนี้ถูกใช้แล้ว ให้ข้ามไป
                            }

                            // กลับมาทำงานใน main thread สำหรับการวางบล็อก
                            int finalNewX = newX;
                            int finalNewZ = newZ;
                            int finalValidHeight = validHeight;

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                Block targetBlock = world.getBlockAt(finalNewX, finalValidHeight, finalNewZ);
                                if (!targetBlock.getType().equals(world.getBlockAt(baseX, finalValidHeight, baseZ).getType())) {
                                    return; // ข้ามถ้าประเภท block ไม่ตรง
                                }

                                Block blockAbove = targetBlock.getRelative(BlockFace.UP);
                                if (!replaceableBlocks.contains(blockAbove.getType().name())) {
                                    return; // ข้ามถ้าบล็อคไม่สามารถแทนที่ได้
                                }

                                // วางบล็อกใน main thread
                                OraxenBlocks.place(oraxenId, blockAbove.getLocation());
                            });
                        }
                    }
                }
            }
        }.runTaskAsynchronously(plugin); // ทำงานแบบ async ใน thread แยก
    }

    // ฟังก์ชันสำหรับการหาความสูงที่ถูกต้อง
    private int findValidHeight(World world, int baseX, int baseZ, int minHeight, int maxHeight, Set<String> biomes, Set<String> bottomBlocks) {
        for (int y = maxHeight; y >= minHeight; y--) {
            Block block = world.getBlockAt(baseX, y, baseZ);
            if (biomes.contains(block.getBiome().name()) && bottomBlocks.contains(block.getType().name())) {
                return y; // เจอความสูงที่ตรงตามเงื่อนไข
            }
        }
        return -1; // ถ้าไม่เจอความสูงที่ตรงตามเงื่อนไข
    }

    // ฟังก์ชันทอยโอกาสที่ปรับให้เสถียรขึ้น
    private boolean rollDiceWithChance(int chance) {
        return random.nextInt(100) < chance;
    }

    // ฟังก์ชั่นสุ่มแบบ Gaussian เพื่อการกระจายที่เสถียรมากขึ้น
    private int gaussianRandom(int radius) {
        return (int) (random.nextGaussian() * radius);
    }



//    public void Hacker(Chunk chunk) {
//
//        ConfigurationSection settingsSection = configManager.getSection("settings");
//
//        if (settingsSection != null) {
//            for (String key : settingsSection.getKeys(false)) {
//                ConfigurationSection flowerSection = settingsSection.getConfigurationSection(key);
//                if (flowerSection != null) {
//
//                    List<String> oraxenIds = flowerSection.getStringList("oraxenIds");
//                    int amount = flowerSection.getInt("amount");
//                    int radius = flowerSection.getInt("radius");
//                    int chance = flowerSection.getInt("chance");
//                    int minHeight = flowerSection.getInt("min_height");
//                    int maxHeight = flowerSection.getInt("max_height");
//                    List<String> bottomBlocks = flowerSection.getStringList("bottom_blocks");
//                    List<String> replaceableBlocks = flowerSection.getStringList("replaceable_blocks");
//                    List<String> biomes = flowerSection.getStringList("biomes");
//
//                    World world = chunk.getWorld();
//                    if (random.nextInt(100) < chance) {
//                        for (int i = 0; i < amount; i++) {
//
//                            int baseX = chunk.getX() * 16 + random.nextInt(16);
//                            int baseZ = chunk.getZ() * 16 + random.nextInt(16);
//
//                            for (int y = maxHeight; y >= minHeight; y--) { // Start from maxHeight and go downwards
//                                Block baseBlock = world.getBlockAt(baseX, y, baseZ);
//
//                                if (biomes.stream().noneMatch(biome -> biome.equalsIgnoreCase(baseBlock.getBiome().name()))) {
//                                    continue; // Skip if the biome does not match
//                                }
//
//                                if (bottomBlocks.stream().anyMatch(blockType -> blockType.equalsIgnoreCase(baseBlock.getType().name()))) {
//                                    for (String oraxenId : oraxenIds) {
//                                        int offsetX = random.nextInt(11) - radius;
//                                        int offsetZ = random.nextInt(11) - radius;
//
//                                        Block targetBlock = world.getBlockAt(baseX + offsetX, y, baseZ + offsetZ);
//
//                                        if (targetBlock.getType().equals(baseBlock.getType())) {
//                                            Location loc = targetBlock.getRelative(BlockFace.UP).getLocation();
//
//                                            if(replaceableBlocks.stream().noneMatch(blockType -> blockType.equalsIgnoreCase(loc.getBlock().getType().name()))) {
//                                                continue;
//                                            }
//
//                                            OraxenBlocks.place(oraxenId, loc);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }


    @Override
    public void onDisable() {
        configManager.saveConfig();
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
