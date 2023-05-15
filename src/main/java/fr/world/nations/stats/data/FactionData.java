package fr.world.nations.stats.data;

import com.massivecraft.factions.Faction;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;

import java.util.Map;

public class FactionData {

    private final Faction faction;
    private int kills = 0;
    private int deaths = 0;

    private double scoreZone = 0;

    private int assaultLose = 0;
    private int assaultWin = 0;

    public FactionData(Faction faction, StatsManager statsManager) {
        this.faction = faction;
        statsManager.saveData(this);
    }

    public Faction getFaction() {
        return faction;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
        save();
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
        save();
    }

    public double getKdr() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / (double) deaths;
    }

    public int getAssaultLose() {
        return assaultLose;
    }

    public void setAssaultLose(int assaultLose) {
        this.assaultLose = assaultLose;
        save();
    }

    public int getAssaultWin() {
        return assaultWin;
    }

    public void setAssaultWin(int assaultWin) {
        this.assaultWin = assaultWin;
        save();
    }

    public int addAssaultLose() {
        return addAssaultLose(1);
    }

    public int addAssaultLose(int n) {
        setAssaultLose(getAssaultLose() + n);
        return getAssaultLose();
    }

    public int addAssaultWin() {
        return addAssaultWin(1);
    }

    public int addAssaultWin(int n) {
        setAssaultWin(getAssaultWin() + n);
        return getAssaultWin();
    }

    public int getAssaultScore() {
        return getAssaultWin() - getAssaultLose();
    }

    public double getScoreZone() {
        return scoreZone;
    }

    public void setScoreZone(double scoreZone) {
        this.scoreZone = scoreZone;
        save();
    }

    public void addScoreZone(double delta) {
        setScoreZone(getScoreZone() + delta);
    }

    public void save() {
        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();
        if (statsManager == null) {
            //Is expected to happen while StatsManager is being initialized
            return;
        }
        statsManager.saveData(this);
    }

    public Map<String, Object> getAsMap() {
        return Map.of(
                "kills", getKills(),
                "deaths", getDeaths(),
                "assault_lose", getAssaultLose(),
                "assault_win", getAssaultWin(),
                "score_zone", getScoreZone()
        );
    }
}
