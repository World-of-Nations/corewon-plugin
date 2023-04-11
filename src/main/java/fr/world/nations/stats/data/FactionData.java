package fr.world.nations.stats.data;

import com.massivecraft.factions.Faction;

public class FactionData {

    private final Faction faction;
    private int kills;
    private int deaths;

    public FactionData(Faction faction) {
        this.faction = faction;
    }

    public Faction getFaction() {
        return faction;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getKdr() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / (double) deaths;
    }

}
