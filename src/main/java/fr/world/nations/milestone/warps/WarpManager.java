package fr.world.nations.milestone.warps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import fr.world.nations.milestone.MilestoneCalculator;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.util.JsonUtil;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WarpManager {

    private final WonMilestone plugin;
    private final File file;

    public WarpManager(WonMilestone plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getConfigFolder(), "warps");
        if (!this.file.exists()) {
            this.file.mkdir();
        }
    }


    public boolean setWarp(Faction faction, String name, Location location) {
        try {
            File file = getFile(faction);
            ObjectNode toWrite = JsonUtil.getObjectNode(file);
            toWrite.put(name, locationNode(location));
            new ObjectMapper().writeValue(file, toWrite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean deleteWarp(Faction faction, String name) {
        try {
            File file = getFile(faction);
            ObjectNode toWrite = JsonUtil.getObjectNode(file);
            boolean contains = toWrite.remove(name) != null;
            new ObjectMapper().writeValue(file, toWrite);
            return contains;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasWarp(Faction faction, String name) {
        return getWarps(faction).containsKey(name);
    }

    private JsonNode locationNode(Location location) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("worldName",
                location.getWorld() == null ? "world" : location.getWorld().getName());
        objectNode.put("x", location.getX());
        objectNode.put("y", location.getY());
        objectNode.put("z", location.getZ());
        objectNode.put("yaw", location.getYaw());
        objectNode.put("pitch", location.getPitch());
        return objectNode;
    }

    private Location fromLocationNode(JsonNode node) {
        String worldName = node.get("worldName").textValue();
        double x = node.get("x").doubleValue();
        double y = node.get("y").doubleValue();
        double z = node.get("z").doubleValue();
        float yaw = node.get("yaw").numberValue().floatValue();
        float pitch = node.get("pitch").numberValue().floatValue();
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public Location getWarp(Faction faction, String name) {
        Location warp = getWarps(faction).get(name);
        return this.verifyWarpIsValid(faction, name, warp);
    }

    public Location verifyWarpIsValid(Faction faction, String warpName, Location warp) {
        if (Conf.homesMustBeInClaimedTerritory && Board.getInstance().getFactionAt(FLocation.wrap(warp)) != faction) {
            if (this.deleteWarp(faction, warpName))
                faction.msg("§cVotre warp " + warpName + " a été supprimé car il ne se trouve plus dans votre territoire.");
            return null;
        }
        return warp;
    }

    @NonNull
    public Map<String, Location> getWarps(Faction faction) {
        Map<String, Location> warps = Maps.newHashMap();

        File file = getFile(faction);
        try {
            JsonNode node = new ObjectMapper().readTree(file);
            if (node instanceof MissingNode) {
                return Maps.newHashMap();
            }
            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                warps.put(entry.getKey(), fromLocationNode(entry.getValue()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return warps;
    }

    private File getFile(Faction faction) {
        File file = new File(this.file, faction.getTag() + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    public boolean isNoWarpAvailable(Faction faction) {
        return availableWarpsRemaining(faction) <= 0;
    }

    public int availableWarpsRemaining(Faction faction) {
        Map<Integer, Integer> warpTable = new HashMap<>();
        warpTable.put(0, 0);
        warpTable.put(1, 0);
        warpTable.put(2, 1);
        warpTable.put(3, 2);
        warpTable.put(4, 3);
        warpTable.put(5, 4);
        MilestoneCalculator data = plugin.getMilestoneData(faction);
        int maxWarps = warpTable.get(data.getMilestone());
        return maxWarps - getWarps(faction).size();
    }
}
