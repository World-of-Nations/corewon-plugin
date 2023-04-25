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

        String msg = "\nDiagnostique du palier de la faction " + faction.getTag() + "\n\n";
        msg += "Joueurs : " + data.getPlayersXp() + " xp\n";
        msg += "Claims : " + data.getLandXp() + " xp\n";
        msg += "KDR : " + data.getKdrXp() + " xp\n";
        msg += "Ratio : " + data.getRatioXp() + " xp\n";
        msg += "Scorezone : " + data.getScorezoneXp() + " xp\n";
        msg += "Bank : " + data.getBankXp() + " xp\n";
        Map<String, Double> bonuses = plugin.getBonuses(faction);
        if (bonuses.isEmpty()) msg += "Bonus : aucun\n";
        else {
            StringBuilder toAdd = new StringBuilder("Bonus : \n");
            for (String key : bonuses.keySet()) {
                toAdd.append(" - ").append(key).append(" : ").append(bonuses.get(key)).append(" xp\n");
            }
            msg += toAdd.toString();
        }
        msg += "Total : " + data.getTotalXp() + " xp\n";
        msg += "Palier : " + data.getMilestone() + "\n";
        msg += "Avancement : " + data.getProgressXp() + "/" + data.getNextMilestoneXp() + "\n";
        commandContext.sender.sendMessage(msg);
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
