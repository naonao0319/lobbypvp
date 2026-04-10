package com.example.pvparea;

import com.example.pvparea.storage.SqliteStatsStorage;
import com.example.pvparea.storage.StatsStorage;
import com.example.pvparea.storage.YamlStatsStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public class PvPAreaPlugin extends JavaPlugin {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static PvPAreaPlugin instance;

    private AreaManager areaManager;
    private StatsManager statsManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        areaManager = new AreaManager(this);
        statsManager = new StatsManager(this, createStorage());
        hologramManager = new HologramManager(this);

        areaManager.loadAreas();
        statsManager.load();
        hologramManager.loadHolograms();

        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        CommandManager cmdManager = new CommandManager(this);
        getCommand("pvparea").setExecutor(cmdManager);
        getCommand("pvparea").setTabCompleter(cmdManager);
        getCommand("killtop").setExecutor(cmdManager);

        scheduleTasks();

        getLogger().info("PvPAreaPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (areaManager != null) areaManager.saveAreas();
        if (statsManager != null) statsManager.close();
        if (hologramManager != null) {
            hologramManager.saveHolograms();
            hologramManager.clearHolograms();
        }
        getLogger().info("PvPAreaPlugin has been disabled!");
    }

    private StatsStorage createStorage() {
        String type = getConfig().getString("storage.type", "sqlite").toLowerCase();
        if ("yaml".equals(type)) {
            return new YamlStatsStorage(this);
        }
        try {
            return new SqliteStatsStorage(this);
        } catch (Throwable t) {
            getLogger().severe("Failed to init SQLite storage, falling back to YAML: " + t.getMessage());
            return new YamlStatsStorage(this);
        }
    }

    private void scheduleTasks() {
        long saveInterval = Math.max(20L, getConfig().getLong("storage.save-interval-ticks", 6000L));
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (statsManager != null) statsManager.flush();
        }, saveInterval, saveInterval);

        if (getConfig().getBoolean("actionbar.enabled", true)) {
            long interval = Math.max(1L, getConfig().getLong("actionbar.interval-ticks", 20L));
            new ActionBarTask(this).runTaskTimer(this, interval, interval);
        }

        long hologramInterval = Math.max(20L, getConfig().getLong("killtop.hologram-refresh-ticks", 100L));
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (hologramManager != null) hologramManager.updateHolograms();
        }, hologramInterval, hologramInterval);
    }

    /** Reload the config file and reschedule any tick-based tasks. */
    public void reloadPluginConfig() {
        reloadConfig();
        getServer().getScheduler().cancelTasks(this);
        scheduleTasks();
    }

    /** Parse a message from the {@code messages.*} config section with placeholder substitution. */
    public Component msg(String key, Object... placeholders) {
        String template = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            template = template.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }
        return MM.deserialize(template);
    }

    public static MiniMessage mm() {
        return MM;
    }

    public static PvPAreaPlugin getInstance() { return instance; }
    public AreaManager getAreaManager() { return areaManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public HologramManager getHologramManager() { return hologramManager; }
}
