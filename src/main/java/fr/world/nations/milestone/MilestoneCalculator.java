package fr.world.nations.milestone;

import com.massivecraft.factions.Faction;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class MilestoneCalculator {
    @Getter
    private final Faction faction;
    @Getter
    private final int milestone;
    @Getter
    private final double totalXp;
    @Getter
    private final double progressXp;
    @Getter
    private final int nextMilestoneXp;
    @Getter
    private final double playersXp;
    @Getter
    private final double landXp;
    @Getter
    private final Map<String, Double> bonuses;
    @Getter
    private final double ratioXp;
    @Getter
    private final double scorezoneXp;
    @Getter
    private final double bankXp;
    @Getter
    private final double kdrXp;

    public MilestoneCalculator(WonMilestone plugin, Faction faction) {
        this.faction = faction;

        FileConfiguration config = WonMilestone.getInstance().getConfig();
        playersXp = faction.getFPlayers().size() * config.getDouble("milestone.experience.per_player", 50);
        landXp = faction.getLandRounded() * config.getDouble("milestone.experience.per_land", 25);
        FactionData factionData = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager().getFactionData(faction);
        kdrXp = factionData.getKdr() * config.getDouble("milestone.experience.kdr_factor", 250);
        ratioXp = factionData.getAssaultScore() * config.getDouble("milestone.experience.assault_score_factor", 25);
        scorezoneXp = factionData.getScoreZone() * config.getDouble("milestone.experience.scorezone_factor", 1.5);
        bankXp = faction.getFactionBalance() * config.getDouble("milestone.experience.bank_factor", 0.01);

        this.bonuses = plugin.getBonuses(faction);

        double xp = playersXp + landXp + kdrXp + ratioXp + scorezoneXp + bankXp;
        for (Double toAdd : this.bonuses.values()) {
            if (toAdd.isNaN()) continue;
            xp += toAdd;
        }

        this.totalXp = xp;
        int[] xpTable = {
                5000,
                10000,
                20000,
                40000,
                60000
        };
        int milestone = 0;
        while (milestone < xpTable.length && xpTable[milestone] <= xp) {
            //xp -= xpTable[milestone];
            milestone += 1;
        }
        this.milestone = milestone;
        this.progressXp = xp;
        this.nextMilestoneXp = milestone >= xpTable.length ? xpTable[xpTable.length - 1] : xpTable[milestone];
    }
}
