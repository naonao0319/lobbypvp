package com.example.pvparea;

import org.bukkit.plugin.java.JavaPlugin;

public class PvPAreaPlugin extends JavaPlugin {

    private static PvPAreaPlugin instance;
    private AreaManager areaManager;
    private StatsManager statsManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        areaManager = new AreaManager(this);
        statsManager = new StatsManager(this);
        hologramManager = new HologramManager(this);

        areaManager.loadAreas();
        statsManager.loadStats();
        hologramManager.loadHolograms();

        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        CommandManager cmdManager = new CommandManager(this);
        getCommand("pvparea").setExecutor(cmdManager);
        getCommand("pvparea").setTabCompleter(cmdManager);
        getCommand("killtop").setExecutor(cmdManager);

        ActionBarTask actionBarTask = new ActionBarTask(this);
        actionBarTask.runTaskTimer(this, 20L, 20L); // every 1 second

        getLogger().info("PvPAreaPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (areaManager != null) areaManager.saveAreas();
        if (statsManager != null) statsManager.saveStats();
        if (hologramManager != null) {
            hologramManager.saveHolograms();
            hologramManager.clearHolograms(); // remove displays on reload/disable to respawn them later
        }

        getLogger().info("PvPAreaPlugin has been disabled!");
    }

    public static PvPAreaPlugin getInstance() {
        return instance;
    }

    public AreaManager getAreaManager() {
        return areaManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}
