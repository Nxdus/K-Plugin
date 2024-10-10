package org.nxdus.kcatch.Listener;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nxdus.kcatch.KCatch;

public class SummonEntityListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        NamespacedKey catchEntityKey = new NamespacedKey(KCatch.getPlugin(KCatch.class), "catch_entity");
        NamespacedKey catchEntityNBTKey = new NamespacedKey(KCatch.getPlugin(KCatch.class), "catch_nbt");

        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

        if (itemInMainHand.getType() == Material.AIR) return;

        ItemMeta itemMeta = itemInMainHand.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        World world = player.getWorld();
        Location location = event.getInteractionPoint();

        if (container.has(catchEntityKey) && container.has(catchEntityNBTKey) && location != null) {
            itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);

            Entity summonedEntity = world.spawnEntity(location, EntityType.valueOf(container.get(catchEntityKey, PersistentDataType.STRING)));

            ReadWriteNBT summonedEntityNBT = NBT.parseNBT(container.get(catchEntityNBTKey, PersistentDataType.STRING));

            summonedEntityNBT.removeKey("Pos");
            summonedEntityNBT.removeKey("Paper.Origin");
            summonedEntityNBT.removeKey("Paper.OriginWorld");

            NBTEntity EntityNBT = new NBTEntity(summonedEntity);
            EntityNBT.mergeCompound(summonedEntityNBT);
        }
    }

}
