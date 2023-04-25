package fr.world.nations.assault.explosion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.assault.WonAssault;
import fr.world.nations.milestone.MilestoneCalculator;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.util.JsonUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExplosionManager {

    private final WonAssault plugin;
    private final Map<String, Long> lastUpdated;
    private final Map<String, Integer> tokens;
    private final File enemyFile;
    private final File tokenFile;

    public ExplosionManager(WonAssault plugin) {
        this.plugin = plugin;
        this.lastUpdated = new HashMap<>();
        this.tokens = new HashMap<>();
        this.enemyFile = new File(plugin.getConfigFolder(), "enemies.yml");
        this.tokenFile = new File(plugin.getConfigFolder(), "tokens.json");
        if (!enemyFile.exists()) {
            try {
                enemyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!tokenFile.exists()) {
            try {
                tokenFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getMaxTokenAmount(Faction faction) {
        MilestoneCalculator data = WonMilestone.getInstance().getMilestoneData(faction);
        int milestone = data.getMilestone();
        return 5 + (milestone > 0 ? milestone - 1 : 0);
    }

    public int getTokenAmount(Faction faction) {
        if (this.shouldResetTokens(faction)) {
            int tokenAmount = getMaxTokenAmount(faction);
            tokens.put(faction.getId(), tokenAmount);
        }
        lastUpdated.put(faction.getId(), System.currentTimeMillis());
        return tokens.get(faction.getId());
    }

    /**
     * Withdraws a token from the faction's account
     *
     * @param faction faction
     * @return true if the token has been withdrawn successfully
     */
    public boolean withdrawToken(Faction faction) {
        int tokenAmount = getTokenAmount(faction);
        if (tokenAmount == 0) return false;
        tokens.put(faction.getId(), tokenAmount - 1);
        return true;
    }

    public boolean tokenAvailable(Faction faction) {
        return getTokenAmount(faction) == 0;
    }

    private boolean shouldResetTokens(Faction faction) {
        if (!tokens.containsKey(faction.getId())) return true;
        if (!lastUpdated.containsKey(faction.getId())) return true;
        return differentWeek(System.currentTimeMillis(), lastUpdated.get(faction.getId()));
    }

    private boolean differentWeek(long t1, long t2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(t1);
        int year1 = c1.get(Calendar.YEAR);
        int week1 = c1.get(Calendar.WEEK_OF_YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(t2);
        int year2 = c2.get(Calendar.YEAR);
        int week2 = c2.get(Calendar.WEEK_OF_YEAR);

        return year1 != year2 || week1 != week2;
    }

    public boolean enemiesSinceSufficientTime(Faction f1, Faction f2) {
        long timeRequiredMillis = (long) (plugin.getConfig().getDouble("explosions.enemy-required-time-days") * 24 * 60 * 60 * 1000);
        return enemySinceMillis(f1, f2) > timeRequiredMillis;
    }

    public long enemySinceMillis(Faction f1, Faction f2) {
        FileConfiguration enemyConfig = YamlConfiguration.loadConfiguration(enemyFile);
        long enemyStartedMillis = enemyConfig.getLong(f1.getId() + "-" + f2.getId(), -1);
        if (enemyStartedMillis < 0) {
            enemyStartedMillis = enemyConfig.getLong(f2.getId() + "-" + f1.getId(), -1);
            if (enemyStartedMillis < 0) return -1;
        }
        return System.currentTimeMillis() - enemyStartedMillis;
    }

    public void setEnemies(Faction f1, Faction f2) {
        FileConfiguration enemyConfig = YamlConfiguration.loadConfiguration(enemyFile);
        long enemyStartedMillis = enemyConfig.getLong(f1.getId() + "-" + f2.getId(), -1);
        if (enemyStartedMillis < 0) {
            enemyConfig.set(f2.getId() + "-" + f1.getId(), System.currentTimeMillis());
        } else {
            enemyConfig.set(f1.getId() + "-" + f2.getId(), System.currentTimeMillis());
        }
    }

    public boolean areEnemies(Faction f1, Faction f2) {
        return enemySinceMillis(f1, f2) != -1;
    }

    public void save() {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        for (String factionId : tokens.keySet()) {
            Faction faction = Factions.getInstance().getFactionById(factionId);
            long lastUpdatedMillis = lastUpdated.get(factionId);
            ObjectNode data = JsonNodeFactory.instance.objectNode();
            data.put("name", faction.getTag());
            data.put("lastUpdatedMillis", lastUpdatedMillis);
            data.put("tokenAmount", getTokenAmount(faction));
            objectNode.put(factionId, data);
        }
        try {
            new ObjectMapper().writeValue(tokenFile, objectNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        JsonNode objectNode = JsonUtil.readFile(tokenFile, true);
        if (objectNode == null || objectNode instanceof MissingNode) return;
        Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> field = it.next();
            JsonNode data = field.getValue();
            int tokenAmount = data.get("tokenAmount").asInt();
            long lastUpdatedMillis = data.get("lastUpdatedMillis").asLong();
            tokens.put(field.getKey(), tokenAmount);
            lastUpdated.put(field.getKey(), lastUpdatedMillis);
        }

    }
}
