package com.example.pvparea;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AreaManager {

    private final PvPAreaPlugin plugin;
    private final Map<String, PvPArea> areas = new HashMap<>();

    public AreaManager(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAreas() {
        areas.clear();
        FileConfiguration data = plugin.getDataFile().get();
        ConfigurationSection section = data.getConfigurationSection("areas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String worldName = section.getString(key + ".world");
            int minX = section.getInt(key + ".minX");
            int minY = section.getInt(key + ".minY");
            int minZ = section.getInt(key + ".minZ");
            int maxX = section.getInt(key + ".maxX");
            int maxY = section.getInt(key + ".maxY");
            int maxZ = section.getInt(key + ".maxZ");

            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                areas.put(key, new PvPArea(key, world, minX, minY, minZ, maxX, maxY, maxZ));
            }
        }
    }

    public void saveAreas() {
        FileConfiguration data = plugin.getDataFile().get();
        data.set("areas", null);

        for (Map.Entry<String, PvPArea> entry : areas.entrySet()) {
            PvPArea area = entry.getValue();
            String path = "areas." + entry.getKey();
            data.set(path + ".world", area.getWorld().getName());
            data.set(path + ".minX", area.getMinX());
            data.set(path + ".minY", area.getMinY());
            data.set(path + ".minZ", area.getMinZ());
            data.set(path + ".maxX", area.getMaxX());
            data.set(path + ".maxY", area.getMaxY());
            data.set(path + ".maxZ", area.getMaxZ());
        }

        plugin.getDataFile().save();
    }

    public boolean createArea(String name, Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        areas.put(name.toLowerCase(), new PvPArea(name, loc1.getWorld(), minX, minY, minZ, maxX, maxY, maxZ));
        saveAreas();
        return true;
    }

    public boolean removeArea(String name) {
        if (areas.remove(name.toLowerCase()) != null) {
            saveAreas();
            return true;
        }
        return false;
    }

    public boolean isInAnyArea(Location loc) {
        return getAreaAt(loc) != null;
    }

    public PvPArea getAreaAt(Location loc) {
        for (PvPArea area : areas.values()) {
            if (area.contains(loc)) {
                return area;
            }
        }
        return null;
    }

    public Set<String> getAreaNames() {
        return areas.keySet();
    }

    public static class PvPArea {
        private final String name;
        private final World world;
        private final int minX, minY, minZ;
        private final int maxX, maxY, maxZ;

        public PvPArea(String name, World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.name = name;
            this.world = world;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public boolean contains(Location loc) {
            if (!loc.getWorld().equals(world)) return false;
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        public String getName() { return name; }
        public World getWorld() { return world; }
        public int getMinX() { return minX; }
        public int getMinY() { return minY; }
        public int getMinZ() { return minZ; }
        public int getMaxX() { return maxX; }
        public int getMaxY() { return maxY; }
        public int getMaxZ() { return maxZ; }
    }
}
