package fr.world.nations.stats.sql;

import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLRequests {

    private final SQLManager sqlManager;
    private final String table = "WonStats";

    public SQLRequests() {
        this.sqlManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getSqlManager();
        createTable();
    }

    public void createTable() {
        try {
            String statement = "CREATE TABLE IF NOT EXISTS " + table + " ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "faction VARCHAR(255),"
                    + "kills INTEGER,"
                    + "deaths INTEGER,"
                    + "assault_lose INTEGER,"
                    + "assault_win INTEGER,"
                    + "score_zone FLOAT"
                    + ");";
            sqlManager.getConnection().createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de créer la table SQL...");
            e.printStackTrace();
        }
    }

    public void createFaction(String faction) {
        try {
            sqlManager.getConnection().createStatement().executeUpdate("INSERT INTO " + table + " (faction, kills, deaths) VALUES ('" + faction + "', 0, 0);");
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de créer la faction " + faction + " dans la table SQL...");
            e.printStackTrace();
        }
    }

    public void deleteFaction(String faction) {
        try {
            sqlManager.getConnection().createStatement().executeUpdate("DELETE FROM " + table + " WHERE faction = '" + faction + "';");
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de supprimer la faction " + faction + " dans la table SQL...");
            e.printStackTrace();
        }
    }

    public ArrayList<String> getAllFactions() {

        ArrayList<String> factions = new ArrayList<>();

        try {
            ResultSet resultSet = sqlManager.getConnection().createStatement().executeQuery("SELECT * FROM " + table + ";");

            while (resultSet.next()) {
                factions.add(resultSet.getString("faction"));
            }

            return factions;
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de récupérer les factions dans la table SQL...");
            e.printStackTrace();
            return factions;
        }

    }

    public void updateFaction(String faction, String column, String value) {
        if (value == null) {
            Core.getInstance().getLogger().warning("Tried to save null value : " + column + " for faction " + faction);
            return;
        }
        try {
            sqlManager.getConnection().createStatement().executeUpdate("UPDATE " + table + " SET " + column + " = " + value + " WHERE faction = '" + faction + "';");
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de mettre à jour la faction " + faction + " dans la table SQL...");
            e.printStackTrace();
        }
    }

    public void updateFaction(String faction, String column, int value) {
        try {
            sqlManager.getConnection().createStatement().executeUpdate("UPDATE " + table + " SET " + column + " = " + value + " WHERE faction = '" + faction + "';");
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de mettre à jour la faction " + faction + " dans la table SQL...");
            e.printStackTrace();
        }
    }

    public ResultSet getFaction(String faction) {
        try {
            return sqlManager.getConnection().createStatement().executeQuery("SELECT * FROM " + table + " WHERE faction = '" + faction + "';");
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de récupérer la faction " + faction + " dans la table SQL...");
            e.printStackTrace();
            return null;
        }
    }

}
