package com.example.pvparea;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionListener implements Listener {

    private final PvPAreaPlugin plugin;

    private static final Map<UUID, Location> pos1Map = new HashMap<>();
    private static final Map<UUID, Location> pos2Map = new HashMap<>();

    public SelectionListener(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("pvparea.admin")) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOLDEN_AXE) return;

        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getLocation();
            pos1Map.put(player.getUniqueId(), loc);
            player.sendMessage(Component.text("Position 1 set to: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.GREEN));
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getLocation();
            pos2Map.put(player.getUniqueId(), loc);
            player.sendMessage(Component.text("Position 2 set to: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ(), NamedTextColor.GREEN));
        }
    }

    public static Location getPos1(UUID uuid) {
        return pos1Map.get(uuid);
    }

    public static Location getPos2(UUID uuid) {
        return pos2Map.get(uuid);
    }
}
