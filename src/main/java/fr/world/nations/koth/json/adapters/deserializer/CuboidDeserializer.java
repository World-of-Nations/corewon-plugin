package fr.world.nations.koth.json.adapters.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Iterator;

public class CuboidDeserializer extends JsonDeserializer<CuboidRegion> {

    public CuboidRegion deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        final ObjectCodec objectCodec = jsonParser.getCodec();
        final JsonNode jsonNode = objectCodec.readTree(jsonParser);
        final String worldName = jsonNode.get("world").textValue();
        final Location loc1 = this.buildLocation(worldName, jsonNode.get("pos1"));
        final Location loc2 = this.buildLocation(worldName, jsonNode.get("pos2"));
        CuboidRegion region = new CuboidRegion(Vector3.toBlockPoint(loc1.getX(), loc1.getY(), loc1.getZ()), Vector3.toBlockPoint(loc2.getX(), loc2.getY(), loc2.getZ()));
        region.setWorld(new BukkitWorld(Bukkit.getWorld(worldName)));
        return region;
    }

    private Location buildLocation(final String worldName, final JsonNode posNode) {
        int x = 0, y = 0, z = 0;
        Iterator<JsonNode> node = posNode.elements();
        while (node.hasNext()) {
            JsonNode nextNode = node.next();
            x = nextNode.get("x").intValue();
            y = nextNode.get("y").intValue();
            z = nextNode.get("z").intValue();
        }
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}
