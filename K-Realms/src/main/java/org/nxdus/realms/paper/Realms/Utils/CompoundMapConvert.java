package org.nxdus.realms.paper.Realms.Utils;

import com.flowpowered.nbt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompoundMapConvert  {

    private static Tag<?> createTagFromType(String type, String key, Object value) {
        switch (type) {
            case "String":
                return new StringTag(key, (String) value);
            case "Integer":
                return new IntTag(key, ((Number) value).intValue());
            case "Byte":
                return new ByteTag(key, ((Number) value).byteValue());
            case "Short":
                return new ShortTag(key, ((Number) value).shortValue());
            case "Long":
                return new LongTag(key, ((Number) value).longValue());
            case "Float":
                return new FloatTag(key, ((Number) value).floatValue());
            case "Double":
                return new DoubleTag(key, ((Number) value).doubleValue());
            case "ByteArray":
                return new ByteArrayTag(key, (byte[]) value);
            case "IntArray":
                return new IntArrayTag(key, (int[]) value);
            case "LongArray":
                return new LongArrayTag(key, (long[]) value);
            case "ShortArray":
                return new ShortArrayTag(key, (short[]) value);
            case "Compound":
                return new CompoundTag(key, (CompoundMap) value);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type + " key: " + key + " value: " + value);
        }
    }

    public static CompoundMap to(String json) {

        Gson gson = new GsonBuilder().create();

        Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> tagList = gson.fromJson(json, listType);

        CompoundMap compoundMap = new CompoundMap();

        for (Map<String, Object> tagMap : tagList) {
            String type = (String) tagMap.get("type");
            String key = (String) tagMap.get("key");
            Object value = tagMap.get("value");

            Tag<?> tag = createTagFromType(type, key, value);

            compoundMap.put(tag);
        }

        return compoundMap;
    }

    public static String from(CompoundMap compoundMap) {
        List<Map<String, Object>> tagList = new ArrayList<>();

        for (Tag<?> tag : compoundMap.values()) {
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("type", tag.getValue().getClass().getSimpleName()); // ประเภทของค่า (เช่น String, Int)
            tagMap.put("key", tag.getName()); // ชื่อของ Tag
            tagMap.put("value", tag.getValue()); // ค่าใน Tag

            tagList.add(tagMap);
        }

        Gson gson = new GsonBuilder().create();
        return gson.toJson(tagList);
    }

}
