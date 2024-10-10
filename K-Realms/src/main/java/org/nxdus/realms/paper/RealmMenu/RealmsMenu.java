package org.nxdus.realms.paper.RealmMenu;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RealmsMenu implements Listener {

    private Inventory inventory;
    private final Player openedPlayer;

    public RealmsMenu(Player player) {
        this.openedPlayer = player;
        createInventory(player);
        MainPaper.plugin.getServer().getPluginManager().registerEvents(this, MainPaper.plugin);
    }

    private final int playerHeadPerPage = 28;
    private int pageIndex = 0;
    List<ItemStack> playerHeads = getPlayerHeads();

    private void createInventory(Player player) {

        String title = "";

        if (hasRealmAlready(player)) {
            title = PlaceholderAPI.setPlaceholders(player, ChatColor.WHITE + "<shift:-8>%oraxen_realms_list%");
        } else {
            title = PlaceholderAPI.setPlaceholders(player, ChatColor.WHITE + "<shift:-8>%oraxen_realms_list2%");
        }


        inventory = Bukkit.createInventory(null, 54, title);
        int[] playerHeadSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

        ItemStack transparentItem = new ItemStack(Material.PAPER);
        transparentItem.editMeta(itemMeta -> {
            itemMeta.setCustomModelData(1000);
            itemMeta.setDisplayName(" ");
        });

        if (hasRealmAlready(player)) {
            // Home Button
            inventory.setItem(3, transparentItem);
            inventory.setItem(4, transparentItem);
            inventory.setItem(5, transparentItem);

            // Settings Button
            inventory.setItem(8, transparentItem);

            // Realms Member List
            inventory.setItem(7, transparentItem);
        } else {
            // Create Button
            inventory.setItem(48, transparentItem);
            inventory.setItem(49, transparentItem);
            inventory.setItem(50, transparentItem);

            // Realms Member List
            inventory.setItem(8, transparentItem);
        }

        // Previous Page Button
        if (pageIndex > 0) {
            inventory.setItem(45, transparentItem);
            inventory.setItem(46, transparentItem);
        }

        // Next Page Button
        if ((pageIndex + 1) * playerHeadPerPage < playerHeads.size()) {
            inventory.setItem(52, transparentItem);
            inventory.setItem(53, transparentItem);
        }

        // Fill player heads
        int start = pageIndex * playerHeadPerPage;
        int end = start + playerHeadPerPage;
        for (int i = start; i < end && i < playerHeads.size(); i++) {
            int slotIndex = i - start;
            if (slotIndex < playerHeadSlots.length) {
                inventory.setItem(playerHeadSlots[slotIndex], playerHeads.get(i));
            }
        }

    }

    private boolean hasRealmAlready(Player player) {

        try {
            PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("SELECT world_id FROM realms WHERE user_id = (SELECT id FROM users WHERE uuid = ? LIMIT 1) LIMIT 1");
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return true;
            }

            preparedStatement.close();
        } catch (SQLException e) { e.printStackTrace(); }

        return false;
    }

    private List<ItemStack> getPlayerHeads() {
        List<List<String>> playerInfos = getPlayerInfo();
        List<ItemStack> playerHeads = new ArrayList<>();

        for (List<String> playerInfo : playerInfos) {
            ItemStack playerHead = getPlayerHead(playerInfo.get(0), playerInfo.get(1), playerInfo.get(2), playerInfo.get(3));
            playerHeads.add(playerHead);
        }

        return playerHeads;
    }

    private List<List<String>> getPlayerInfo() {

        List<List<String>> playerInfo = new ArrayList<>();

        try {
            PreparedStatement statement = KCore.databaseConnection.prepareStatement("SELECT * FROM users JOIN realms ON users.id = realms.user_id");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");
                String url = resultSet.getString("skin_url");

                if (id != null && uuid != null && username != null && url != null) {
                    playerInfo.add(List.of(id, uuid, username, url));
                }
            }

        } catch (SQLException e) { e.printStackTrace(); }

        return playerInfo;
    }

    private ItemStack getPlayerHead(String id, String uuid, String username, String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        head.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(MainPaper.plugin, "username"), org.bukkit.persistence.PersistentDataType.STRING, username);
            itemMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(MainPaper.plugin, "id"), org.bukkit.persistence.PersistentDataType.STRING, id);
            itemMeta.setDisplayName(ChatColor.WHITE + PlaceholderAPI.setPlaceholders(openedPlayer, getPrefix(UUID.fromString(uuid))) + " " + username);
            itemMeta.setCustomModelData(1002);

            List<String> itemLore = new ArrayList<>();
            itemLore.add(ChatColor.GREEN + "→ Click to visit realm");
            itemMeta.setLore(itemLore);
        });

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        URL urlObject; try { urlObject = new URL(url); } catch (MalformedURLException exception) {throw new RuntimeException("Invalid URL", exception);}
        textures.setSkin(urlObject);
        profile.setTextures(textures);

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwnerProfile(profile);
        head.setItemMeta(skullMeta);

        return head;
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Handle page navigation
        if ((slot == 45 || slot == 46) && pageIndex > 0) {  // Check if it's your 'previous' button slot
            pageIndex--;
            createInventory(player);
            player.openInventory(inventory);
        } else if ((slot == 52 || slot == 53) && (pageIndex + 1) * playerHeadPerPage < playerHeads.size()) {  // Check if it's your 'next' button slot
            pageIndex++;
            createInventory(player);
            player.openInventory(inventory);
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        if (clickedItem.getType().equals(Material.PAPER)) {

            if (slot == 3 || slot == 4 || slot == 5) {
                player.performCommand("realms load");
            } else if (slot == 48 || slot == 49 || slot == 50) {
                player.performCommand("dm open realms_create_biome_arid");
            } else if (slot == 7) {
                new TrustedRealmsMenu(player).openInventory(player);
            } else if (slot == 8) {
                if (hasRealmAlready(player)) {
                    player.performCommand("dm open realms_setting");
                } else {
                    new TrustedRealmsMenu(player).openInventory(player);
                }
            }
        } else if (clickedItem.getType().equals(Material.PLAYER_HEAD) && clickedItem.getItemMeta().getPersistentDataContainer().has(new org.bukkit.NamespacedKey(MainPaper.plugin, "username"), org.bukkit.persistence.PersistentDataType.STRING)) {
            String username = clickedItem.getItemMeta().getPersistentDataContainer().get(new org.bukkit.NamespacedKey(MainPaper.plugin, "username"), org.bukkit.persistence.PersistentDataType.STRING);
            player.performCommand("realms join " + username);
        }

        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
    }

    private String getPrefix(UUID playerUuid) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        try {
            // โหลดข้อมูลผู้ใช้แบบ synchronous
            CompletableFuture<String> prefixFuture = new CompletableFuture<>();
            luckPerms.getUserManager().loadUser(playerUuid).thenAccept(user -> {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                prefixFuture.complete(prefix != null ? prefix : "%oraxen_ranks_noob%");
            }).exceptionally(throwable -> {
                prefixFuture.complete("%oraxen_ranks_noob%");
                return null;
            });

            // บล็อก thread จนกว่า future จะเสร็จ
            return prefixFuture.get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "%oraxen_ranks_noob%";
        }
    }

}
