package fr.world.nations.stats.data;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.stats.sql.SQLRequests;

import java.sql.ResultSet;
import java.util.ArrayList;

public class StatsManager {

    protected final ArrayList<FactionData> factions = new ArrayList<>();

    protected SQLRequests sqlRequests = new SQLRequests();


    public StatsManager() {
        loadData();
    }

    private void loadFactions() {

        Factions.getInstance().getAllFactions().forEach(faction -> {
            if (!faction.isWilderness() && !faction.isSafeZone() && !faction.isWarZone()) {
                factions.add(new FactionData(faction));
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

    public void saveData() {
        for (FactionData factionData : factions) {
            sqlRequests.updateFaction(factionData.getFaction().getTag(), "kills", factionData.getKills());
            sqlRequests.updateFaction(factionData.getFaction().getTag(), "deaths", factionData.getDeaths());
        }
    }

    public void addFaction(Faction faction) {
        if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) return;
        sqlRequests.createFaction(faction.getTag());
        factions.add(new FactionData(faction));
    }

    public void removeFaction(Faction faction) {
        sqlRequests.deleteFaction(faction.getTag());
        factions.remove(getFactionData(faction));
    }

    public FactionData getFactionData(String faction) {
        return factions.stream().filter(factionData -> factionData.getFaction().getTag().equals(faction)).findFirst().orElse(null);
    }

    public FactionData getFactionData(Faction faction) {
        return factions.stream().filter(factionData -> factionData.getFaction().equals(faction)).findFirst().orElse(null);
    }

}
