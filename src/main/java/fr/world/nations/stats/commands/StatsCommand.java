package fr.world.nations.stats.commands;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import fr.world.nations.stats.data.StatsManager;

import java.util.Arrays;

public class StatsCommand extends FCommand {


    public StatsCommand() {
        super();
        this.aliases.addAll(Arrays.asList("stats", "s"));
        this.optionalArgs.put("faction", "yours");
        this.requirements = new CommandRequirements.Builder(null)
                .memberOnly()
                .build();
    }


    @Override
    public void perform(CommandContext context) {
        Faction faction = context.faction;
        if (context.argIsSet(0)) {
            faction = context.argAsFaction(0);
        }

        if (faction == null || faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) {
            context.sendMessage("§cVous devez être dans une faction pour utiliser cette commande !");
            return;
        }

        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();
        FactionData factionData = statsManager.getFactionData(faction);
        context.sendMessage("§6§l" + faction.getTag() + "§r§6 stats:");
        context.sendMessage("§6Kills: §r" + factionData.getKills());
        context.sendMessage("§6Deaths: §r" + factionData.getDeaths());
        context.sendMessage("§6KDR: §r" + factionData.getKdr());


    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }


}
