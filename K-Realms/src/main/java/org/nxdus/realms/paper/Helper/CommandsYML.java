package org.nxdus.realms.paper.Helper;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nxdus.realms.paper.MainPaper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommandsYML {

    public CommandsYML(MainPaper plugin) {
        File commandsFile = new File(System.getProperty("user.dir"), "commands.yml");

        if (commandsFile.exists()) {
            FileConfiguration commandsConfig = new YamlConfiguration();

            try {
                commandsConfig.load(commandsFile);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }

            commandsConfig.createSection("aliases");

            ConfigurationSection aliasesSection = commandsConfig.getConfigurationSection("aliases");
            assert aliasesSection != null;
            aliasesSection.set("gms", List.of("k-realms:gm 0"));
            aliasesSection.set("gmc", List.of("k-realms:gm 1"));
            aliasesSection.set("gma", List.of("k-realms:gm 2"));
            aliasesSection.set("gmsp", List.of("k-realms:gm 3"));
            aliasesSection.set("god", List.of("k-realms:kgod"));

            commandsConfig.set("aliases", aliasesSection);

            try {
                commandsConfig.save(commandsFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
