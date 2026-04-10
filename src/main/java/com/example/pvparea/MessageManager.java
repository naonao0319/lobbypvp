package com.example.pvparea;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads messages from a language file inside the {@code messages/} folder.
 * The active language is determined by the {@code language} key in config.yml.
 */
public class MessageManager {

    private final PvPAreaPlugin plugin;
    private YamlConfiguration messages;

    public MessageManager(PvPAreaPlugin plugin) {
        this.plugin = plugin;
    }

    /** (Re)load the language file that matches the configured language. */
    public void load() {
        String lang = plugin.getConfig().getString("language", "en").toLowerCase();

        // Save the bundled language files if missing on disk.
        saveDefaultLang("en");
        saveDefaultLang("ja");

        File langFile = new File(plugin.getDataFolder(), "messages/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file '" + lang + ".yml' not found, falling back to en.yml");
            langFile = new File(plugin.getDataFolder(), "messages/en.yml");
        }

        messages = YamlConfiguration.loadConfiguration(langFile);

        // Layer the bundled file as defaults so new keys are always available.
        InputStream bundled = plugin.getResource("messages/" + lang + ".yml");
        if (bundled == null) bundled = plugin.getResource("messages/en.yml");
        if (bundled != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(bundled, StandardCharsets.UTF_8));
            messages.setDefaults(defaults);
        }
    }

    private void saveDefaultLang(String lang) {
        String resource = "messages/" + lang + ".yml";
        if (plugin.getResource(resource) != null) {
            File outFile = new File(plugin.getDataFolder(), resource);
            if (!outFile.exists()) {
                plugin.saveResource(resource, false);
            }
        }
    }

    /** Get a raw message string for the given key, with placeholder substitution. */
    public String raw(String key, Object... placeholders) {
        String template = messages.getString(key, key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            template = template.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }
        return template;
    }

    /** Get a parsed {@link Component} for the given key, with placeholder substitution. */
    public Component get(String key, Object... placeholders) {
        return PvPAreaPlugin.parseText(raw(key, placeholders));
    }
}
