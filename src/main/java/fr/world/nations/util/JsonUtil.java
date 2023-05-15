package fr.world.nations.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
}
