package com.ortodontalio.smindless.service;

import com.ortodontalio.smindless.model.Smile;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class SmindlessService {
    private static final SmindlessService INSTANCE = new SmindlessService();
    private static final String SMILES_SECTION = "smiles";
    private static final String INPUTS_KEY = "smiles.%s.inputs";
    private static final String OUTPUT_KEY = "smiles.%s.output";
    private static final String PREMIUM_KEY = "smiles.%s.premium";
    private List<Smile> smiles;

    private SmindlessService() {
    }

    public static SmindlessService getInstance() {
        return INSTANCE;
    }

    public int initSmiles(@Nonnull FileConfiguration config) {
        ConfigurationSection smilesSection = config.getConfigurationSection(SMILES_SECTION);
        smiles = Optional.ofNullable(smilesSection).map(section -> section.getKeys(false).stream()
                .map(key -> {
                    List<String> inputs = List.of(Objects.requireNonNull(
                            config.getString(String.format(INPUTS_KEY, key))
                    ).split(","));
                    String output = config.getString(String.format(OUTPUT_KEY, key));
                    boolean premium = Boolean.parseBoolean(config.getString(String.format(PREMIUM_KEY, key)));
                    return new Smile(key, inputs, output, premium);
                }).toList()).orElse(new ArrayList<>());
        return smiles.size();
    }

    public Map<String, Smile> getSmilesSortedByInput() {
        return smiles.stream()
                .flatMap(sm -> sm.inputs().stream()
                        .collect(Collectors.toMap(input -> input, input -> sm)).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (sm1, sm2) -> sm1, LinkedHashMap::new));
    }

    public List<Smile> getSmiles() {
        return smiles;
    }

    /**
     * Secondary method for setting a color for a string.
     *
     * @param input some string.
     * @return formatted colored string.
     */
    public String colored(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
