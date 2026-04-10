package com.example.pvparea;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HologramManager {

    private final PvPAreaPlugin plugin;
    private final Set<String> holoNames = new HashSet<>();

    public HologramManager(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadHolograms() {
        holoNames.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("holograms");
        if (section != null) {
            holoNames.addAll(section.getKeys(false));
        }
        updateHolograms();
    }

    public void saveHolograms() {
        FileConfiguration config = plugin.getConfig();
        config.set("holograms", null);
        for (String name : holoNames) {
            config.set("holograms." + name, true);
        }
        plugin.saveConfig();
    }

    public void createHologram(String name, Location loc) {
        String fullName = "pvparea_" + name.toLowerCase();
        
        de.oliver.fancyholograms.api.HologramManager fhManager = FancyHologramsPlugin.get().getHologramManager();
        
        // Remove if exists
        fhManager.getHologram(fullName).ifPresent(fhManager::removeHologram);

        TextHologramData data = new TextHologramData(fullName, loc);
        Hologram hologram = fhManager.create(data);
        fhManager.addHologram(hologram);

        holoNames.add(name.toLowerCase());
        saveHolograms();
        updateHolograms();
    }

    public boolean removeHologram(String name) {
        String fullName = "pvparea_" + name.toLowerCase();
        de.oliver.fancyholograms.api.HologramManager fhManager = FancyHologramsPlugin.get().getHologramManager();
        
        Hologram hologram = fhManager.getHologram(fullName).orElse(null);
        if (hologram != null) {
            fhManager.removeHologram(hologram);
            holoNames.remove(name.toLowerCase());
            saveHolograms();
            return true;
        }
        
        if (holoNames.remove(name.toLowerCase())) {
            saveHolograms();
            return true;
        }
        return false;
    }

    public void clearHolograms() {
        // Nothing to do since FancyHolograms handles despawning
    }

    public void updateHolograms() {
        List<StatsManager.PlayerStats> topKillers = plugin.getStatsManager().getTopKillers(10);
        List<String> lines = generateTopKillersLines(topKillers);

        de.oliver.fancyholograms.api.HologramManager fhManager = FancyHologramsPlugin.get().getHologramManager();

        for (String name : holoNames) {
            String fullName = "pvparea_" + name;
            Hologram hologram = fhManager.getHologram(fullName).orElse(null);
            if (hologram != null && hologram.getData() instanceof TextHologramData textData) {
                textData.setText(lines);
                hologram.forceUpdate();
            }
        }
    }

    private List<String> generateTopKillersLines(List<StatsManager.PlayerStats> topKillers) {
        List<String> lines = new ArrayList<>();
        lines.add("<gray>--- <gold><bold>Kill Top</bold></gold> ---</gray>");

        if (topKillers.isEmpty()) {
            lines.add("<gray>No data yet.</gray>");
        } else {
            int rank = 1;
            for (StatsManager.PlayerStats stats : topKillers) {
                lines.add("<yellow>#" + rank + "</yellow> <white>" + stats.getName() + "</white> <gray>-</gray> <red>" + stats.getKills() + " Kills</red>");
                rank++;
            }
        }
        return lines;
    }
}
