package com.example.pvparea;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final PvPAreaPlugin plugin;

    public CommandManager(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pvparea")) {
            if (!sender.hasPermission("pvparea.admin")) {
                sender.sendMessage(Component.text("You don't have permission to do this.", NamedTextColor.RED));
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(Component.text("Usage: /pvparea <create|remove|hologram> ...", NamedTextColor.RED));
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                return true;
            }

            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("create")) {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /pvparea create <name>", NamedTextColor.RED));
                    return true;
                }
                String name = args[1];

                Location loc1 = SelectionListener.getPos1(player.getUniqueId());
                Location loc2 = SelectionListener.getPos2(player.getUniqueId());

                if (loc1 == null || loc2 == null) {
                    player.sendMessage(Component.text("You must select two positions with a Golden Axe first.", NamedTextColor.RED));
                    return true;
                }

                if (plugin.getAreaManager().createArea(name, loc1, loc2)) {
                    player.sendMessage(Component.text("PvP Area '" + name + "' created successfully!", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Positions are not in the same world.", NamedTextColor.RED));
                }

            } else if (subCommand.equals("remove")) {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /pvparea remove <name>", NamedTextColor.RED));
                    return true;
                }
                String name = args[1];
                if (plugin.getAreaManager().removeArea(name)) {
                    player.sendMessage(Component.text("PvP Area '" + name + "' removed.", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Area not found.", NamedTextColor.RED));
                }

            } else if (subCommand.equals("hologram")) {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /pvparea hologram <create|remove> [name]", NamedTextColor.RED));
                    return true;
                }
                String action = args[1].toLowerCase();

                if (action.equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage(Component.text("Usage: /pvparea hologram create <name>", NamedTextColor.RED));
                        return true;
                    }
                    String holoName = args[2];
                    plugin.getHologramManager().createHologram(holoName, player.getLocation());
                    player.sendMessage(Component.text("Hologram '" + holoName + "' created at your location.", NamedTextColor.GREEN));

                } else if (action.equals("remove")) {
                    if (args.length < 3) {
                        player.sendMessage(Component.text("Usage: /pvparea hologram remove <name>", NamedTextColor.RED));
                        return true;
                    }
                    String holoName = args[2];
                    if (plugin.getHologramManager().removeHologram(holoName)) {
                        player.sendMessage(Component.text("Hologram removed.", NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("Hologram not found.", NamedTextColor.RED));
                    }
                }
            } else {
                player.sendMessage(Component.text("Unknown sub-command.", NamedTextColor.RED));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("killtop")) {
            List<StatsManager.PlayerStats> topKillers = plugin.getStatsManager().getTopKillers(10);
            sender.sendMessage(Component.text("--- ", NamedTextColor.GRAY).append(Component.text("Kill Top", NamedTextColor.GOLD)).append(Component.text(" ---", NamedTextColor.GRAY)));

            if (topKillers.isEmpty()) {
                sender.sendMessage(Component.text("No data yet.", NamedTextColor.GRAY));
            } else {
                int rank = 1;
                for (StatsManager.PlayerStats stats : topKillers) {
                    sender.sendMessage(Component.text("#" + rank + " ", NamedTextColor.YELLOW)
                            .append(Component.text(stats.getName() + " ", NamedTextColor.WHITE))
                            .append(Component.text("- ", NamedTextColor.GRAY))
                            .append(Component.text(stats.getKills() + " Kills", NamedTextColor.RED)));
                    rank++;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("pvparea")) {
            if (!sender.hasPermission("pvparea.admin")) return completions;

            if (args.length == 1) {
                completions.add("create");
                completions.add("remove");
                completions.add("hologram");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("remove")) {
                    completions.addAll(plugin.getAreaManager().getAreaNames());
                } else if (args[0].equalsIgnoreCase("hologram")) {
                    completions.add("create");
                    completions.add("remove");
                }
            }
        }
        return completions;
    }
}
