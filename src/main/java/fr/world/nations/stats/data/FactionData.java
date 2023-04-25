package fr.world.nations.stats.data;

import com.massivecraft.factions.Faction;

import java.util.Map;

public class FactionData {

    private final Faction faction;
    private int kills;
    private int deaths;

    private double scoreZone = 0;

    private int assaultLose = 0;
    private int assaultWin = 0;

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

    public int getAssaultLose() {
        return assaultLose;
    }

    public void setAssaultLose(int assaultLose) {
        this.assaultLose = assaultLose;
    }

    public int getAssaultWin() {
        return assaultWin;
    }

    public void setAssaultWin(int assaultWin) {
        this.assaultWin = assaultWin;
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
    }

    public void addScoreZone(double delta) {
        scoreZone += delta;
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
