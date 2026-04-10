package com.example.pvparea;

import net.kyori.adventure.text.Component;
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
        String format = plugin.getConfig().getString("actionbar.format",
                "<gold>PvP Area</gold> <gray>Kills:</gray> <red>{kills}</red>");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!plugin.getAreaManager().isInAnyArea(player.getLocation())) continue;

            PlayerStats stats = plugin.getStatsManager().getStats(player.getUniqueId(), player.getName());
            String rendered = format
                    .replace("{kills}", String.valueOf(stats.getKills()))
                    .replace("{deaths}", String.valueOf(stats.getDeaths()))
                    .replace("{kd}", String.valueOf(stats.getKD()))
                    .replace("{ping}", String.valueOf(player.getPing()));
            Component message = PvPAreaPlugin.parseText(rendered);
            player.sendActionBar(message);
        }
    }
}
