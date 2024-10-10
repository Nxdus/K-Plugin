package mc.CushyPro.KItemSkin.Used;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PInven {

    public interface InventoryFull {
        void WhenFull(Player player, Map<Integer, ItemStack> items);
    }

    public static InventoryFull DROP_ITEM = (player, items) -> items.forEach((integer, itemStack) -> player.getWorld().dropItem(player.getLocation(), itemStack));

    Player player;
    PlayerInventory inventory;

    public PInven(Player player) {
        this.player = player;
        this.inventory = player.getInventory();
    }

    public void AddItem(ItemStack... items) {
        AddItem(DROP_ITEM, items);
    }

    public void AddItem(InventoryFull action, ItemStack... items) {
        HashMap<Integer, ItemStack> maps = this.inventory.addItem(items);
        if (!maps.isEmpty()) {
            if (action != null) {
                action.WhenFull(player, maps);
            }
        }
    }

    public boolean canFit(ItemStack... item) {
        if (player == null) return false;
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack st : item) {
            list.add(st.clone());
        }
        Inventory inventory = Bukkit.createInventory(null, 36);
        for (byte b = 0; b < 36; b++) {
            ItemStack stack = player.getInventory().getItem(b);
            if (stack != null && stack.getType() != Material.AIR) {
                inventory.setItem(b, stack.clone());
            }
        }
        return inventory.addItem(list.toArray(new ItemStack[]{})).isEmpty();
    }

    public boolean hasItem(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (!this.inventory.containsAtLeast(stack, stack.getAmount())) {
                return false;
            }
        }
        return true;
    }

    public void removeItem(ItemStack... stacks) {
        for (ItemStack is : stacks) {
            int am = is.getAmount();
            for (ItemStack it : inventory) {
                if (it != null && it.getType() != Material.AIR) {
                    if (it.isSimilar(is)) {
                        if (am > it.getAmount()) {
                            am -= it.getAmount();
                            it.setAmount(0);
                        } else {
                            it.setAmount(it.getAmount() - am);
                            am = 0;
                        }
                        if (0 >= am) {
                            break;
                        }
                    }
                }
            }
        }
    }

}
