package mc.CushyPro.KItemSkin;

import mc.CushyPro.KItemSkin.Used.JsonModule;
import mc.CushyPro.KItemSkin.Used.RegData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoreData extends JsonModule {

    @Override
    public File getFile() {
        return KItemSkinMain.getFile("config.json");
    }

    @RegData
    private List<String> types = new ArrayList<>(List.of("sword", "axe", "shovel", "hoe", "pickaxe", "bow"));
    @RegData(mapclass = List.class)
    private Map<String, List<String>> typeofmaterial = Map.of(
            "sword", List.of(Material.DIAMOND_SWORD.toString(), Material.NETHERITE_SWORD.toString()),
            "axe", List.of(Material.DIAMOND_AXE.toString(), Material.NETHERITE_AXE.toString()),
            "shovel", List.of(Material.DIAMOND_SHOVEL.toString(), Material.NETHERITE_SHOVEL.toString()),
            "hoe", List.of(Material.DIAMOND_HOE.toString(), Material.NETHERITE_HOE.toString()),
            "pickaxe", List.of(Material.DIAMOND_PICKAXE.toString(), Material.NETHERITE_PICKAXE.toString()),
            "bow", List.of(Material.BOW.toString())
    );
    @RegData
    private List<String> skins = new ArrayList<>();
    @RegData(mapclass = IconData.class)
    private List<IconData> icons = new ArrayList<>();

    public List<String> getTypes() {
        return types;
    }

    public List<IconData> getIcons() {
        return icons;
    }

    public List<String> getSkins() {
        return skins;
    }

    public List<String> getMaterialOf(String type) {
        if (typeofmaterial.containsKey(type)) {
            return typeofmaterial.get(type);
        }
        return new ArrayList<>();
    }

    public String addSkin(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return null;
        }
        int idemodel = meta.getCustomModelData();
        for (String v : typeofmaterial.keySet()) {
            for (String m : typeofmaterial.get(v)) {
                if (m.equalsIgnoreCase(stack.getType().toString())) {
                    String idc = v + "," + idemodel;
                    skins.add(idc);
                    return idc;
                }
            }
        }
        return null;
    }


    public String getSkin(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return null;
        }
        int idemodel = meta.getCustomModelData();
        for (String v : typeofmaterial.keySet()) {
            for (String m : typeofmaterial.get(v)) {
                if (m.equalsIgnoreCase(stack.getType().toString())) {
                    String skinid = v + "," + idemodel;
                    if (getSkins().contains(skinid)) {
                        return skinid;
                    }
                }
            }
        }
        return null;
    }

    public IconData getIcon(String skinid) {
        String[] data = skinid.split(",");
        String type = data[0];
        int idmodel = Integer.parseInt(data[1]);
        for (IconData icon : getIcons()) {
            if (icon.getType() != null) {
                if (icon.getType().equalsIgnoreCase(type)) {
                    if (icon.getIdmodel() == idmodel) {
                        return icon;
                    }
                }
            }
        }
        return null;
    }

    public String getType(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return null;
        }
        for (String v : typeofmaterial.keySet()) {
            for (String m : typeofmaterial.get(v)) {
                if (m.equalsIgnoreCase(stack.getType().toString())) {
                    return v;
                }
            }
        }
        return null;
    }

    public IconData getIcon(ItemStack stack) {
        for (IconData icon : icons) {
            if (icon.getStack().isSimilar(stack)) {
                return icon;
            }
        }
        return null;
    }

    public boolean setSkinItem(IconData icon, ItemStack stack) {
        if (getMaterialOf(icon.getType()).contains(stack.getType().toString())) {
            stack.editMeta(itemMeta -> itemMeta.setCustomModelData(icon.getIdmodel()));
            return true;
        }
        return false;
    }

}
