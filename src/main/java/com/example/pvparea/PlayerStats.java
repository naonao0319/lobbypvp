package com.example.pvparea;

import java.util.UUID;

public class PlayerStats {

    private final UUID uuid;
    private String name;
    private int kills;
    private int deaths;

    public PlayerStats(UUID uuid, String name, int kills, int deaths) {
        this.uuid = uuid;
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public void addKill() { kills++; }
    public void addDeath() { deaths++; }

    public double getKD() {
        double ratio = deaths == 0 ? kills : (double) kills / deaths;
        return Math.round(ratio * 10.0) / 10.0;
    }
}
