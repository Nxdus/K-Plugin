package mc.CushyPro.KItemSkin.Used;

import com.google.gson.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.nxdus.core.paper.KCore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JsonModule {
    private static final Gson mapper = new GsonBuilder().setPrettyPrinting().create();

    public abstract File getFile();

    public void loadConfig() {

        try {

            PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("SELECT settings.value FROM settings WHERE `key` = ?");
            preparedStatement.setString(1, "skin.items.config");
            preparedStatement.executeQuery();
            preparedStatement.getResultSet().next();
            String json = preparedStatement.getResultSet().getString(1);

            if (json.isEmpty()) {
                json = "{}";
            }

            loadJson(JsonParser.parseString(json).getAsJsonObject());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


//        try {
//            File file = getFile();
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
//            JsonElement json = new JsonObject();
//            if (!file.exists()) {
//                file.createNewFile();
//                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//                writer.write(mapper.toJson(json));
//                writer.close();
//            } else {
//                json = JsonParser.parseReader(new FileReader(file));
//            }
//            loadJson(json.getAsJsonObject());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    public void saveConfig() {

        try {
            PreparedStatement preparedStatement = KCore.databaseConnection.prepareStatement("UPDATE settings SET value = ? WHERE `key` = ?");
            preparedStatement.setString(1, toJson().toString());
            preparedStatement.setString(2, "skin.items.config");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

//        try {
//            File file = getFile();
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            writer.write(mapper.toJson(toJson()));
//            writer.close();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    public void loadJson(JsonObject json) {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(RegData.class)) {
                    RegData con = field.getAnnotation(RegData.class);
                    String key = con.value();
                    if (key.isEmpty()) {
                        key = field.getName();
                    }
                    if (json.has(key)) {
                        field.setAccessible(true);
                        Object co = ReadJson(con, field.getType(), json.get(key));
                        if (co != null) {
                            field.set(this, co);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error field: " + field.getName());
                e.printStackTrace();
            }
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(RegData.class)) {
                    RegData con = field.getAnnotation(RegData.class);
                    String key = con.value();
                    if (key.isEmpty()) {
                        key = field.getName();
                    }
                    Class<?> cs = field.getType();
                    field.setAccessible(true);
                    Object obj = field.get(this);
                    if (obj != null) {
                        JsonElement ele = WriteJson(con, cs, obj);
                        if (ele != null) {
                            json.add(key, ele);
                        }
                    }
                }
            }
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private JsonElement WriteJson(RegData con, Class<?> cs, Object obj) {
        if (cs.getSuperclass() == JsonModule.class || cs == JsonModule.class) {
            JsonModule k = (JsonModule) obj;
            return k.toJson();
        } else if (cs == JsonObject.class) {
            return (JsonObject) obj;
        } else if (cs == List.class) {
            List<?> list = (List<?>) obj;
            JsonArray arr = new JsonArray();
            for (Object el : list) {
                arr.add(WriteJson(con, con.mapclass(), el));
            }
            return arr;
        } else if (cs == Map.class) {
            Map<String, Object> map = (Map<String, Object>) obj;
            JsonObject a = new JsonObject();
            RegData ced;
            if (con.mapclass() == List.class) {
                ced = newRagData();
            } else {
                ced = con;
            }
            map.forEach((name, o) -> a.add(name, WriteJson(ced, con.mapclass(), o)));
            return a;
        } else if (cs == String.class) {
            return new JsonPrimitive((String) obj);
        } else if (cs.getSuperclass() == Number.class || cs == int.class || cs == long.class || cs == double.class || cs == float.class) {
            return new JsonPrimitive((Number) obj);
        } else if (cs == Boolean.class || cs == boolean.class) {
            return new JsonPrimitive((Boolean) obj);
        } else if (cs == ItemStack.class) {
            return ISJson.goJson((ItemStack) obj);
        } else if (obj instanceof ConfigurationSerializable || cs.getSuperclass() == ConfigurationSerializable.class || cs == ConfigurationSerializable.class) {
            return ConfigModemJson.goJson(obj);
        }
        return null;
    }

    private RegData newRagData() {
        return new RegData() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public String value() {
                return "";
            }

            @Override
            public Class<?> mapclass() {
                return String.class;
            }

        };
    }

    private Object ReadJson(RegData con, Class<?> cs, JsonElement ele) throws Exception {
        if (cs.getSuperclass() == JsonModule.class || cs == JsonModule.class) {
            JsonModule jm = (JsonModule) cs.getConstructor().newInstance();
            jm.loadJson(ele.getAsJsonObject());
            return jm;
        } else if (cs == JsonObject.class) {
            return ele.getAsJsonObject();
        } else if (cs == Map.class) {
            Map<String, Object> map = new HashMap<>();
            JsonObject a = ele.getAsJsonObject();
            RegData ced;
            if (con.mapclass() == List.class) {
                ced = newRagData();
            } else {
                ced = con;
            }
            for (String s : a.keySet()) {
                Object ons = ReadJson(ced, con.mapclass(), a.get(s));
                if (ons != null) {
                    map.put(s, ons);
                }
            }
            return map;
        } else if (cs == List.class) {
            List<Object> list = new ArrayList<>();
            JsonArray arr = ele.getAsJsonArray();
            for (JsonElement el : arr) {
                Object ob = ReadJson(con, con.mapclass(), el);
                if (ob != null) {
                    list.add(ob);
                }
            }
            return list;
        } else if (cs == String.class) {
            return ele.getAsString();
        } else if (cs == int.class || cs == Integer.class) {
            return ele.getAsInt();
        } else if (cs == double.class || cs == Double.class) {
            return ele.getAsDouble();
        } else if (cs == long.class || cs == Long.class) {
            return ele.getAsLong();
        } else if (cs == float.class || cs == Float.class) {
            return ele.getAsFloat();
        } else if (cs == boolean.class || cs == Boolean.class) {
            return ele.getAsBoolean();
        } else if (cs == ItemStack.class) {
            return ISJson.read(ele.getAsJsonObject());
        } else if (ConfigurationSerializable.class.isAssignableFrom(cs)) {
            return ConfigModemJson.read(ele);
        }
        return null;
    }


}
