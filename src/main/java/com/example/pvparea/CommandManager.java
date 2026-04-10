package com.example.pvparea;

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
                sender.sendMessage(plugin.msg("no-permission"));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(plugin.msg("usage-root"));
                return true;
            }

            String sub = args[0].toLowerCase();

            if (sub.equals("reload")) {
                plugin.reloadPluginConfig();
                sender.sendMessage(plugin.msg("config-reloaded"));
                return true;
            }

            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.msg("player-only"));
                return true;
            }

            switch (sub) {
                case "create" -> handleCreate(player, args);
                case "remove" -> handleRemove(player, args);
                case "hologram" -> handleHologram(player, args);
                default -> player.sendMessage(plugin.msg("unknown-subcommand"));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("killtop")) {
            int limit = plugin.getConfig().getInt("killtop.limit", 10);
            List<PlayerStats> top = plugin.getStatsManager().getTopKillers(limit);

            sender.sendMessage(PvPAreaPlugin.mm().deserialize(
                    plugin.getConfig().getString("killtop.header", "<gray>--- Kill Top ---</gray>")));

            if (top.isEmpty()) {
                sender.sendMessage(PvPAreaPlugin.mm().deserialize(
                        plugin.getConfig().getString("killtop.empty", "<gray>No data yet.</gray>")));
            } else {
                String template = plugin.getConfig().getString("killtop.line",
                        "<yellow>#{rank}</yellow> <white>{name}</white> <gray>-</gray> <red>{kills} Kills</red>");
                int rank = 1;
                for (PlayerStats ps : top) {
                    sender.sendMessage(PvPAreaPlugin.mm().deserialize(template
                            .replace("{rank}", String.valueOf(rank))
                            .replace("{name}", ps.getName())
                            .replace("{kills}", String.valueOf(ps.getKills()))));
                    rank++;
                }
            }
            return true;
        }

        return false;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.msg("usage-create"));
            return;
        }
        String name = args[1];
        Location loc1 = SelectionListener.getPos1(player.getUniqueId());
        Location loc2 = SelectionListener.getPos2(player.getUniqueId());
        if (loc1 == null || loc2 == null) {
            player.sendMessage(plugin.msg("need-selection"));
            return;
        }
        if (plugin.getAreaManager().createArea(name, loc1, loc2)) {
            player.sendMessage(plugin.msg("area-created", "name", name));
        } else {
            player.sendMessage(plugin.msg("area-not-same-world"));
        }
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.msg("usage-remove"));
            return;
        }
        String name = args[1];
        if (plugin.getAreaManager().removeArea(name)) {
            player.sendMessage(plugin.msg("area-removed", "name", name));
        } else {
            player.sendMessage(plugin.msg("area-not-found"));
        }
    }

    private void handleHologram(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.msg("usage-hologram"));
            return;
        }
        String action = args[1].toLowerCase();
        String holoName = args[2];
        if (action.equals("create")) {
            plugin.getHologramManager().createHologram(holoName, player.getLocation());
            player.sendMessage(plugin.msg("hologram-created", "name", holoName));
        } else if (action.equals("remove")) {
            if (plugin.getHologramManager().removeHologram(holoName)) {
                player.sendMessage(plugin.msg("hologram-removed"));
            } else {
                player.sendMessage(plugin.msg("hologram-not-found"));
            }
        } else {
            player.sendMessage(plugin.msg("usage-hologram"));
        }
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
                completions.add("reload");
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
