package org.nxdus.paper.placeholder.Expansion;

import com.google.gson.JsonArray;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nxdus.paper.placeholder.KPlaceholder;

public class BedrockOffsetExpansion extends PlaceholderExpansion {
    private final KPlaceholder instance;

    public BedrockOffsetExpansion(KPlaceholder instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "k-bedrock-offset";
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

        boolean isBedrockPlayer = FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());

        if (!isBedrockPlayer) return "";

        try {
            int shift = Integer.parseInt(params);

            JsonArray unicodeShifts = (JsonArray) KPlaceholder.getConfigManager().getConfigValue("bedrock_shifts_unicode");
            StringBuilder finalShift = new StringBuilder();

            if (shift > unicodeShifts.size()) return "No have this shift";

            for (int i = 0; i < shift; i++) {
                finalShift.append(unicodeShifts.get(i));
            }

            return finalShift.toString().replace("\"", "");

        } catch (NumberFormatException e) {
            return "Offset not a number";
        }
    }
}
