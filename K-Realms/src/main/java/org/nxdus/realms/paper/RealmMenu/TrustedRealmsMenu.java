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

public class TrustedRealmsMenu implements Listener {

    private Inventory inventory;
    private final List<ItemStack> playerHeads;

    public TrustedRealmsMenu(Player player) {
        this.playerHeads = getPlayerHeads(player);
        createInventory(player);
        MainPaper.plugin.getServer().getPluginManager().registerEvents(this, MainPaper.plugin);
    }

    private final int playerHeadPerPage = 14;
    private int pageIndex = 0;

    private void createInventory(Player player) {
        inventory = Bukkit.createInventory(null, 27, PlaceholderAPI.setPlaceholders(player,ChatColor.WHITE + "<shift:-8>%oraxen_realms_friend%"));
        int[] playerHeadSlots = {1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16};

        ItemStack transparentItem = new ItemStack(Material.PAPER);
        transparentItem.editMeta(itemMeta -> {
            itemMeta.setCustomModelData(1000);
            itemMeta.setDisplayName(" ");
        });

        // Goto realms menu
        inventory.setItem(0, transparentItem);

        // Previous Page Button
        if (pageIndex > 0) {
            inventory.setItem(18, transparentItem);
            inventory.setItem(19, transparentItem);
        }

        // Next Page Button
        if ((pageIndex + 1) * playerHeadPerPage < playerHeads.size()) {
            inventory.setItem(25, transparentItem);
            inventory.setItem(26, transparentItem);
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

    private List<ItemStack> getPlayerHeads(Player player) {
        List<List<String>> playerInfos = getPlayerInfo(player);
        List<ItemStack> playerHeads = new ArrayList<>();

        for (List<String> playerInfo : playerInfos) {
            ItemStack playerHead = getPlayerHead(player, playerInfo.get(0), playerInfo.get(1), playerInfo.get(2), playerInfo.get(3));
            playerHeads.add(playerHead);
        }

        return playerHeads;
    }

    private List<List<String>> getPlayerInfo(Player player) {
        List<List<String>> playerInfo = new ArrayList<>();

        try {
            PreparedStatement selectUserID = KCore.databaseConnection.prepareStatement("SELECT users.id FROM users WHERE users.uuid = ? LIMIT 1");
            selectUserID.setString(1, player.getUniqueId().toString());
            ResultSet selectUserIDResult = selectUserID.executeQuery();
            String userID = selectUserIDResult.next() ? selectUserIDResult.getString("id") : null;

            PreparedStatement realmsMemberStatement = KCore.databaseConnection.prepareStatement(
                    "SELECT u.* FROM users u WHERE u.id IN (" +
                            "SELECT r.user_id FROM realms r JOIN realm_member rm ON r.id = rm.realm_id " +
                            "WHERE rm.user_id = ? AND rm.role = 'trusted')"
            );
            realmsMemberStatement.setString(1, userID);
            ResultSet resultSet = realmsMemberStatement.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");
                String url = resultSet.getString("skin_url");

                if (id != null && uuid != null && username != null && url != null) playerInfo.add(List.of(id, uuid, username, url));
            }

            selectUserID.close();
            realmsMemberStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerInfo;
    }


    private ItemStack getPlayerHead(Player player, String id, String uuid, String username, String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        head.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(MainPaper.plugin, "id"), org.bukkit.persistence.PersistentDataType.STRING, id);
            itemMeta.setDisplayName(ChatColor.WHITE + PlaceholderAPI.setPlaceholders(player, getPrefix(UUID.fromString(uuid))) + " " + username);
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // Handle page navigation
        if ((slot == 18 || slot == 19) && pageIndex > 0) {  // Check if it's your 'previous' button slot
            pageIndex--;
            createInventory(player);
            player.openInventory(inventory);
        } else if ((slot == 25 || slot == 26) && (pageIndex + 1) * playerHeadPerPage < playerHeads.size()) {  // Check if it's your 'next' button slot
            pageIndex++;
            createInventory(player);
            player.openInventory(inventory);
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        if (clickedItem.getType().equals(Material.PAPER)) {
            if (slot == 0) {
                player.performCommand("realms");
            }
        } else if (clickedItem.getType().equals(Material.PLAYER_HEAD) && clickedItem.getItemMeta().getPersistentDataContainer().has(new org.bukkit.NamespacedKey(MainPaper.plugin, "id"), org.bukkit.persistence.PersistentDataType.STRING)) {
            String userID = clickedItem.getItemMeta().getPersistentDataContainer().get(new org.bukkit.NamespacedKey(MainPaper.plugin, "id"), org.bukkit.persistence.PersistentDataType.STRING);
            player.sendMessage(userID);
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

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }
}
