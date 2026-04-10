package com.example.pvparea;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarTask extends BukkitRunnable {

    private final PvPAreaPlugin plugin;

    public ActionBarTask(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getAreaManager().isInAnyArea(player.getLocation())) {
                StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId(), player.getName());

                int kills = stats.getKills();
                int deaths = stats.getDeaths();
                double kd = stats.getKD();
                int ping = player.getPing();

                Component message = Component.text("PvP Area | ", NamedTextColor.GOLD)
                        .append(Component.text("Kills: ", NamedTextColor.GRAY))
                        .append(Component.text(kills + " ", NamedTextColor.RED))
                        .append(Component.text("| Deaths: ", NamedTextColor.GRAY))
                        .append(Component.text(deaths + " ", NamedTextColor.RED))
                        .append(Component.text("| K/D: ", NamedTextColor.GRAY))
                        .append(Component.text(kd + " ", NamedTextColor.RED))
                        .append(Component.text("| Ping: ", NamedTextColor.GRAY))
                        .append(Component.text(ping + "ms", NamedTextColor.GREEN));

                player.sendActionBar(message);
            }
        }
    }
}
