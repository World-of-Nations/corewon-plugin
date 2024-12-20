package fr.world.nations.stats.data;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.stats.sql.SQLRequests;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsManager {

    protected static final List<FactionData> factions = new ArrayList<>();

    protected static SQLRequests sqlRequests = new SQLRequests();


    public StatsManager() {
        loadData();
    }

    public static void saveAllData() {
        for (FactionData factionData : factions) {
            Map<String, Object> map = factionData.getAsMap();
            for (String column : map.keySet()) {
                sqlRequests.updateFaction(factionData.getFaction().getTag(), column, map.get(column).toString());
            }
//            sqlRequests.updateFaction(factionData.getFaction().getTag(), "kills", factionData.getKills());
//            sqlRequests.updateFaction(factionData.getFaction().getTag(), "deaths", factionData.getDeaths());
        }
    }

    private void loadFactions() {
        Factions.getInstance().getAllFactions().forEach(faction -> {
            if (!faction.isWilderness() && !faction.isSafeZone() && !faction.isWarZone()) {
                factions.add(new FactionData(faction, this));
            }
        });
    }

    public void loadData() {
        loadFactions();
        for (FactionData factionData : factions) {
            ResultSet resultSet = sqlRequests.getFaction(factionData.getFaction().getTag());

            try {
                if (resultSet.next()) {
                    factionData.setKills(resultSet.getInt("kills"));
                    factionData.setDeaths(resultSet.getInt("deaths"));
                    factionData.setAssaultWin(resultSet.getInt("assault_win"));
                    factionData.setAssaultLose(resultSet.getInt("assault_lose"));
                    factionData.setScoreZone(resultSet.getInt("scorezone"));
                } else {
                    sqlRequests.createFaction(factionData.getFaction().getTag());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        ArrayList<String> dataFactions = sqlRequests.getAllFactions();

        for (String faction : dataFactions) {
            if (getFactionData(faction) == null) {
                sqlRequests.deleteFaction(faction);
            }
        }

    }

    public void saveData(String factionTag) {
        FactionData data = getFactionData(factionTag);
        if (data == null) return;
        saveData(data);
    }

    public void saveData(Faction faction) {
        FactionData data = getFactionData(faction);
        if (data == null) return;
        saveData(data);
    }

    public void saveData(FactionData data) {
        Map<String, Object> map = data.getAsMap();
        for (String column : map.keySet()) {
            sqlRequests.updateFaction(data.getFaction().getTag(), column, map.get(column).toString());
        }
    }

    public void addFaction(Faction faction) {
        if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) return;
        sqlRequests.createFaction(faction.getTag());
        factions.add(new FactionData(faction, this));
    }

    public void removeFaction(Faction faction) {
        FactionData data = getFactionData(faction);
        if (data == null) return;
        sqlRequests.deleteFaction(faction.getTag());
        factions.remove(data);
    }

    public FactionData getFactionData(String faction) {
        return factions.stream().filter(factionData -> factionData.getFaction().getTag().equalsIgnoreCase(faction)).findFirst().orElse(null);
    }

    public FactionData getFactionData(Faction faction) {
        return factions.stream().filter(factionData -> factionData.getFaction().equals(faction)).findFirst().orElse(null);
    }

    public List<FactionData> getFactionDatas() {
        return factions;
    }
}
