package fr.world.nations.koth.json.adapters.serializer;

/*
 *  * @Created on 2021 - 22:06
 *  * @Project UtilsAPI
 *  * @Author Jimmy  / vSKAH#0075
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.Location;

import java.io.IOException;

public class LocationSerializer extends JsonSerializer<Location> {

    @Override
    public void serialize(Location location, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("world", location.getWorld().getName());
        jsonGenerator.writeNumberField("x", location.getX());
        jsonGenerator.writeNumberField("y", location.getY());
        jsonGenerator.writeNumberField("z", location.getZ());
        jsonGenerator.writeNumberField("yaw", location.getYaw());
        jsonGenerator.writeNumberField("pitch", location.getPitch());
        jsonGenerator.writeEndObject();
    }

}
