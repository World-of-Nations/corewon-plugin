package fr.world.nations.country;

import fr.world.nations.country.sql.SQLManager;
import fr.world.nations.country.sql.SQLRequests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CountryManager {
    protected final Map<String, Country> countries = new HashMap<>();

    protected final SQLRequests sqlRequests;

    public CountryManager(SQLManager sqlManager) {
        this.sqlRequests = new SQLRequests(sqlManager);
        loadData();
    }

    public void loadData() {
        List<Country> allCountries = sqlRequests.getAllCountries();
        for (Country country : allCountries) {
            countries.put(country.getName(), country);
        }
    }

    public void saveData() {
        for (Country country : countries.values()) {
            if (country.getSpawn() != null) {
                sqlRequests.updateCountrySpawn(country.getName(), country.getSpawn());
            }
            sqlRequests.updateCountryAvailability(country.getName(), country.isAvailable());
        }
    }

    public Country getCountry(String name) {
        return countries.get(name);
    }

    public void addCountry(String name) {
        sqlRequests.createCountry(name);
        countries.put(name, new Country(name));
    }

    public List<String> getAvailableCountryNames() {
        return countries.keySet().stream().filter(name -> countries.get(name).isAvailable()).collect(Collectors.toList());
    }

    public List<String> getCountryNames() {
        return countries.keySet().stream().toList();
    }
}
