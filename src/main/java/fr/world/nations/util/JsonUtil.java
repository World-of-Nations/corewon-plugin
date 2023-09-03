package fr.world.nations.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

public class JsonUtil {
    public static JsonNode readFile(File file) {
        return readFile(file, false);
    }

    public static JsonNode readFile(File file, boolean createNodeIfEmpty) {
        try {
            return new ObjectMapper().readTree(file);
        } catch (JsonMappingException e) {
            if (createNodeIfEmpty) {
                try {
                    new ObjectMapper().writeValue(file, JsonNodeFactory.instance.objectNode());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectNode getObjectNode(File file) {
        JsonNode node = readFile(file);
        if (node == null || node instanceof MissingNode) {
            return JsonNodeFactory.instance.objectNode();
        } else if (node instanceof ObjectNode) {
            return (ObjectNode) node;
        } else {
            throw new RuntimeException("Could not read node properly");
        }
    }

    public static void write(File file, JsonNode node) {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Location getLocation(JsonNode node) {
        try {
            double x = node.get("x").asDouble();
            double y = node.get("y").asDouble();
            double z = node.get("z").asDouble();
            float pitch = 0;
            if (node.has("pitch")) {
                pitch = node.get("pitch").numberValue().floatValue();
            }
            float yaw = 0;
            if (node.has("yaw")) {
                yaw = node.get("yaw").numberValue().floatValue();
            }
            String worldName = "world";
            if (node.has("world_name")) {
                worldName = node.get("world_name").asText();
            }
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    public static ObjectNode wrapLocation(Location location, boolean includeYawPitch) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        if (location == null) return objectNode;
        objectNode.put("x", location.getX());
        objectNode.put("y", location.getY());
        objectNode.put("z", location.getZ());
        if (includeYawPitch) {
            objectNode.put("yaw", location.getYaw());
            objectNode.put("pitch", location.getPitch());
        }
        World world = location.getWorld();
        if (world != null) {
            objectNode.put("world_name", location.getWorld().getName());
        }
        return objectNode;
    }
}
