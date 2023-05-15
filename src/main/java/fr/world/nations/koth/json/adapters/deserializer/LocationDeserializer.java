package fr.world.nations.koth.json.adapters.deserializer;

/*
 *  * @Created on 2021 - 22:16
 *  * @Project UtilsAPI
 *  * @Author Jimmy  / vSKAH#0075
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationDeserializer extends JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final ObjectCodec objectCodec = jsonParser.getCodec();
        final JsonNode jsonNode = objectCodec.readTree(jsonParser);
        return new Location(Bukkit.getWorld(jsonNode.get("world").textValue()),
                jsonNode.get("x").doubleValue(),
                jsonNode.get("y").doubleValue(),
                jsonNode.get("z").doubleValue(),
                jsonNode.get("yaw").numberValue().floatValue(),
                jsonNode.get("pitch").numberValue().floatValue());
    }
}
