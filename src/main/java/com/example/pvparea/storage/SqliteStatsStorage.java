package com.example.pvparea.storage;

import com.example.pvparea.PlayerStats;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SqliteStatsStorage implements StatsStorage {

    private final JavaPlugin plugin;
    private Connection connection;

    public SqliteStatsStorage(JavaPlugin plugin) throws SQLException, ClassNotFoundException {
        this.plugin = plugin;
        File dbFile = new File(plugin.getDataFolder(), "stats.db");
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("CREATE TABLE IF NOT EXISTS player_stats (" +
                    "uuid TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "kills INTEGER NOT NULL DEFAULT 0," +
                    "deaths INTEGER NOT NULL DEFAULT 0)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_player_stats_kills ON player_stats(kills DESC)");
        }
    }

    @Override
    public void load(Map<UUID, PlayerStats> out) {
        if (connection == null) return;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT uuid, name, kills, deaths FROM player_stats")) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    out.put(uuid, new PlayerStats(uuid, rs.getString("name"),
                            rs.getInt("kills"), rs.getInt("deaths")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load stats from SQLite: " + e.getMessage());
        }
    }

    @Override
    public void save(Collection<PlayerStats> snapshot) {
        if (connection == null || snapshot.isEmpty()) return;
        String sql = "INSERT INTO player_stats(uuid, name, kills, deaths) VALUES(?,?,?,?) " +
                "ON CONFLICT(uuid) DO UPDATE SET name=excluded.name, kills=excluded.kills, deaths=excluded.deaths";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (PlayerStats s : snapshot) {
                    ps.setString(1, s.getUuid().toString());
                    ps.setString(2, s.getName());
                    ps.setInt(3, s.getKills());
                    ps.setInt(4, s.getDeaths());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save stats to SQLite: " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ignored) {}
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
