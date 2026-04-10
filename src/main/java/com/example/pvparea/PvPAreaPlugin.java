package com.example.pvparea;

import com.example.pvparea.storage.SqliteStatsStorage;
import com.example.pvparea.storage.StatsStorage;
import com.example.pvparea.storage.YamlStatsStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PvPAreaPlugin extends JavaPlugin {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static PvPAreaPlugin instance;

    private AreaManager areaManager;
    private StatsManager statsManager;
    private HologramManager hologramManager;
    private MessageManager messageManager;
    private DataFile dataFile;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        applyConfigDefaults();

        dataFile = new DataFile(this);
        dataFile.load();

        messageManager = new MessageManager(this);
        messageManager.load();

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

    /** Reload the config file, language messages, and reschedule any tick-based tasks. */
    public void reloadPluginConfig() {
        reloadConfig();
        applyConfigDefaults();
        if (messageManager != null) messageManager.load();
        getServer().getScheduler().cancelTasks(this);
        scheduleTasks();
    }

    /**
     * Layer the bundled config.yml over the on-disk config as defaults. Any key the
     * user hasn't defined will transparently fall back to the value shipped in the jar,
     * so upgrading the plugin never leaves messages or settings looking empty.
     */
    private void applyConfigDefaults() {
        InputStream in = getResource("config.yml");
        if (in == null) return;
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        getConfig().setDefaults(defaults);
        getConfig().options().copyDefaults(true);
    }

    /** Parse a message from the language file with placeholder substitution. */
    public Component msg(String key, Object... placeholders) {
        return messageManager.get(key, placeholders);
    }

    /**
     * Parse a string that may contain legacy {@code &}-style color codes and/or
     * MiniMessage tags. If the string contains an {@code <…>} tag it is parsed as
     * MiniMessage, otherwise as legacy ampersand.
     */
    public static Component parseText(String s) {
        if (s == null) return Component.empty();
        if (s.indexOf('<') >= 0 && s.indexOf('>') >= 0) {
            return MM.deserialize(s);
        }
        return LEGACY.deserialize(s);
    }

    public static MiniMessage mm() {
        return MM;
    }

    public static PvPAreaPlugin getInstance() { return instance; }
    public AreaManager getAreaManager() { return areaManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DataFile getDataFile() { return dataFile; }
}
