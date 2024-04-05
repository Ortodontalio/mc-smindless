package com.ortodontalio.smindless.service;

import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

@ParametersAreNonnullByDefault
@SuppressWarnings({"java:S2629", "java:S899", "ResultOfMethodCallIgnored"})
public class FileService {
    private static final String BAN_FOLDER = "ban-list";
    private static final String PREMIUM_FOLDER = "premium-list";
    private static final String PLAYER_FILE = "%s.txt";
    private final JavaPlugin plugin;
    private final Path pathToBanList;
    private final Path pathToPremiumList;

    public FileService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pathToBanList = Path.of(plugin.getDataFolder().getAbsolutePath(), BAN_FOLDER);
        this.pathToPremiumList = Path.of(plugin.getDataFolder().getAbsolutePath(), PREMIUM_FOLDER);
        try {
            Files.createDirectories(pathToBanList);
            Files.createDirectories(pathToPremiumList);
        } catch (IOException e) {
            plugin.getLogger().info(String.format("Could not create folders for policies management: %s",
                    e.getLocalizedMessage()));
        }
    }

    public boolean writeToBanList(String playerName, String smileKey) {
        return writePolicy(BAN_FOLDER, new File(pathToBanList.toString(), String.format(PLAYER_FILE, playerName)),
                playerName, smileKey);
    }

    public boolean writeToPremiumList(String playerName, String smileKey) {
        return writePolicy(PREMIUM_FOLDER, new File(pathToPremiumList.toString(), String.format(PLAYER_FILE, playerName)),
                playerName, smileKey);
    }

    private boolean writePolicy(String listName, File writeTo, String playerName, String smileKey) {
        try {
            writeTo.createNewFile();
            Files.writeString(writeTo.toPath(), smileKey + System.lineSeparator(), CREATE, APPEND);
            return true;
        } catch (IOException e) {
            plugin.getLogger().info(String.format("Could not write policy to '%s' for %s player and %s key: %s",
                    listName, playerName, smileKey, e.getLocalizedMessage()));
            return false;
        }
    }

    public boolean deleteFromBanList(String playerName, String smileKey) {
        return deletePolicy(BAN_FOLDER, new File(pathToBanList.toString(), String.format(PLAYER_FILE, playerName)),
                playerName, smileKey);
    }

    public boolean deleteFromPremiumList(String playerName, String smileKey) {
        return deletePolicy(PREMIUM_FOLDER, new File(pathToPremiumList.toString(), String.format(PLAYER_FILE, playerName)),
                playerName, smileKey);
    }

    private boolean deletePolicy(String listName, File deleteFrom, String playerName, String smileKey) {
        try {
            deleteFrom.createNewFile();
            List<String> allSmileKeys = Files.readAllLines(deleteFrom.toPath());
            String filteredSmileKeys = allSmileKeys.stream()
                    .filter(key -> !key.equals(smileKey))
                    .collect(Collectors.joining(System.lineSeparator()));
            Files.writeString(deleteFrom.toPath(), filteredSmileKeys, CREATE, TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            plugin.getLogger().info(String.format("Could not remove policy from '%s' for %s player and %s key: %s",
                    listName, playerName, smileKey, e.getLocalizedMessage()));
            return false;
        }
    }

    public boolean restrictExistsForPlayer(String playerName, String smileKey) {
        return policyExistsForPlayer(BAN_FOLDER, new File(pathToBanList.toString(), String.format(PLAYER_FILE, playerName)),
                playerName, smileKey);
    }

    public boolean grantExistsForPlayer(String playerName, String smileKey) {
        return policyExistsForPlayer(PREMIUM_FOLDER, new File(pathToPremiumList.toString(), String.format(PLAYER_FILE, playerName)),
                playerName, smileKey);
    }

    private boolean policyExistsForPlayer(String listName, File searchIn, String playerName, String smileKey) {
        try {
            searchIn.createNewFile();
            try (Scanner scanner = new Scanner(searchIn)) {
                scanner.useDelimiter(System.lineSeparator());
                while (scanner.hasNext()) {
                    if (smileKey.equals(scanner.next())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().info(String.format("Could not determine policies in '%s' for %s player and %s key: %s",
                    listName, playerName, smileKey, e.getLocalizedMessage()));
        }
        return false;
    }
}
