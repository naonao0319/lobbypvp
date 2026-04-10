package com.example.pvparea.storage;

import com.example.pvparea.PlayerStats;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface StatsStorage {
    void load(Map<UUID, PlayerStats> out);
    void save(Collection<PlayerStats> snapshot);
    /** Wipe all persisted stats. */
    void clear();
    void close();
}
