package org.nxdus.realms.paper.ArmorHidden;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.nxdus.core.paper.KCore;
import org.nxdus.realms.paper.MainPaper;
import org.nxdus.realms.paper.ArmorHidden.Utils.SelfArmorUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ArmorToggleService implements Listener, CommandExecutor {

    private final MainPaper plugin;
    private final ProtocolManager protocolManager;
    private final HashMap<UUID, Boolean> armorToggleMap = new HashMap<>();
    private final HashMap<UUID, Long> cooldownMap = new HashMap<>();
    private static final long COOLDOWN_TIME = 2000; // 5 seconds in milliseconds


    public ArmorToggleService(MainPaper plugin, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.protocolManager = protocolManager;

        registerHandler();
        registerPacketListener();
    }

    private void updatePlayerVisibility(Player player) {
        World world = player.getWorld();
        for(Player worldPlayer : world.getPlayers()) {
            player.hidePlayer(plugin, worldPlayer);
            player.showPlayer(plugin, worldPlayer);
        }
    }

    private void registerHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("toggle-armor").setExecutor(this);
    }

    private void registerPacketListener() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();

                boolean armorToggleEnabled;

                try {
                    armorToggleEnabled = isArmorVisible(receiver);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                if (!armorToggleEnabled) return;

                World world = receiver.getWorld();

                for (Entity entity : world.getEntities()) {
                    if (entity.getEntityId() == event.getPacket().getIntegers().read(0)) {
                        EntityType entityType = entity.getType();
                        if (entityType != EntityType.PLAYER) return;
                    }
                }

                PacketContainer packet = event.getPacket();
                StructureModifier<List<Pair<ItemSlot, ItemStack>>> stackPairLists = packet.getSlotStackPairLists();
                List<Pair<ItemSlot, ItemStack>> readed = stackPairLists.read(0);

                readed.forEach(pair -> {
                    ItemSlot slot = pair.getFirst();
                    if (slot == ItemSlot.HEAD && pair.getSecond().getType().toString().contains("HELMET")) {
                        pair.setSecond(new ItemStack(Material.AIR));
                    } else if (slot == ItemSlot.CHEST || slot == ItemSlot.LEGS || slot == ItemSlot.FEET) {
                        pair.setSecond(new ItemStack(Material.AIR));
                    }
                });

                stackPairLists.write(0, readed);
                event.setPacket(packet);
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {

                PacketContainer packet = event.getPacket();
                Player receiver = event.getPlayer();

                boolean armorToggleEnabled;

                try {
                    armorToggleEnabled = isArmorVisible(receiver);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                if (!armorToggleEnabled) return;

                if(packet.getType().equals(PacketType.Play.Server.SET_SLOT) && packet.getIntegers().read(0) == 0){
                    ItemStack itemStack = packet.getItemModifier().read(0);

                    if (packet.getIntegers().read(2) == 5 && !itemStack.getType().toString().contains("HELMET")) return;
                    if (packet.getIntegers().read(2) == 6 && itemStack.getType().toString().contains("ELYTRA")) return;

                    if (!(packet.getIntegers().read(2) > 4 && packet.getIntegers().read(2) < 9)) return;
                    if (itemStack.getType() == Material.AIR) return;

                    packet.getItemModifier().write(0, SelfArmorUtils.getHiddenArmor(itemStack));
                    event.setPacket(packet);
                }

                if (packet.getType().equals(PacketType.Play.Server.WINDOW_ITEMS) && packet.getIntegers().read(0) == 0) {

                    List<ItemStack> itemStacks = packet.getItemListModifier().read(0);

                    for (int i = 5; i < 9; i++) {

                        if (i == 5 && !itemStacks.get(5).getType().toString().contains("HELMET")) {
                            continue;
                        } else if (i == 6 && itemStacks.get(6).getType().toString().contains("ELYTRA")) {
                            continue;
                        }

                        itemStacks.set(i, SelfArmorUtils.getHiddenArmor(itemStacks.get(i)));
                    }

                    packet.getItemListModifier().write(0, itemStacks);
                    event.setPacket(packet);
                }
            }
        });
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        isArmorVisible(player);
    }

    @EventHandler
    private void onShiftClickArmor(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        boolean armorToggleEnabled;

        try {
            armorToggleEnabled = isArmorVisible(player);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (!armorToggleEnabled) return;
        if(!(event.getClickedInventory() instanceof PlayerInventory)) return;
        if (!event.isShiftClick()) return;

        if (event.getSlotType() == InventoryType.SlotType.ARMOR || event.getCurrentItem().getType().getEquipmentSlot().isArmor()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onGameModeChange(PlayerGameModeChangeEvent event) throws SQLException {
        Player player = event.getPlayer();

        boolean armorToggleEnabled = isArmorVisible(player);

        if (!armorToggleEnabled) return;

        if (event.getNewGameMode().equals(GameMode.CREATIVE)) {
            setArmorVisible(player, false);
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cYou must be a player to use this command.");
            return true;
        }

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            player.sendMessage(KCore.translate.format("you-are-creative-mode"));
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (cooldownMap.containsKey(playerUUID)) {
            long lastUsed = cooldownMap.get(playerUUID);
            long timeLeft = COOLDOWN_TIME - (currentTime - lastUsed);

            if (timeLeft > 0) {
                player.sendMessage(KCore.translate.format("armor-toggle.cooldown-use", "cooldown", String.format("%.1f", timeLeft / 1000.0)));
                return true;
            }
        }

        try {
            boolean isToggle = !isArmorVisible(player);
            setArmorVisible(player,isToggle);
            if(isToggle) {
                player.sendMessage(KCore.translate.format("armor-toggle.true"));
            }else {
                player.sendMessage(KCore.translate.format("armor-toggle.false"));
            }
            cooldownMap.put(playerUUID, currentTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private boolean isArmorVisible(Player player) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        if (armorToggleMap.containsKey(playerUUID)) {
            return armorToggleMap.get(playerUUID);
        } else {
            boolean toggle = fetchArmorVisibilityFromDatabase(player);
            armorToggleMap.put(playerUUID, toggle);
            return toggle;
        }
    }

    private boolean fetchArmorVisibilityFromDatabase(Player player) throws SQLException {
        PreparedStatement statement = KCore.databaseConnection.prepareStatement("SELECT users.toggle_visible_armor FROM users WHERE users.uuid = ?");
        statement.setString(1, player.getUniqueId().toString());
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getBoolean("toggle_visible_armor");
        }

        return false;
    }

    public void setArmorVisible(Player player, boolean toggle) throws SQLException {
        UUID playerUUID = player.getUniqueId();
        armorToggleMap.put(playerUUID, toggle);

        sendSelfArmor(player);

        updateArmorVisibilityInDatabase(player, toggle);
        updatePlayerVisibility(player);
    }

    private void updateArmorVisibilityInDatabase(Player player, boolean toggle) throws SQLException {
        PreparedStatement statement = KCore.databaseConnection.prepareStatement("UPDATE users SET users.toggle_visible_armor = ? WHERE users.uuid = ?");
        statement.setBoolean(1, toggle);
        statement.setString(2, player.getUniqueId().toString());
        statement.executeUpdate();
    }

    private void sendSelfArmor(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int i = 5; i < 9; i ++) {
            PacketContainer packetSelf = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);

            packetSelf.getIntegers().write(0, 0);
            packetSelf.getIntegers().write(2, i);
            packetSelf.getItemModifier().write(0, SelfArmorUtils.getArmor(SelfArmorUtils.ArmorType.getType(i), inv));

            protocolManager.sendServerPacket(player, packetSelf);
        }

    }
}
