package com.example.pvparea.storage;

import com.example.pvparea.PlayerStats;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class YamlStatsStorage implements StatsStorage {

    private final JavaPlugin plugin;
    private final File file;

    public YamlStatsStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create stats.yml: " + e.getMessage());
            }
        }
    }

    @Override
    public void load(Map<UUID, PlayerStats> out) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("players");
        if (section == null) return;
        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String name = section.getString(uuidStr + ".name", "Unknown");
                int kills = section.getInt(uuidStr + ".kills", 0);
                int deaths = section.getInt(uuidStr + ".deaths", 0);
                out.put(uuid, new PlayerStats(uuid, name, kills, deaths));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void save(Collection<PlayerStats> snapshot) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (PlayerStats ps : snapshot) {
            String path = "players." + ps.getUuid();
            yaml.set(path + ".name", ps.getName());
            yaml.set(path + ".kills", ps.getKills());
            yaml.set(path + ".deaths", ps.getDeaths());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats.yml: " + e.getMessage());
        }
    }

    @Override
    public void close() {
    }
}
