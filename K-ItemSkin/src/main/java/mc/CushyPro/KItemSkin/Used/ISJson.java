package mc.CushyPro.KItemSkin.Used;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ISJson {

    public static ItemStack read(JsonObject json) {
        return new ISJson(json).getItem();
    }

    public static JsonObject goJson(ItemStack item) {
        return new ISJson(item).toJsonObject();
    }

    public ISJson(JsonObject json) {
        item = (ItemStack) A(json);
    }

    ItemStack item;

    public ISJson(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    private Object c(JsonElement es) {
        if (es.isJsonObject()) {
            return A(es.getAsJsonObject());
        } else if (es.isJsonArray()) {
            return A(es.getAsJsonArray());
        } else if (es.isJsonPrimitive()) {
            String test = es.toString();
            if (test.startsWith("\"")) {
                return es.getAsString().replace("▌", "\"");
            } else if (test.equalsIgnoreCase("false") || test.equalsIgnoreCase("true")) {
                return es.getAsBoolean();
            } else if (test.equalsIgnoreCase("null")) {
                return null;
            } else if (!test.matches("\\D")) {
                Number number = es.getAsNumber();
                if (test.contains(".")) {
                    return number.doubleValue();
                } else {
                    return number.intValue();
                }
            }
        }
        return es.toString();
    }

    private List<Object> A(JsonArray el) {
        List<Object> l = new ArrayList<>();
        for (JsonElement key : el) {
            l.add(c(key));
        }
        return l;
    }

    private Object A(JsonObject es) {
        HashMap<String, Object> map = new HashMap<>();
        Set<Map.Entry<String, JsonElement>> md = es.entrySet();
        for (Map.Entry<String, JsonElement> key : md) {
            JsonElement el = key.getValue();
            map.put(key.getKey(), c(el));
        }
        if (map.containsKey("COB")) {
            String cn = (String) map.get("COB");
            try {
                Class<? extends ConfigurationSerializable> cl = ConfigurationSerialization.getClassByAlias(cn);
                if (cl != null) {
                    try {
                        if (map.containsKey("BlockStateTag")) {
                            Map<String, Object> ms = (Map<String, Object>) map.get("BlockStateTag");
                            Map<String, String> key = new HashMap<>();
                            ms.forEach((s, o) -> key.put(s, String.valueOf(o)));
                            map.put("BlockStateTag", key);
                        }
                        return ConfigurationSerialization.deserializeObject(map, cl);
                    } catch (Exception e) {
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    public JsonObject toJsonObject() {
        return B(item);
    }

    private JsonElement B(Object obj) {
        return switch (obj) {
            case null -> new JsonPrimitive("null");
            case ConfigurationSerializable configurationSerializable -> B(configurationSerializable);
            case String str -> new JsonPrimitive(str.replace("\"", "▌"));
            case Boolean be -> new JsonPrimitive(be);
            case Number c -> new JsonPrimitive(c);
            case List<?> c -> B(c);
            case Map<?, ?> map -> B(map);
            default -> new JsonPrimitive(obj.toString());
        };
    }

    private JsonObject B(ConfigurationSerializable map) {
        JsonObject json = new JsonObject();
        if (map != null) {
            json.addProperty("COB", getClassName(map));
            for (String o : map.serialize().keySet()) {
                json.add(o, B(map.serialize().get(o)));
            }
        }
        return json;
    }

    private String getClassName(ConfigurationSerializable map) {
        return ConfigurationSerialization.getAlias(map.getClass());
    }

    private JsonObject B(Map<?, ?> map) {
        JsonObject json = new JsonObject();
        for (Object o : map.keySet()) {
            if (!(o instanceof String)) {
                continue;
            }
            json.add(o.toString(), B(map.get(o)));
        }
        return json;
    }

    private JsonArray B(List<?> list) {
        JsonArray json = new JsonArray();
        for (Object o : list) {
            json.add(B(o));
        }
        return json;
    }
}
