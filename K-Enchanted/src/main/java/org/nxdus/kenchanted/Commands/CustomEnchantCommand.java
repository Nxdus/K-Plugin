package org.nxdus.kenchanted.Commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class CustomEnchantCommand implements BasicCommand {

    private final NamespacedKey addSlot = new NamespacedKey("custom-enchant", "add-slot");

    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] strings) {

        if (!(commandSourceStack.getSender() instanceof Player player)) return;

        if (strings.length == 1 && strings[0].equalsIgnoreCase("giveRuneSlot")) {

            ItemStack runeItem = new ItemStack(Material.NETHER_STAR);

            runeItem.editMeta(itemMeta -> {
                itemMeta.setDisplayName(ChatColor.GOLD + "Rune Slot");
                itemMeta.getPersistentDataContainer().set(addSlot, PersistentDataType.INTEGER, 1);
            });

            player.getInventory().addItem(runeItem);

            player.sendMessage(ChatColor.GREEN + "Get Rune Slot" +
                    runeItem.getItemMeta().getPersistentDataContainer().get(addSlot, PersistentDataType.INTEGER));
        }


    }

}
