package fr.world.nations.milestone.commands.xp;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.MilestoneCalculator;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.util.FactionUtil;

import java.util.Map;

public class MilestoneDiagnosisCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneDiagnosisCommand(WonMilestone plugin) {
        this.plugin = plugin;
        aliases.add("diagnosis");
        optionalArgs.put("faction", "you");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.milestone.op")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }

        Faction faction = commandContext.argAsFaction(0, commandContext.faction);

        if (!FactionUtil.isPlayerFaction(faction)) {
            commandContext.sender.sendMessage("Faction invalide !");
            return;
        }

        MilestoneCalculator data = plugin.getMilestoneData(faction);

        String msg = "\n§cDiagnostique du palier de la faction §6" + faction.getTag() + "\n\n";
        msg += "§4Joueurs : §6" + data.getPlayersXp() + " xp\n";
        msg += "§4Claims : §6" + data.getLandXp() + " xp\n";
        msg += "§4KDR : §6" + data.getKdrXp() + " xp\n";
        msg += "§4Ratio : §6" + data.getRatioXp() + " xp\n";
        msg += "§4Scorezone : §6" + data.getScorezoneXp() + " xp\n";
        msg += "§4Bank : §6" + data.getBankXp() + " xp\n";
        Map<String, Double> bonuses = plugin.getBonuses(faction);
        if (bonuses.isEmpty()) msg += "§4Bonus : §6aucun\n";
        else {
            StringBuilder toAdd = new StringBuilder("§4Bonus : \n");
            for (String key : bonuses.keySet()) {
                toAdd.append("§c - ").append(key).append(" : §6").append(bonuses.get(key)).append(" xp\n");
            }
            msg += toAdd.toString();
        }
        msg += "§4Total : §6" + data.getTotalXp() + " xp\n";
        msg += "§4Palier : §6" + data.getMilestone() + "\n";
        msg += "Avancement : §6" + data.getProgressXp() + "§c/§6" + data.getNextMilestoneXp() + "\n";
        commandContext.sender.sendMessage(msg);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
