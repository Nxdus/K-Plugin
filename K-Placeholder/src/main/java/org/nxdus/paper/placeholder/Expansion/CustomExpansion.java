package org.nxdus.paper.placeholder.Expansion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nxdus.paper.placeholder.KPlaceholder;

import java.util.Map;

public class CustomExpansion extends PlaceholderExpansion {

    private final KPlaceholder instance;

    public CustomExpansion(KPlaceholder instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "k-custom";
    }

    @Override
    public @NotNull String getAuthor() {
        return instance.getDescription().getName();
    }

    @Override
    public @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

        JsonObject placeholderSection = (JsonObject) KPlaceholder.getConfigManager().getConfigValue("placeholders");

        for (Map.Entry<String, JsonElement> entry : placeholderSection.entrySet()) {

            String placeholder = entry.getKey();

            if (params.equalsIgnoreCase(placeholder)) {
                JsonObject placeholderObject = (JsonObject) entry.getValue();

                boolean isBedrockOffsetCheck = placeholderObject.get("bedrock_check").getAsBoolean();
                int bedrockOffset = isBedrockOffsetCheck ? placeholderObject.get("bedrock_offset").getAsInt() : 0;

                String[] original = (PlaceholderAPI.setPlaceholders(player, "%k-bedrock-offset_" + bedrockOffset + "%") + PlaceholderAPI.setPlaceholders(player, placeholderObject.get("original").getAsString())).split("");
                JsonObject regexSet = placeholderObject.get("regex").getAsJsonObject();
                StringBuilder finalOriginal = new StringBuilder();

                for (int i = 0; i < original.length; i++) {
                    for (String regex : regexSet.keySet()) {
                        if (original[i].equals(regex)) {

                            String replaceString = regexSet.get(regex).getAsString();
                            replaceString = replaceString.replaceAll("lengths", "length" + i);

                            original[i] = replaceString;
                            break;
                        }
                    }
                    finalOriginal.append(original[i]);
                }

                return PlaceholderAPI.setPlaceholders(player, finalOriginal.toString());
            }
        }

        return null;
    }
}
