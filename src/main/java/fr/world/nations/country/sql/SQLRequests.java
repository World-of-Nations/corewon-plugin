package fr.world.nations.country.sql;

import fr.world.nations.Core;
import fr.world.nations.country.Country;
import fr.world.nations.country.WonContry;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLRequests {

    private final SQLManager sqlManager;
    private final String table = "countries";

    public SQLRequests() {
        this.sqlManager = Core.getInstance().getModuleManager().getModule(WonContry.class).getSqlManager();
        createTable();
    }

    public void createTable() {
        try (Connection connection = sqlManager.getConnection()) {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name VARCHAR(255) NOT NULL UNIQUE,"
                    + "x INTEGER,"
                    + "y INTEGER,"
                    + "z INTEGER,"
                    + "world VARCHAR(255),"
                    + "available INTEGER DEFAULT 1"
                    + ");");
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de créer la table SQL...");
            e.printStackTrace();
        }
    }

    public void createCountry(String country) {
        try (Connection connection = sqlManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (name) VALUES (?);");
            preparedStatement.setString(1, country);
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de créer le pays " + country + " dans la table SQL...");
            e.printStackTrace();
        }
    }

    public List<Country> getAllCountries() {

        List<Country> contries = new ArrayList<>();

        try (Connection connection = sqlManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + table + ";");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Country name = new Country(resultSet.getString("name"));
                name.setAvailable(resultSet.getInt("available") != 0);
                if (resultSet.getString("world") != null) {
                    name.setSpawn(new Location(Bukkit.getWorld(resultSet.getString("world")),
                            resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")));
                }
                contries.add(name);
            }
            return contries;
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de récupérer les pays dans la table SQL...");
            e.printStackTrace();
            return contries;
        }

    }

    public void updateCountryAvailability(String country, boolean available) {
        try (Connection connection = sqlManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + table + " SET available = ? WHERE name = ?;");
            preparedStatement.setInt(1, available ? 1 : 0);
            preparedStatement.setString(2, country);
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de mettre à jour le pays " + country + " dans la table SQL...");
            e.printStackTrace();
        }
    }

    public void updateCountrySpawn(String country, Location spawn) {
        if (spawn.getWorld() == null) {
            Core.getInstance().getLogger().info("WonCountry | Impossible de mettre à jour le spawn du pays " + country + " car le monde n'existe pas...");
            return;
        }

        try (Connection connection = sqlManager.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + table + " SET x = ?, y = ?, z = ?, world = ? WHERE name = ?;");
            preparedStatement.setInt(1, spawn.getBlockX());
            preparedStatement.setInt(2, spawn.getBlockY());
            preparedStatement.setInt(3, spawn.getBlockZ());
            preparedStatement.setString(4, spawn.getWorld().getName());
            preparedStatement.setString(5, country);
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de mettre à jour le pays " + country + " dans la table SQL...");
            e.printStackTrace();
        }
    }
}
