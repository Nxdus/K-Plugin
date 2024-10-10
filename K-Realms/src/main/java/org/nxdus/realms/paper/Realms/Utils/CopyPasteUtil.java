package org.nxdus.realms.paper.Realms.Utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CopyPasteUtil {

    public static boolean use(
            World worldOriginalAdapt,
            World worldCloneAdapt,
            double radius,
            double x1,
            double y1,
            double z1,
            double xTarget,
            double yTarget,
            double zTarget
    ) {

        double xSource = (int) x1;
        double ySource = (int) y1;
        double zSource = (int) z1;

        BlockVector3 pos1 = BlockVector3.at(xSource - radius, ySource - radius, zSource - radius);
        BlockVector3 pos2 = BlockVector3.at(xSource + radius, ySource + radius, zSource + radius);
        CuboidRegion region = new CuboidRegion(worldOriginalAdapt, pos1, pos2);

        // Method 1
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldOriginalAdapt)) {

            editSession.getAllowedRegions();

            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            clipboard.setOrigin(BlockVector3.at(xSource, ySource, zSource));

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession.getBypassAll().disableHistory(), region, clipboard, region.getMinimumPoint()
            );
            forwardExtentCopy.setCopyingBiomes(true);
            Operations.complete(forwardExtentCopy);

            try (EditSession cloneSession = WorldEdit.getInstance().newEditSession(worldCloneAdapt)) {

                cloneSession.getAllowedRegions();


                ClipboardHolder holder = new ClipboardHolder(clipboard);
                Operation operation = holder.createPaste(cloneSession.getBypassAll().disableHistory())
                        .to(BlockVector3.at(xTarget, yTarget, zTarget))
                        .ignoreAirBlocks(true)
                        .copyBiomes(true)
                        .build();
                Operations.complete(operation);
                cloneSession.flushQueue();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static CompletableFuture<Boolean> useAsync(
            World worldOriginalAdapt,
            World worldCloneAdapt,
            double radius,
            double x1,
            double y1,
            double z1,
            double xTarget,
            double yTarget,
            double zTarget
    ) {
        int divisionsX = 4;
        int divisionsZ = 2;
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        double chunkRadiusX = radius / divisionsX;
        double chunkRadiusZ = radius / divisionsZ;

        // ใช้ลูปเพื่อแบ่งส่วนของการคัดลอกบล็อกตาม X และ Z
        for (int i = 0; i < divisionsX; i++) {
            for (int j = 0; j < divisionsZ; j++) {
                double xOffset = (i * chunkRadiusX) - radius / 2;
                double zOffset = (j * chunkRadiusZ) - radius / 2;

                // เรียกใช้ CompletableFuture แบบแยกการทำงานแต่ละส่วน
                futures.add(CompletableFuture.supplyAsync(() -> copyAndPasteRegion(
                        worldOriginalAdapt,
                        worldCloneAdapt,
                        x1 + xOffset + chunkRadiusX / 2,
                        y1,
                        z1 + zOffset + chunkRadiusZ / 2,
                        xTarget + xOffset + chunkRadiusX / 2,
                        yTarget,
                        zTarget + zOffset + chunkRadiusZ / 2,
                        chunkRadiusX,
                        chunkRadiusZ,
                        radius
                )));
            }
        }

        // รวมผลลัพธ์ของ CompletableFuture ทั้งหมด
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().allMatch(CompletableFuture::join));
    }

    // ฟังก์ชันแยกสำหรับการคัดลอกและวางบล็อก
    private static Boolean copyAndPasteRegion(
            World worldOriginalAdapt,
            World worldCloneAdapt,
            double xSource,
            double ySource,
            double zSource,
            double xTarget,
            double yTarget,
            double zTarget,
            double chunkRadiusX,
            double chunkRadiusZ,
            double radius
    ) {
        try {
            // สร้าง CuboidRegion สำหรับการคัดลอก
            BlockVector3 pos1 = BlockVector3.at(xSource - chunkRadiusX / 2, ySource - radius, zSource - chunkRadiusZ / 2);
            BlockVector3 pos2 = BlockVector3.at(xSource + chunkRadiusX / 2, ySource + radius, zSource + chunkRadiusZ / 2);
            CuboidRegion region = new CuboidRegion(worldOriginalAdapt, pos1, pos2);

            EditSession editSession = WorldEdit.getInstance().newEditSession(worldOriginalAdapt);

            // สร้าง clipboard เพื่อเก็บข้อมูลบล็อกที่คัดลอก
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            clipboard.setOrigin(BlockVector3.at(xSource, ySource, zSource));

            // คัดลอกบล็อกจากต้นฉบับ
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            forwardExtentCopy.setCopyingBiomes(true);
            Operations.complete(forwardExtentCopy);

            // วางบล็อกที่คัดลอกไปยังโลกเป้าหมาย
            EditSession cloneSession = WorldEdit.getInstance().newEditSession(worldCloneAdapt);
            ClipboardHolder holder = new ClipboardHolder(clipboard);

            Operation operation = holder.createPaste(cloneSession)
                    .to(BlockVector3.at(xTarget, yTarget, zTarget))
                    .ignoreAirBlocks(true)
                    .copyBiomes(true)
                    .build();
            Operations.complete(operation);

            // ล้างคิวงานใน session
            cloneSession.flushQueue();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//    public static CompletableFuture<Boolean> useAsync(
//            World worldOriginalAdapt,
//            World worldCloneAdapt,
//            double radius,
//            double x1,
//            double y1,
//            double z1,
//            double xTarget,
//            double yTarget,
//            double zTarget
//    ) {
//        int divisionsX = 2;
//        int divisionsZ = 2;
//        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
//
//        double chunkRadiusX = radius / divisionsX;
//        double chunkRadiusZ = radius / divisionsZ;
//
//        for (int i = 0; i < divisionsX; i++) {
//            for (int j = 0; j < divisionsZ; j++) {
//                double xOffset = (i * chunkRadiusX) - radius / 2;
//                double zOffset = (j * chunkRadiusZ) - radius / 2;
//
//                futures.add(CompletableFuture.supplyAsync(() -> {
//                    try {
//                        double xSource = x1 + xOffset + chunkRadiusX / 2;
//                        double ySource = y1 + 0;
//                        double zSource = z1 + zOffset + chunkRadiusZ / 2;
//
//                        BlockVector3 pos1 = BlockVector3.at(xSource - chunkRadiusX / 2, ySource - radius, zSource - chunkRadiusZ / 2);
//                        BlockVector3 pos2 = BlockVector3.at(xSource + chunkRadiusX / 2, ySource + radius, zSource + chunkRadiusZ / 2);
//                        CuboidRegion region = new CuboidRegion(worldOriginalAdapt, pos1, pos2);
//
//                        EditSession editSession = WorldEdit.getInstance().newEditSession(worldOriginalAdapt);
//
//                        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
//                        clipboard.setOrigin(BlockVector3.at(xSource, ySource, zSource));
//
//                        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
//                                editSession, region, clipboard, region.getMinimumPoint()
//                        );
//                        forwardExtentCopy.setCopyingBiomes(true);
//                        Operations.complete(forwardExtentCopy);
//
//                        EditSession cloneSession = WorldEdit.getInstance().newEditSession(worldCloneAdapt);
//
//                        ClipboardHolder holder = new ClipboardHolder(clipboard);
//                        Operation operation = holder.createPaste(cloneSession)
//                                .to(BlockVector3.at(xTarget + xOffset + chunkRadiusX / 2, yTarget, zTarget + zOffset + chunkRadiusZ / 2))
//                                .ignoreAirBlocks(true)
//                                .copyBiomes(true)
//                                .build();
//                        Operations.complete(operation);
//                        cloneSession.flushQueue();
//                        return true;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return false;
//                    }
//                }));
//            }
//        }
//
//        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> futures.stream().allMatch(CompletableFuture::join));
//    }

// Example how to use

//    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//
//        if(CoreService.getConfigType() != ConfigManager.ConfigType.GENERAL) {
//            sender.sendMessage("§cERROR: this server is not for world generators.");
//        }
//
//        if (args.length != 9) {
//            sender.sendMessage("Usage: /copy <radius> <world-original> <x> <y> <z> <world-clone> <x> <y> <z>");
//            return true;
//        }
//
//        double radius = parseCoordinate(args[0]);
//        double x1 = parseCoordinate(args[2]);
//        double y1 = Double.parseDouble(args[3]);
//        double z1 = parseCoordinate(args[4]);
//        double x2 = parseCoordinate(args[6]);
//        double y2 = Double.parseDouble(args[7]);
//        double z2 = parseCoordinate(args[8]);
//
//        org.bukkit.World worldOriginal = Bukkit.getWorld(args[1]);
//        org.bukkit.World worldClone = Bukkit.getWorld(args[5]);
//
//        if (worldOriginal == null || worldClone == null) {
//            sender.sendMessage("One or both of the specified worlds do not exist.");
//            return true;
//        }
//
//        try {
//            BukkitAdapter.adapt(worldOriginal);
//            BukkitAdapter.adapt(worldClone);
//        } catch (NullPointerException e) {
//            sender.sendMessage("ERROR: adapting worlds.");
//            e.printStackTrace();
//            return true;
//        }
//
//        com.sk89q.worldedit.world.World worldOriginalAdapt = BukkitAdapter.adapt(worldOriginal);
//        com.sk89q.worldedit.world.World worldCloneAdapt = BukkitAdapter.adapt(worldClone);
//
//        TaskManager.taskManager().async(() -> {
//            long startTime = System.currentTimeMillis();
//            useAsync( worldOriginalAdapt, worldCloneAdapt, radius, x1, y1, z1, x2, y2, z2)
//                    .thenAccept(success -> {
//                        long duration = System.currentTimeMillis() - startTime;
//                        if (success) {
//                            sender.sendMessage("Region copied and pasted successfully in " + duration + "ms.");
//                        } else {
//                            sender.sendMessage("Failed to copy and paste the region in " + duration + "ms.");
//                        }
//                    });
//        });
//
//        return true;
//    }
//
//    @Override
//    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
//        if (args.length == 1) {
//            return Collections.singletonList("<radius>");
//        } else if (args.length == 2 || args.length == 6) {
//            List<String> worldNames = new ArrayList<>();
//            Bukkit.getWorlds().forEach(world -> worldNames.add(world.getName()));
//            return worldNames;
//        } else if (args.length == 3 || args.length == 4 || args.length == 5 || args.length == 7 || args.length == 8 || args.length == 9) {
//            return Collections.singletonList("<coordinate>");
//        }
//
//        return Collections.emptyList();
//    }
//
//    private static double parseCoordinate(String coord) {
//        if (!coord.contains(".")) {
//            coord = coord + ".500";
//        }
//        return Double.parseDouble(coord);
//    }
}
