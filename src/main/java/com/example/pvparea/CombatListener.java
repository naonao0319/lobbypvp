package com.example.pvparea;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {

    private final PvPAreaPlugin plugin;

    public CombatListener(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (!plugin.getAreaManager().isInAnyArea(victim.getLocation())) return;

        plugin.getStatsManager().addDeath(victim.getUniqueId(), victim.getName());

        if (killer != null && !killer.equals(victim)) {
            plugin.getStatsManager().addKill(killer.getUniqueId(), killer.getName());
            playKillEffects(killer, victim);
            giveKillRewards(killer);
        }
    }

    private void giveKillRewards(Player killer) {
        ConfigurationSection rewards = plugin.getConfig().getConfigurationSection("kill-rewards");
        if (rewards == null) return;

        ConfigurationSection apple = rewards.getConfigurationSection("golden-apple");
        if (apple != null && apple.getBoolean("enabled", true)) {
            int amount = Math.max(1, apple.getInt("amount", 3));
            killer.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, amount));
        }

        ConfigurationSection hp = rewards.getConfigurationSection("max-health");
        if (hp != null && hp.getBoolean("enabled", true)) {
            AttributeInstance attr = killer.getAttribute(Attribute.MAX_HEALTH);
            if (attr == null) return;
            double increase = hp.getDouble("increase", 5.0);
            double cap = hp.getDouble("max", 40.0);
            double current = attr.getBaseValue();
            if (current >= cap) return;
            double next = Math.min(cap, current + increase);
            attr.setBaseValue(next);
            killer.setHealth(Math.min(killer.getHealth() + (next - current), next));
        }
    }

    private void playKillEffects(Player killer, Player victim) {
        ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("kill-effects");
        if (cfg == null) return;

        ConfigurationSection sound = cfg.getConfigurationSection("sound");
        if (sound != null && sound.getBoolean("enabled", true)) {
            String name = sound.getString("name", "ENTITY_PLAYER_LEVELUP");
            try {
                Sound s = Sound.valueOf(name.toUpperCase());
                float volume = (float) sound.getDouble("volume", 1.0);
                float pitch = (float) sound.getDouble("pitch", 1.2);
                killer.playSound(killer.getLocation(), s, volume, pitch);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid kill sound name: " + name);
            }
        }

        ConfigurationSection particle = cfg.getConfigurationSection("particles");
        if (particle != null && particle.getBoolean("enabled", true)) {
            String type = particle.getString("type", "CRIT");
            try {
                Particle p = Particle.valueOf(type.toUpperCase());
                int count = particle.getInt("count", 12);
                double ox = particle.getDouble("offset-x", 0.3);
                double oy = particle.getDouble("offset-y", 0.6);
                double oz = particle.getDouble("offset-z", 0.3);
                double speed = particle.getDouble("speed", 0.05);
                Location loc = victim.getLocation().add(0, 1, 0);
                victim.getWorld().spawnParticle(p, loc, count, ox, oy, oz, speed);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid kill particle type: " + type);
            }
        }
    }
}
