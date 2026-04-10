package com.example.pvparea;

import com.example.pvparea.storage.StatsStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps player stats in-memory and coordinates persistence via a {@link StatsStorage}.
 *
 * <p>All read/write helpers are expected to run on the main server thread. Actual disk
 * writes are dispatched asynchronously by {@link #flush()} to avoid blocking the tick.
 */
public class StatsManager {

    private final PvPAreaPlugin plugin;
    private final StatsStorage storage;
    private final Map<UUID, PlayerStats> stats = new HashMap<>();
    private boolean dirty = false;

    public StatsManager(PvPAreaPlugin plugin, StatsStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void load() {
        stats.clear();
        storage.load(stats);
    }

    /** Snapshot current stats on the main thread and save them on an async thread. */
    public void flush() {
        if (!dirty) return;
        List<PlayerStats> snapshot = new ArrayList<>(stats.size());
        for (PlayerStats ps : stats.values()) {
            snapshot.add(new PlayerStats(ps.getUuid(), ps.getName(), ps.getKills(), ps.getDeaths()));
        }
        dirty = false;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> storage.save(snapshot));
    }

    /** Wipe every player's stats both in memory and on disk. */
    public void clearAll() {
        stats.clear();
        dirty = false;
        storage.clear();
    }

    /** Synchronous save + close. Intended for {@code onDisable}. */
    public void close() {
        if (dirty) {
            storage.save(new ArrayList<>(stats.values()));
            dirty = false;
        }
        storage.close();
    }

    public PlayerStats getStats(UUID uuid, String name) {
        PlayerStats ps = stats.get(uuid);
        if (ps == null) {
            ps = new PlayerStats(uuid, name, 0, 0);
            stats.put(uuid, ps);
            dirty = true;
        } else if (!ps.getName().equals(name)) {
            ps.setName(name);
            dirty = true;
        }
        return ps;
    }

    public void addKill(UUID uuid, String name) {
        getStats(uuid, name).addKill();
        dirty = true;
    }

    public void addDeath(UUID uuid, String name) {
        getStats(uuid, name).addDeath();
        dirty = true;
    }

    public List<PlayerStats> getTopKillers(int limit) {
        if (stats.isEmpty()) return Collections.emptyList();
        List<PlayerStats> list = new ArrayList<>(stats.values());
        list.sort((a, b) -> Integer.compare(b.getKills(), a.getKills()));
        if (list.size() > limit) return new ArrayList<>(list.subList(0, limit));
        return list;
    }
}
