package fr.world.nations.koth.json;

/*
 *  * @Created on 2021 - 23:06
 *  * @Project UtilsAPI
 *  * @Author Jimmy  / vSKAH#0075
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sk89q.worldedit.regions.CuboidRegion;
import fr.world.nations.koth.json.adapters.deserializer.CuboidDeserializer;
import fr.world.nations.koth.json.adapters.deserializer.LocationDeserializer;
import fr.world.nations.koth.json.adapters.serializer.CuboidSerializer;
import fr.world.nations.koth.json.adapters.serializer.LocationSerializer;
import org.bukkit.Location;

public class MinecraftObjectMapper {

    private final SimpleModule simpleModule;
    private final ObjectMapper objectMapper;

    public MinecraftObjectMapper() {
        simpleModule = new SimpleModule();
        objectMapper = new ObjectMapper();

        simpleModule.addSerializer(CuboidRegion.class, new CuboidSerializer());
        simpleModule.addSerializer(Location.class, new LocationSerializer());

        simpleModule.addDeserializer(CuboidRegion.class, new CuboidDeserializer());
        simpleModule.addDeserializer(Location.class, new LocationDeserializer());
    }

    public SimpleModule getSimpleModule() {
        return simpleModule;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper.registerModule(getSimpleModule());
    }
}
