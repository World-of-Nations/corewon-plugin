package fr.world.nations.koth.json.adapters.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.io.IOException;

public class CuboidSerializer extends JsonSerializer<CuboidRegion> {

    public void serialize(final CuboidRegion cuboidRegion, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("world", cuboidRegion.getWorld().getName());
        jsonGenerator.writeArrayFieldStart("pos1");
        this.writePos(cuboidRegion.getPos1(), jsonGenerator);
        jsonGenerator.writeEndArray();
        jsonGenerator.writeArrayFieldStart("pos2");
        this.writePos(cuboidRegion.getPos2(), jsonGenerator);
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    private void writePos(final BlockVector3 pos, final JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("x", pos.getBlockX());
        jsonGenerator.writeNumberField("y", pos.getBlockY());
        jsonGenerator.writeNumberField("z", pos.getBlockZ());
        jsonGenerator.writeEndObject();
    }
}
