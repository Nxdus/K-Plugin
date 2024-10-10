package org.nxdus.kcatch.Reciped;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class CatchItem {

    public static ItemStack animalsCatcher() {

        ItemStack animalsCatcher = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK);

        animalsCatcher.editMeta(itemMeta -> {
           itemMeta.setDisplayName("Animals Catcher");
           itemMeta.setCustomModelData(555);

           Damageable damageable = (Damageable) itemMeta;
           damageable.setMaxDamage(4);
        });

        return animalsCatcher;
    }

    public static ItemStack monsterCatcher() {

        ItemStack monsterCatcher = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK);

        monsterCatcher.editMeta(itemMeta -> {
            itemMeta.setDisplayName("Monster Catcher");
            itemMeta.setCustomModelData(556);

            Damageable damageable = (Damageable) itemMeta;
            damageable.setMaxDamage(4);
        });

        return monsterCatcher;
    }

}
