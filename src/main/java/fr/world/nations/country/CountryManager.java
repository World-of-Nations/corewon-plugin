package fr.world.nations.country;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.world.nations.util.JsonUtil;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryManager {

    private final Map<String, Country> countryMap = new HashMap<>();
    private final File folder;
    private int emergencyIdNumb = 0;
    private static CountryManager instance;

    public CountryManager(WonContry module) {
        instance = this;
        folder = new File(module.getConfigFolder(), "storage");
        if (!folder.exists()) {
            folder.mkdir();
        } else {
            for (Country country : loadData()) {
                countryMap.put(country.getId(), country);
            }
        }
        if (countryMap.isEmpty()) {
            SQLManager sqlManager = new SQLManager(module);
            SQLRequests sqlRequests = new SQLRequests(sqlManager);
            for (Country country : sqlRequests.getAllCountries(this)) {
                countryMap.put(country.getId(), country);
            }
            try {
                sqlManager.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        saveData();
    }

    public static CountryManager getInstance() {
        return instance;
    }

    public static void setInstance(CountryManager instance) {
        CountryManager.instance = instance;
    }

    public void saveData() {
        for (Country country : getAllCountries())
            saveCountry(country);
    }

    private File getFile(Country country) {
        return getFile(country, false);
    }

    private File getFile(Country country, boolean create) {
        File countryFile = new File(folder, country.getId() + ".json");
        if (!countryFile.exists()) {
            if (!create) return null;
            try {
                countryFile.createNewFile();
                JsonUtil.write(countryFile, JsonNodeFactory.instance.objectNode());
                return countryFile;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return countryFile;
    }

    public void saveCountry(Country country) {
        File file = getFile(country, true);
        if (file == null) {
            System.err.println("Failed to save " + country.getName() + " country");
            return;
        }
        ObjectNode objectNode = JsonUtil.getObjectNode(file);
        objectNode.put("id", country.getId());
        objectNode.put("name", country.getName());
        objectNode.put("available", country.isAvailable());
        objectNode.put("spawn_location", JsonUtil.wrapLocation(country.getSpawn(), true, true));
        try {
            objectNode.put("flag", new ObjectMapper().writeValueAsString(country.getFlag()));
        } catch (IOException e) {
            System.err.println("Could not write country " + country.getName() + "'s flag");
        }
        JsonUtil.write(file, objectNode);
    }

    @Nullable
    public Country createCountry(String countryId, String countryName, Location spawn, boolean available, int[] flag) throws IllegalArgumentException {
        if (getCountryById(countryId) != null)
            throw new IllegalArgumentException("Country with id " + countryId + " already exists");
        if (getCountry(countryName) != null)
            throw new IllegalArgumentException("Country with name " + countryName + " already exists");
        Country country = new Country(this, countryId, countryName, spawn, available, flag);
        saveCountry(country);
        return country;
    }

    @Nullable
    public Country createCountry(String countryId, String countryName, Location spawn, boolean available) throws IllegalArgumentException {
        return createCountry(countryId, countryName, spawn, available, new int[512]);
    }

    @Deprecated
    public Country createCountry(String name) throws IllegalArgumentException {
        if (getCountry(name) != null)
            throw new IllegalArgumentException("Country with name " + name + " already exists");
        System.err.println("Le pays " + name + " n'a pas d'id définie ! Veuillez effectuer la commande /f country setid " + name + " <id> !");
        Country country = new Country(this, String.valueOf(emergencyIdNumb++), name);
        saveCountry(country);
        return country;
    }

    public void removeCountry(Country country) {
        countryMap.remove(country.getId());
        File file = getFile(country);
        if (file == null) return;
        file.delete();
    }

    public List<Country> getAllCountries() {
        return countryMap.values().stream().toList();
    }

    private List<Country> loadData() {
        File[] files = folder.listFiles();
        if (files == null) return new ArrayList<>();
        List<Country> countries = new ArrayList<>();
        for (File file : files) {
            JsonNode node = JsonUtil.readFile(file);
            String id = node.get("id").asText();
            String name = node.get("name").asText();
            boolean available = node.get("available").asBoolean();
            Location spawnLocation = JsonUtil.getLocation(node.get("spawn_location"));
            int[] flag = new int[512];
            if (node.has("flag")) {
                try {
                    new ObjectMapper().readValue(node.get("flag").asText(), int[].class);
                } catch (IOException e) {
                    System.err.println("Could not read country " + name + "'s flag");
                }
            }
            countries.add(new Country(this, id, name, spawnLocation, available, flag));
        }
        return countries;
    }

    public Country getCountryById(String id) {
        return countryMap.get(id);
    }

    public Country getCountry(String name) {
        return countryMap.values().stream()
                .filter(country -> country.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    public void addCountry(String id, String name) throws IllegalArgumentException {
        addCountry(id, name, null, true);
    }

    public void addCountry(String id, String name, Location spawn, boolean available) throws IllegalArgumentException {
        Country country = createCountry(id, name, spawn, available);
        countryMap.put(id, country);
    }

    public List<String> getAvailableCountryNames() {
        return countryMap.values().stream().filter(Country::isAvailable).map(Country::getName).toList();
    }

    public List<String> getCountryNames() {
        return countryMap.values().stream().map(Country::getName).toList();
    }

    @Deprecated
    public void setId(Country country, String id) throws IllegalArgumentException {
        removeCountry(country);
        addCountry(id, country.getName(), country.getSpawn(), country.isAvailable());
    }
}
