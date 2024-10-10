package org.nxdus.core.shared.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    public enum ConfigType {
        GENERAL("general"),
        REALM("realm"),
        REALM_BASE("realm-base"),
        SPAWN("realm-spawn"),
        GENERATOR("realm-generator"),
        RESOURCES_GENERATOR("realm-resource-generator"),
        VELOCITY("velocity"),
        PRE_AUTH("pre-auth");

        private final String value;

        ConfigType(String value) { this.value = value;}

        public String getValue() { return this.value; }
    }

    private JsonObject config;
    private final String configPath;

    public final Gson gson = new Gson();

    public ConfigManager() {
        String currentDir = System.getProperty("user.dir");

        Path coreDirPath = Paths.get(currentDir, "@core");
        configPath = Paths.get(coreDirPath.toString(), "config.json").toString();

        if (!Files.exists(coreDirPath)) {
            try {
                Files.createDirectory(coreDirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (new File(configPath).exists()) {

            try {
                FileReader reader = new FileReader(configPath);
                config = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        config = new JsonObject();
        config.add("redis", setRedisConfig());
        config.add("mysql", setMysqlConfig());
        config.add("type", JsonParser.parseString("general"));

        saveConfig();

        System.out.println("\u001B[32m" + "Create New Config Success : " + configPath + "\u001B[0m");
    }

    private JsonObject setRedisConfig() {
        JsonObject redisConfig = new JsonObject();

        redisConfig.addProperty("enable", false);
        redisConfig.addProperty("host", "");
        redisConfig.addProperty("port", 6379);
        redisConfig.addProperty("username", "");
        redisConfig.addProperty("password", "");

        return redisConfig;
    }

    private JsonObject setMysqlConfig() {
        JsonObject mysqlConfig = new JsonObject();

        mysqlConfig.addProperty("enable", false);
        mysqlConfig.addProperty("host", "");
        mysqlConfig.addProperty("port", 3306);
        mysqlConfig.addProperty("username", "");
        mysqlConfig.addProperty("password", "");
        mysqlConfig.addProperty("database", "");

        return mysqlConfig;
    }

    private void saveConfig() {
        try {
            FileWriter writer = new FileWriter(configPath);
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getConfig(String key) {
        String[] Keys = key.split("\\.");
        JsonObject obj = config;

        for (int i = 0; i < Keys.length - 1; i++) {
            if (obj.has(Keys[i])) {
                obj = obj.getAsJsonObject(Keys[i]);
            } else {
                return null;
            }
        }

        JsonElement values = obj.get(Keys[Keys.length - 1]);

        if (values != null) {
            if (values.getAsJsonPrimitive().isBoolean()) {
                return values.getAsBoolean();
            } else if (values.getAsJsonPrimitive().isNumber()) {
                return values.getAsNumber();
            } else if (values.getAsJsonPrimitive().isString()) {
                return values.getAsString();
            }
        }

        throw new Error("Key: "+ key + " has not found in "+ configPath);
    }

    public boolean getConfigAsBoolean(String key) {
        return this.getConfig(key).toString().equalsIgnoreCase("true");
    }

    public int getConfigAsInt(String key) {
        return Integer.parseInt(this.getConfig(key).toString());
    }

    public String getConfigAsString(String key) {
        return this.getConfig(key).toString();
    }

    public ConfigType getType() {

        String type = config.get("type").getAsString();
        if (type != null) {
            for (ConfigType ct : ConfigType.values()) {
                if (type.equals(ct.getValue())) {
                    return ct;
                }
            }

            System.err.println("Invalid config type: " + type);
        }

        return null;
    }

}
