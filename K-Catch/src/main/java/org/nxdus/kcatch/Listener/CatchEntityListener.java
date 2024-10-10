package org.nxdus.kcatch.Listener;

import de.tr7zw.nbtapi.NBTEntity;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nxdus.kcatch.KCatch;

public class CatchEntityListener implements Listener {

    @EventHandler
    public void onLeftClickEntity(PrePlayerAttackEntityEvent event) {
        Entity entity = event.getAttacked();
        Player player = event.getPlayer();

        onCatchEntity(entity, player);
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractEntityEvent event) {

        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        onCatchEntity(entity, player);
    }

    private void onCatchEntity(Entity entity, Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        if (itemInMainHand.getType() == Material.AIR || !itemInMainHand.getItemMeta().hasCustomModelData()) return;

        if (itemInMainHand.getDurability() == 3) {
            player.getInventory().removeItem(itemInMainHand);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F,1F);
        }

        ToastNotification notification = new ToastNotification(getEntitySpawnItems(entity), "You have successfully caught the " + entity.getName() + ".", AdvancementDisplay.AdvancementFrame.GOAL);

        if (itemInMainHand.getItemMeta().getCustomModelData() == 555 && entity instanceof Animals) {
            player.getInventory().addItem(getEntitySpawnItems(entity));
            itemInMainHand.setDurability((short) (itemInMainHand.getDurability() + 1));
            entity.remove();

            notification.send(player);
        } else if (itemInMainHand.getItemMeta().getCustomModelData() == 556 && entity instanceof Monster) {
            player.getInventory().addItem(getEntitySpawnItems(entity));
            itemInMainHand.setDurability((short) (itemInMainHand.getDurability() + 1));
            entity.remove();

            notification.send(player);
        }
    }

    private ItemStack getEntitySpawnItems(Entity entity) {

        NBTEntity entityNBT = new NBTEntity(entity);
        NamespacedKey catchEntityKey = new NamespacedKey(KCatch.getPlugin(KCatch.class), "catch_entity");
        NamespacedKey catchEntityNBTKey = new NamespacedKey(KCatch.getPlugin(KCatch.class), "catch_nbt");
        ItemStack entitySpawnItems = entity instanceof Animals ? new ItemStack(Material.RABBIT_HIDE) : new ItemStack(Material.LEATHER);

        entitySpawnItems.editMeta(itemMeta -> {
            itemMeta.setCustomModelData(555);
        });

        ItemMeta entitySpawnItemsMeta = entitySpawnItems.getItemMeta();

        entitySpawnItemsMeta.setDisplayName(entity.getName() + " Bag !");
        PersistentDataContainer container = entitySpawnItemsMeta.getPersistentDataContainer();
        container.set(catchEntityKey, PersistentDataType.STRING, entity.getType().toString());
        container.set(catchEntityNBTKey, PersistentDataType.STRING, entityNBT.getCompound().toString());

        entitySpawnItems.setItemMeta(entitySpawnItemsMeta);

        return entitySpawnItems;
    }

}
