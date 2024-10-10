package mc.CushyPro.KItemSkin;

import mc.CushyPro.KItemSkin.Used.JsonModule;
import mc.CushyPro.KItemSkin.Used.RegData;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class IconData extends JsonModule {
    @Override
    public File getFile() {
        return null;
    }

    @RegData
    ItemStack stack;
    @RegData
    String type;
    @RegData
    int idmodel = 0;

    public IconData() {

    }

    public IconData(ItemStack stack) {
        this.stack = stack;
    }

    public int getIdmodel() {
        return idmodel;
    }

    public void setIdmodel(int idmodel) {
        this.idmodel = idmodel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}
