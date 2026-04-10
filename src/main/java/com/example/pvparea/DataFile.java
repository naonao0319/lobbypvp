package com.example.pvparea;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Manages a separate {@code data.yml} file for plugin-managed data (areas, holograms)
 * so that {@code config.yml} is never programmatically overwritten and user edits are preserved.
 */
public class DataFile {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration data;

    public DataFile(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    /** Load (or reload) data.yml from disk. */
    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    /** Save data.yml to disk. */
    public void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    /** Get the underlying {@link FileConfiguration}. */
    public FileConfiguration get() {
        if (data == null) load();
        return data;
    }
}
