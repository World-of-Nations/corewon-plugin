package fr.world.nations.country;

import org.bukkit.Location;

public class Country {
    private final String name;
    private Location spawn;
    private boolean available = true;

    public Country(String name) {
        this.name = name;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public Location getSpawn() {
        return spawn;
    }
}