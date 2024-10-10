package mc.CushyPro.KItemSkin.Used;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemOrder {

    ItemStack stack;

    public ItemOrder(Material type) {
        stack = new ItemStack(type);
    }

    public ItemOrder meta(Consumer<ItemMeta> args) {
        stack.editMeta(args);
        return this;
    }

    public ItemOrder setAmount(int args) {
        stack.setAmount(args);
        return this;
    }

    public ItemOrder setDisplay(String args) {
        stack.editMeta(meta -> meta.setDisplayName(args));
        return this;
    }

    public ItemOrder addLore(String args) {
        stack.editMeta(meta -> {
            List<String> list = new ArrayList<>();
            if (meta.hasLore()) {
                list = meta.getLore();
            }
            list.add(args);
            meta.setLore(list);
        });
        return this;
    }

    public ItemOrder hideAttr() {
        stack.editMeta(meta -> {
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("a", 0, AttributeModifier.Operation.ADD_NUMBER));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        return this;
    }

    public <M extends ItemMeta> ItemOrder meta(@NotNull Class<M> metaClass, @NotNull Consumer<? super M> args) {
        stack.editMeta(metaClass, args);
        return this;
    }

    public ItemStack getItem() {
        return stack;
    }

    public ItemOrder hideToolTip() {
        stack.editMeta(meta -> meta.setHideTooltip(true));
        return this;
    }

    public ItemOrder setCustomModel(int model) {
        stack.editMeta(meta -> meta.setCustomModelData(model));
        return this;
    }
}
