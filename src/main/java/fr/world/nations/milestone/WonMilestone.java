package fr.world.nations.milestone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.FCommand;
import fr.world.nations.Core;
import fr.world.nations.milestone.commands.MilestoneExpandCommand;
import fr.world.nations.milestone.commands.MilestoneHelpCommand;
import fr.world.nations.milestone.commands.xp.*;
import fr.world.nations.modules.WonModule;
import fr.world.nations.util.FactionUtil;
import fr.world.nations.util.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class WonMilestone extends WonModule {

    private static WonMilestone instance;
    private File file;
    private int regularCheckTaskId;

    public WonMilestone(Plugin loader, String name) {
        super(loader, name);
    }

    public static WonMilestone getInstance() {
        return instance;
    }


    public double getOpModif(Faction faction) {
        return getXpBonus(faction, "op");
    }

    public void setOpModif(Faction faction, double modif) {
        setXpBonus(faction, "op", modif);
    }

    public void addOpModif(Faction faction, double modif) {
        addXpBonus(faction, "op", modif);
    }

    public double getXpBonus(Faction faction, String fieldName) {
        JsonNode node = JsonUtil.readFile(file);
        if (!node.has(faction.getTag())) return 0;
        JsonNode factionNode = node.get(faction.getTag());
        if (!factionNode.has(fieldName)) return 0;
        return factionNode.get(fieldName).asDouble();
    }

    public void setXpBonus(Faction faction, String fieldName, double modif) {
        ObjectNode node = JsonUtil.getObjectNode(file);
        ObjectNode factionNode;
        if (node.has(faction.getTag())) {
            factionNode = (ObjectNode) node.get(faction.getTag());
        } else {
            factionNode = JsonNodeFactory.instance.objectNode();
        }
        factionNode.put(fieldName, modif);
        node.put(faction.getTag(), factionNode);
        try {
            new ObjectMapper().writeValue(file, node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addXpBonus(Faction faction, String fieldName, double modif) {
        setXpBonus(faction, fieldName, getOpModif(faction) + modif);
    }

    public Map<String, Double> getBonuses(Faction faction) {
        ObjectNode node = JsonUtil.getObjectNode(file);
        JsonNode factionNode = node.get(faction.getTag());
        if (factionNode == null) return Maps.newHashMap();
        Map<String, Double> map = Maps.newHashMap();
        Iterator<Map.Entry<String, JsonNode>> it = factionNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> nodeEntry = it.next();
            map.put(nodeEntry.getKey(), nodeEntry.getValue().doubleValue());
        }
        return map;
    }

    public MilestoneCalculator getMilestoneData(Faction faction) {
        return new MilestoneCalculator(this, faction);
    }

    @Override
    public void load() {
        instance = this;
        // Plugin startup logic

        try {
            File dataFolder = this.getConfigFolder();
            if (!dataFolder.exists()) dataFolder.mkdir();

            File config = new File(this.getConfigFolder(), "config.yml");
            if (!config.exists()) {
                config.createNewFile();
            }


            file = new File(dataFolder, "xp_bonus.json");
            if (!file.exists()) {
                file.createNewFile();
                new ObjectMapper().writeValue(file, JsonNodeFactory.instance.objectNode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int delay = this.getDefaultConfig().getInt("milestone.check_delay_seconds", 5) * 20;
        if (delay < 1) delay = 1;

        BukkitTask task = new BukkitRunnable() {
            final HashMap<String, Integer> map = new HashMap<>();

            @Override
            public void run() {
                for (Faction faction : FactionUtil.getAllPlayerFactions()) {
                    String factionName = faction.getTag();
                    int currentMilestone = getMilestoneData(faction).getMilestone();
                    faction.setWarpsLimit(getDefaultConfig().getInt("warps.limit." + currentMilestone));
                    if (!map.containsKey(factionName)) {
                        map.put(factionName, currentMilestone);
                        continue;
                    }
                    if (currentMilestone > map.get(factionName)) {
                        if (currentMilestone == 5) {
                            Bukkit.broadcastMessage("§cLe pays §e" + factionName + " §cpasse au §ePalier V §c! C'est désormais un §4§oEmpire §6!");
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                            }
                        }
                        faction.sendMessage("§aVotre pays est passé au palier supérieur !");
                        for (Player player : faction.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        }
                        map.put(factionName, currentMilestone);
                    }
                }
            }
        }.runTaskTimerAsynchronously(Core.getInstance(), delay, delay);
        regularCheckTaskId = task.getTaskId();
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(regularCheckTaskId);
    }

    @Override
    public List<FCommand> registerFCommands() {
        return Lists.newArrayList(
                new MilestoneAddXpCommand(this),
                new MilestoneInfoCommand(),
                new MilestoneRemoveXpCommand(this),
                new MilestoneResetOMCommand(this),
                new MilestoneTopCommand(this),
                new MilestoneHelpCommand(),
                new MilestoneDiagnosisCommand(this),
                new MilestoneExpandCommand(this));
    }

    @Override
    protected Map<String, Object> getDefaultConfigValues() {
        Map<String, Object> values = new HashMap<>(Map.of(
                "warps.limit.0", 0,
                "warps.limit.1", 0,
                "warps.limit.2", 1,
                "warps.limit.3", 2,
                "warps.limit.4", 3,
                "warps.limit.5", 4,
                "milestone.experience.per_player", 50,
                "milestone.experience.per_land", 25,
                "milestone.experience.kdr_factor", 250,
                "milestone.experience.assault_score_factor", 25
        ));
        values.put("milestone.experience.scorezone_factor", 1.5);
        values.put("milestone.experience.bank_factor", 0.01);
        values.put("milestone.check_delay_seconds", 5);
        return values;
    }
}
