package fr.world.nations.stats.commands;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import fr.world.nations.stats.data.StatsManager;
import fr.world.nations.util.StringUtil;

import java.util.Arrays;

public class StatsCommand extends FCommand {


    public StatsCommand() {
        super();
        this.getAliases().addAll(Arrays.asList("stats", "s"));
        this.setRequirements(new CommandRequirements.Builder(Permission.HELP).memberOnly().build());
        this.getOptionalArgs().put("faction", "yours");
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
        context.sendMessage("§7======= §6§l" + faction.getTag() + "§r§6 stats §7=======");
        context.sendMessage("§eKills§7: §a" + factionData.getKills());
        context.sendMessage("§eMorts§7: §c" + factionData.getDeaths());
        context.sendMessage("§eRatio K/M§7: §r" + factionData.getKdr());
        context.sendMessage("§eBanque§7: §r" + faction.getFactionBalance());
        context.sendMessage("§eScore zone§7: §r" + StringUtil.round(factionData.getScoreZone(), 2));
        context.sendMessage("§eAssauts gagnés§7: §r" + factionData.getAssaultWin());
        context.sendMessage("§eAssauts perdus§7: §r" + factionData.getAssaultLose());
        context.sendMessage("§eScore assaut§7: §r" + factionData.getAssaultScore());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }


}
