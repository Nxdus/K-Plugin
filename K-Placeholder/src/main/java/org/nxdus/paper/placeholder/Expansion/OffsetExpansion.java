package org.nxdus.paper.placeholder.Expansion;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nxdus.paper.placeholder.KPlaceholder;

import java.util.Arrays;

public class OffsetExpansion extends PlaceholderExpansion {

    private final KPlaceholder instance;
    private final int[] shifts = {1024, 512, 256, 128, 64, 32, 16, 8, 7, 6, 5, 4, 3, 2, 1};

    public OffsetExpansion(KPlaceholder instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "k-offset";
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

        String[] split = params.split("_");

        if (split.length >= 1) {

            // %k-offset_value_-99_6_placeholder_without_percent%
            if(split[0].equals("value")){
                if (split.length >= 4) {
                    int offsetDefault = Integer.parseInt(split[1]);
                    int offsetAdd = Integer.parseInt(split[2]);
                    String placeholderWithoutPercent = "%" + String.join("_", Arrays.copyOfRange(split, 3, split.length)) + "%";
                    String placeholderValue = PlaceholderAPI.setPlaceholders(player, "%" + placeholderWithoutPercent + "%");

                    int Offset = offsetDefault + (placeholderValue.length() * offsetAdd);

                    System.out.println("%k-offset_"+ Offset +"%");

                    return PlaceholderAPI.setPlaceholders(player, "%k-offset_"+ Offset +"%");
                }

                return "%k-offset_value_-99_6_placeholder_without_percent%";
            }

            try {
                int offset = Integer.parseInt(split[0]);
                if (offset == 0) return "";
                boolean isNegative = offset < 0;
                offset = Math.abs(offset);
                StringBuilder result = new StringBuilder();
                for (int shift : shifts) {
                    if (offset >= shift) {
                        offset -= shift;
                        if (isNegative) {
                            result.append("%oraxen_neg_shift_").append(shift).append("%");
                        } else {
                            result.append("%oraxen_shift_").append(shift).append("%");
                        }
                    }
                }

                if(params.startsWith("!")) {
                    return result.toString();
                }
                return PlaceholderAPI.setPlaceholders(player, result.toString());
            } catch (NumberFormatException e) {
                return "Invalid number format";
            }

        }

        return "Unknown Action";
    }
}
