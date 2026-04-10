package com.example.pvparea;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatsManager {

    private final PvPAreaPlugin plugin;
    private File statsFile;
    private FileConfiguration statsConfig;

    private final Map<UUID, PlayerStats> stats = new HashMap<>();

    public StatsManager(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadStats() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create stats.yml");
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        stats.clear();
        ConfigurationSection section = statsConfig.getConfigurationSection("players");
        if (section != null) {
            for (String uuidStr : section.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    int kills = section.getInt(uuidStr + ".kills", 0);
                    int deaths = section.getInt(uuidStr + ".deaths", 0);
                    String name = section.getString(uuidStr + ".name", "Unknown");
                    stats.put(uuid, new PlayerStats(uuid, name, kills, deaths));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveStats() {
        statsConfig.set("players", null);
        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            PlayerStats ps = entry.getValue();
            String path = "players." + entry.getKey().toString();
            statsConfig.set(path + ".name", ps.getName());
            statsConfig.set(path + ".kills", ps.getKills());
            statsConfig.set(path + ".deaths", ps.getDeaths());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats.yml");
        }
    }

    public PlayerStats getStats(UUID uuid, String name) {
        return stats.computeIfAbsent(uuid, k -> new PlayerStats(uuid, name, 0, 0));
    }

    public void addKill(UUID uuid, String name) {
        PlayerStats s = getStats(uuid, name);
        s.addKill();
        saveStats();
        plugin.getHologramManager().updateHolograms();
    }

    public void addDeath(UUID uuid, String name) {
        PlayerStats s = getStats(uuid, name);
        s.addDeath();
        saveStats();
    }

    public List<PlayerStats> getTopKillers(int limit) {
        List<PlayerStats> list = new ArrayList<>(stats.values());
        list.sort((a, b) -> Integer.compare(b.getKills(), a.getKills())); // descending
        return list.subList(0, Math.min(limit, list.size()));
    }

    public static class PlayerStats {
        private final UUID uuid;
        private String name;
        private int kills;
        private int deaths;

        public PlayerStats(UUID uuid, String name, int kills, int deaths) {
            this.uuid = uuid;
            this.name = name;
            this.kills = kills;
            this.deaths = deaths;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
        public void addKill() { kills++; }
        public void addDeath() { deaths++; }

        public double getKD() {
            if (deaths == 0) return kills;
            return Math.round(((double) kills / deaths) * 100.0) / 100.0;
        }
    }
}
