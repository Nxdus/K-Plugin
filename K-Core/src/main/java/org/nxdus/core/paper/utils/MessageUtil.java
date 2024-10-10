package org.nxdus.core.paper.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    public static Component hex(String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);

        Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            String colorCode = matcher.group();
            TextColor color = TextColor.fromHexString(colorCode);
            TextComponent coloredText = Component.text(colorCode, color);
            component = component.replaceText(builder -> builder.matchLiteral(colorCode).replacement(coloredText));
        }

        return component;
    }
}
