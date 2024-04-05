package com.ortodontalio.smindless.service;

import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseService {
    public static final String NO_USER_FOUND_ERR = "No access rules has been found for this player and this key: %s %s";
    public static final String INSERTION_ERR = "An error has occurred during inserting a new row to %s table: %s";
    public static final String DELETION_ERR = "An error has occurred during deletion from %s table: %s";
    private static Connection connection;
    private static final String ACCESS_TABLE = "access";
    private static final String PREMIUM_TABLE = "premium";
    private static final String PLAYER_NAME_COL = "player_name";
    private static final String PLAYER_IP_COL = "player_ip";
    private static final String SMILE_KEY_COL = "smile_key";
    private static final String CREATE_TABLE_PATTERN = """
            CREATE TABLE IF NOT EXISTS %s (
                'id' INTEGER PRIMARY KEY AUTOINCREMENT,
                '%s' VARCHAR(100) NOT NULL,
                '%s' VARCHAR(40),
                '%s' VARCHAR(100) NOT NULL
            )
            """;
    private static final String CREATE_UNIQUE_INDEX_PATTERN = """
            CREATE UNIQUE INDEX IF NOT EXISTS name_key ON %s (%s, %s)
            """;
    private static final String INSERT_INTO_PATTERN = """
            INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?)
            """;
    private static final String DELETE_FROM_PATTERN = """
            DELETE FROM %s WHERE %s = ? AND %s = ?
            """;
    private static final String SELECT_ENABLED_PATTERN = """
            SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?
            """;
    private final JavaPlugin plugin;

    public DatabaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:central.smidb");
                plugin.getLogger().info("Smindless connection has been established successfully.");
                try (PreparedStatement statement = connection.prepareStatement(String.format(CREATE_TABLE_PATTERN,
                        ACCESS_TABLE, PLAYER_NAME_COL, PLAYER_IP_COL, SMILE_KEY_COL))) {
                    statement.execute();
                }
                try (PreparedStatement statement = connection.prepareStatement(String.format(CREATE_TABLE_PATTERN,
                        PREMIUM_TABLE, PLAYER_NAME_COL, PLAYER_IP_COL, SMILE_KEY_COL))) {
                    statement.execute();
                }
                try (PreparedStatement statement = connection.prepareStatement(String.format(CREATE_UNIQUE_INDEX_PATTERN,
                        ACCESS_TABLE, PLAYER_NAME_COL, SMILE_KEY_COL))) {
                    statement.execute();
                }
                try (PreparedStatement statement = connection.prepareStatement(String.format(CREATE_UNIQUE_INDEX_PATTERN,
                        PREMIUM_TABLE, PLAYER_NAME_COL, SMILE_KEY_COL))) {
                    statement.execute();
                }
            } catch (ClassNotFoundException | SQLException e) {
                plugin.getLogger().severe(String.format("An error has occurred during manipulation with Smindless database: %s",
                        e.getMessage()));
            }
        }
    }

    public boolean writeToAccessTable(String playerName, @Nullable String playerIp, String smileKey) {
        return writeToTable(ACCESS_TABLE, playerName, playerIp, smileKey);
    }

    public boolean writeToPremiumTable(String playerName, @Nullable String playerIp, String smileKey) {
        return writeToTable(PREMIUM_TABLE, playerName, playerIp, smileKey);
    }

    private boolean writeToTable(String table, String playerName, @Nullable String playerIp, String smileKey) {
        try (PreparedStatement statement = connection.prepareStatement(String.format(INSERT_INTO_PATTERN,
                table, PLAYER_NAME_COL, PLAYER_IP_COL, SMILE_KEY_COL))) {
            statement.setString(1, playerName);
            statement.setString(2, playerIp);
            statement.setString(3, smileKey);
            statement.execute();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe(String.format(INSERTION_ERR, table, e.getMessage()));
            return false;
        }
    }

    public boolean deleteFromAccessTable(String playerName, String smileKey) {
        return deleteFromTable(ACCESS_TABLE, playerName, smileKey);
    }

    public boolean deleteFromPremiumTable(String playerName, String smileKey) {
        return deleteFromTable(PREMIUM_TABLE, playerName, smileKey);
    }

    private boolean deleteFromTable(String table, String playerName, String smileKey) {
        try (PreparedStatement statement = connection.prepareStatement(String.format(DELETE_FROM_PATTERN,
                table, PLAYER_NAME_COL, SMILE_KEY_COL))) {
            statement.setString(1, playerName);
            statement.setString(2, smileKey);
            statement.execute();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe(String.format(DELETION_ERR, table, e.getMessage()));
            return false;
        }
    }

    public boolean restrictExistsForPlayer(String playerName, String smileKey) {
        return recordExistsForPlayer(ACCESS_TABLE, playerName, smileKey);
    }

    public boolean grantExistsForPlayer(String playerName, String smileKey) {
        return recordExistsForPlayer(PREMIUM_TABLE, playerName, smileKey);
    }

    public boolean recordExistsForPlayer(String table, String playerName, String smileKey) {
        try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_ENABLED_PATTERN,
                table, PLAYER_NAME_COL, SMILE_KEY_COL))) {
            statement.setString(1, playerName);
            statement.setString(2, smileKey);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            plugin.getLogger().severe(String.format(INSERTION_ERR, table, e.getMessage()));
            return false;
        }
    }
}
