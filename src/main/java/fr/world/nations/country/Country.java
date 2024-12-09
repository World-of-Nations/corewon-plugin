package fr.world.nations.country;

import org.apache.commons.lang3.Validate;
import org.bukkit.Location;

public class Country {

    private final CountryManager countryManager;
    private final String id;
    private String name;
    private Location spawn;
    private boolean available;
    private int[] flag;

    protected Country(CountryManager countryManager, String id, String name) {
        this(countryManager, id, name, null, true, new int[512]);
    }

    protected Country(CountryManager countryManager, String id, String name, Location spawn, boolean available, int[] flag) {
        Validate.notNull(id);
        Validate.notNull(name);
        this.countryManager = countryManager;
        this.id = id;
        this.name = name;
        this.spawn = spawn;
        this.available = available;
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        countryManager.saveCountry(this);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
        countryManager.saveCountry(this);
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
        countryManager.saveCountry(this);
    }

    public int[] getFlag() {
        return flag;
    }

    public void setFlag(int[] flag) {
        if (flag.length != 512) {
            throw new IllegalArgumentException("Flag should be a 512 length array");
        }
        this.flag = flag;
        countryManager.saveCountry(this);
    }
}
