package com.example.pvparea;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AreaEntryListener implements Listener {

    private final PvPAreaPlugin plugin;
    private final Map<UUID, String> currentArea = new HashMap<>();

    public AreaEntryListener(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;
        handle(event.getPlayer(), to);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        handle(event.getPlayer(), event.getTo());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        currentArea.remove(event.getPlayer().getUniqueId());
    }

    private void handle(Player player, Location to) {
        AreaManager.PvPArea area = plugin.getAreaManager().getAreaAt(to);
        String newName = area == null ? null : area.getName().toLowerCase();
        String oldName = currentArea.get(player.getUniqueId());

        if (newName == null) {
            currentArea.remove(player.getUniqueId());
            return;
        }
        if (newName.equals(oldName)) return;

        currentArea.put(player.getUniqueId(), newName);

        if (!plugin.getConfig().getBoolean("area-entry.broadcast", true)) return;
        Bukkit.getServer().sendMessage(plugin.msg("area-enter-broadcast",
                "player", player.getName(), "area", area.getName()));
    }
}
