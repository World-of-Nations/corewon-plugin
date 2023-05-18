package fr.world.nations.koth.managers;

/*
 *  * @Created on 18/08/2022
 *  * @Project KOTH-WON
 *  * @Author Jimmy  / SKAH#7513
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.models.PowerAddedModel;
import fr.world.nations.util.StringUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class PowerManager {

    private static PowerManager instance;
    private final List<PowerAddedModel> kothPowerLogger = Lists.newArrayList();
    private final File file = new File(WonKoth.getInstance().getConfigFolder(), "powerlogs.json");
    private final File factorsFile = new File(WonKoth.getInstance().getConfigFolder(), "factionPowerFactors.json");

    public PowerManager() {
        instance = this;
    }

    public static PowerManager getInstance() {
        return instance == null ? new PowerManager() : instance;
    }

    public void loadPower() {
        if (file.exists()) {
            final ObjectMapper mapper = new ObjectMapper();
            try {
                kothPowerLogger.addAll(mapper.readValue(file, new TypeReference<List<PowerAddedModel>>() {
                }));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!factorsFile.exists()) {
            try {
                factorsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPower(PowerAddedModel power) {
        kothPowerLogger.add(power);
    }

    public void updatePower() {
        final List<PowerAddedModel> powers = new ArrayList<>();

        for (PowerAddedModel power : kothPowerLogger.toArray(new PowerAddedModel[0])) {
            if (power.getMustRemoveAt() == 0) {
                continue;
            }

            if (System.currentTimeMillis() >= power.getMustRemoveAt()) {
                if (power.getPower() >= 0) {
                    continue;
                }
                Faction faction = Factions.getInstance().getFactionById(power.getFactionTagId());
                faction.setPowerBoost(faction.getPowerBoost() - power.getPower());

                power.setPower(power.getPower() - (power.getPower() * 25 / 100));
                power.setMustRemoveAt(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
                faction.setPowerBoost(faction.getPowerBoost() + power.getPower());
                kothPowerLogger.add(power);
            }
        }
        kothPowerLogger.clear();
        kothPowerLogger.addAll(powers);
    }

    @SneakyThrows
    public void savePower() {
        final ObjectMapper mapper = new ObjectMapper();
        final List<PowerAddedModel> powers = new ArrayList<>();

        for (PowerAddedModel power : kothPowerLogger.toArray(new PowerAddedModel[0])) {
            if (power.getMustRemoveAt() == 0) {
                continue;
            }
            kothPowerLogger.add(power);
        }

        mapper.writeValue(file, powers);
        powers.clear();
    }

    public double getFactionFactor(Faction faction, boolean putIfAbsent) {
        String factionName = faction.getTag();
        try {
            JsonNode node = new ObjectMapper().readTree(factorsFile);
            if (!node.has(factionName) && putIfAbsent) {
                ObjectNode objectNode = getObjectNode(node);
                objectNode.put(factionName, System.currentTimeMillis());
                new ObjectMapper().writeValue(factorsFile, objectNode);
                return 1;
            }
            JsonNode factionNode = node.get(factionName);
            long coolDownStart = factionNode.asLong();
            String updateCoolDown = WonKoth.getInstance().getDefaultConfig().getString("config.power-update-cooldown", "1we");
            long updateCoolDownLong = StringUtil.toMillis(updateCoolDown);
            long timeDelta = System.currentTimeMillis() - coolDownStart;
            int steps = (int) Math.floor(timeDelta / updateCoolDownLong);
            double percentage = 1;
            double stepFactor = WonKoth.getInstance().getDefaultConfig().getDouble("config.power-update-factor", 0.75);
            for (int i = 0; i < steps; i++) {
                percentage *= stepFactor;
            }
            return percentage;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long resetTimer(Faction faction) {
        return setTimer(faction, System.currentTimeMillis(), false);
    }

    public long setTimer(Faction faction, long dateMillis) {
        return setTimer(faction, dateMillis, true);
    }

    public long setTimer(Faction faction, long dateMillis, boolean putIfAbsent) {
        if (faction == null) {
            throw new IllegalStateException("faction cannot be null !");
        }
        String factionName = faction.getTag();
        try {
            JsonNode node = new ObjectMapper().readTree(factorsFile);
            if (!node.has(factionName)) {
                if (!putIfAbsent) return -1;
                ObjectNode objectNode = getObjectNode(node);
                objectNode.put(factionName, System.currentTimeMillis());
                new ObjectMapper().writeValue(factorsFile, objectNode);
                return dateMillis;
            }
            JsonNode valueNode = node.get(factionName);
            long prev = valueNode.asLong();
            ObjectNode replacement = getObjectNode(node);
            replacement.put(factionName, dateMillis);
            new ObjectMapper().writeValue(factorsFile, replacement);
            return prev;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public long deleteTimer(Faction faction) {
        if (faction == null) {
            throw new IllegalStateException("faction cannot be null !");
        }
        String factionName = faction.getTag();
        try {
            JsonNode node = new ObjectMapper().readTree(factorsFile);
            ObjectNode replacement = getObjectNode(node);
            JsonNode valueNode = replacement.remove(factionName);
            if (valueNode == null) return -1;
            new ObjectMapper().writeValue(factorsFile, replacement);
            return valueNode.asLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectNode getObjectNode(JsonNode node) {
        if (node instanceof MissingNode)
            return JsonNodeFactory.instance.objectNode();
        return (ObjectNode) node;
    }

}
