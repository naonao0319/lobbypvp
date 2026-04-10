package com.example.pvparea;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatListener implements Listener {

    private final PvPAreaPlugin plugin;

    public CombatListener(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        boolean victimInArea = plugin.getAreaManager().isInAnyArea(victim.getLocation());

        if (victimInArea) {
            plugin.getStatsManager().addDeath(victim.getUniqueId(), victim.getName());

            if (killer != null && !killer.equals(victim)) {
                plugin.getStatsManager().addKill(killer.getUniqueId(), killer.getName());
            }
        }
    }
}
