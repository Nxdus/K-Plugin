package org.nxdus.paper.placeholder.Config;

import com.google.gson.*;
import org.nxdus.paper.placeholder.KPlaceholder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String configPath;
    private final KPlaceholder instance;
    private JsonObject config;

    public ConfigManager(KPlaceholder instance) {
        this.instance = instance;
        this.configPath = instance.getDataFolder() + "/config.json";

        loadConfig();
    }

    public void loadConfig() {
        config = readConfig();
    }

    public JsonObject readConfig() {

        if (!Files.exists(Paths.get(instance.getDataFolder().toURI()))) {
            try {
                Files.createDirectory(Paths.get(instance.getDataFolder().toURI()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (FileReader reader = new FileReader(configPath)) {
            return gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            instance.getLogger().warning("Failed to read config.json : " + e.getMessage());
        }

        JsonObject defaultConfig = new JsonObject();
        defaultConfig.add("placeholders", new JsonObject());

        writeConfig();

        return defaultConfig;
    }

    public void writeConfig() {
        try (FileWriter writer = new FileWriter(configPath)) {
            gson.toJson(config, writer);
            writer.close();
            instance.getLogger().info("Config file saved successfully.");
        } catch (IOException e) {
            instance.getLogger().warning("Failed to write config.json : " + e.getMessage());
        }
    }

    public Object getConfigValue(String key) {
        JsonElement value = config.get(key);

        if (value != null) {
            if (value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return primitive.getAsString();
                } else if (primitive.isNumber()) {
                    return primitive.getAsNumber();
                } else if (primitive.isBoolean()) {
                    return primitive.getAsBoolean();
                }
            } else if (value.isJsonObject()) {
                return value.getAsJsonObject();
            } else if (value.isJsonArray()) {
                return value.getAsJsonArray();
            }
        }

        instance.getLogger().warning("Key '" + key + "' not found in config.");
        return null;
    }

}
