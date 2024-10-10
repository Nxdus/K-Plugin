package org.nxdus.kcatch.Reciped;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.nxdus.kcatch.KCatch;

public class CustomRecipe {

    public CustomRecipe() {

        NamespacedKey catcherAnimalsKey = new NamespacedKey(KCatch.getPlugin(KCatch.class), "catcher_animals");
        ShapedRecipe catcherAnimalsRecipe = new ShapedRecipe(catcherAnimalsKey, CatchItem.animalsCatcher());

        catcherAnimalsRecipe.shape(" LL", " SL", "S  ");
        catcherAnimalsRecipe.setIngredient('L', Material.LEAD);
        catcherAnimalsRecipe.setIngredient('S', Material.STICK);

        NamespacedKey catcherMonstersKey = new NamespacedKey(KCatch.getPlugin(KCatch.class), "catcher_monsters");
        ShapelessRecipe catcherMonstersRecipe = new ShapelessRecipe(catcherMonstersKey, CatchItem.monsterCatcher());

        catcherMonstersRecipe.addIngredient(1,Material.PHANTOM_MEMBRANE);
        catcherMonstersRecipe.addIngredient(1,CatchItem.animalsCatcher());

        Bukkit.removeRecipe(catcherAnimalsKey);
        Bukkit.removeRecipe(catcherMonstersKey);

        Bukkit.addRecipe(catcherAnimalsRecipe);
        Bukkit.addRecipe(catcherMonstersRecipe);

        Bukkit.updateRecipes();
    }

}
