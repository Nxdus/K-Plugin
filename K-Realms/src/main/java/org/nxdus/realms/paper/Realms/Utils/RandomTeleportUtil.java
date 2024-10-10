package org.nxdus.realms.paper.Realms.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomTeleportUtil {
    private static final List<String> BLACKLISTED_BLOCKS = Arrays.asList(
            "WATER",
            "LAVA",
            "CACTUS",
            "FIRE",
            "MAGMA_BLOCK",
            "LEAVES"
    );

    public static Location findRandomSafeLocation(World world) {
        Random random = new Random();
        Location randomLocation = null;
        WorldBorder worldBorder = world.getWorldBorder();
        // -500 เพื่อไม่ให้ ใกล้ worldBorder เกินไป ตรงนั้นยังไม่ Generator
        double size = (worldBorder.getSize() / 2) - 500;
        Location center = worldBorder.getCenter();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        for (int i = 0; i < 32; i++) {
            int x = centerX + random.nextInt((int) size * 2) - (int) size;
            int z = centerZ + random.nextInt((int) size * 2) - (int) size;
            Block highestBlock = world.getHighestBlockAt(x, z);
            Location location = highestBlock.getLocation();

            Block block = world.getBlockAt(location);
            Material blockType = block.getType();

            boolean isBlacklisted = BLACKLISTED_BLOCKS.stream()
                    .anyMatch(blacklisted -> blockType.toString().contains(blacklisted.toUpperCase()));

            if (!isBlacklisted && blockType.isBlock()) {
                randomLocation = location.add(0.5, 1, 0.5);
                break;
            }
        }

        return randomLocation;
    }
}
