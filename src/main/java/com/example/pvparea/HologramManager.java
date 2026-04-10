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
        FileConfiguration data = plugin.getDataFile().get();
        ConfigurationSection section = data.getConfigurationSection("holograms");
        if (section != null) holoNames.addAll(section.getKeys(false));
        updateHolograms();
    }

    public void saveHolograms() {
        FileConfiguration data = plugin.getDataFile().get();
        data.set("holograms", null);
        for (String name : holoNames) {
            data.set("holograms." + name, true);
        }
        plugin.getDataFile().save();
    }

    public void createHologram(String name, Location loc) {
        String fullName = "pvparea_" + name.toLowerCase();
        de.oliver.fancyholograms.api.HologramManager fh = FancyHologramsPlugin.get().getHologramManager();
        fh.getHologram(fullName).ifPresent(fh::removeHologram);

        TextHologramData data = new TextHologramData(fullName, loc);
        Hologram hologram = fh.create(data);
        fh.addHologram(hologram);

        holoNames.add(name.toLowerCase());
        saveHolograms();
        updateHolograms();
    }

    public boolean removeHologram(String name) {
        String fullName = "pvparea_" + name.toLowerCase();
        de.oliver.fancyholograms.api.HologramManager fh = FancyHologramsPlugin.get().getHologramManager();
        Hologram hologram = fh.getHologram(fullName).orElse(null);
        if (hologram != null) {
            fh.removeHologram(hologram);
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
        // FancyHolograms handles despawning automatically.
    }

    public void updateHolograms() {
        if (holoNames.isEmpty()) return;
        int limit = plugin.getConfig().getInt("killtop.limit", 10);
        List<PlayerStats> top = plugin.getStatsManager().getTopKillers(limit);
        List<String> lines = buildLines(top);

        de.oliver.fancyholograms.api.HologramManager fh = FancyHologramsPlugin.get().getHologramManager();
        for (String name : holoNames) {
            String fullName = "pvparea_" + name;
            Hologram hologram = fh.getHologram(fullName).orElse(null);
            if (hologram != null && hologram.getData() instanceof TextHologramData textData) {
                textData.setText(lines);
                hologram.forceUpdate();
            }
        }
    }

    private List<String> buildLines(List<PlayerStats> top) {
        List<String> lines = new ArrayList<>();
        lines.add(plugin.getConfig().getString("killtop.header", "<gray>--- Kill Top ---</gray>"));
        if (top.isEmpty()) {
            lines.add(plugin.getConfig().getString("killtop.empty", "<gray>No data yet.</gray>"));
            return lines;
        }
        String template = plugin.getConfig().getString("killtop.line",
                "<yellow>#{rank}</yellow> <white>{name}</white> <gray>-</gray> <red>{kills} Kills</red>");
        int rank = 1;
        for (PlayerStats ps : top) {
            lines.add(template
                    .replace("{rank}", String.valueOf(rank))
                    .replace("{name}", ps.getName())
                    .replace("{kills}", String.valueOf(ps.getKills())));
            rank++;
        }
        return lines;
    }
}
