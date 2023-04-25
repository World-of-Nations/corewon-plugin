package fr.world.nations.milestone.commands.warps;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.milestone.warps.WarpManager;
import fr.world.nations.util.FactionUtil;

import java.util.List;
import java.util.Set;

public class MilestoneWarpsCommand extends FCommand {

    private final WarpManager warpManager;

    public MilestoneWarpsCommand(WonMilestone plugin) {
        this.warpManager = plugin.getWarpManager();
        aliases.addAll(List.of("warps", "warplist"));
        optionalArgs.put("faction", "you");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.warps")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        Faction faction = commandContext.argAsFaction(0, commandContext.faction);
        if (!commandContext.sender.hasPermission("fac-milestone.warps.op")) {
            faction = commandContext.faction;
        }

        if (FactionUtil.isNone(faction)) {
            if (faction == commandContext.faction) {
                commandContext.sender.sendMessage("Vous n'avez pas de faction !");
            } else {
                commandContext.sender.sendMessage("La faction " + commandContext.argAsString(0) + " n'existe pas !");
            }
            return;
        }
        Set<String> warps = warpManager.getWarps(faction).keySet();
        String list = String.join(", ", warps);
        if (warps.isEmpty()) {
            String facName = faction == FactionUtil.getFaction(commandContext.sender) ? "Votre faction" : "La faction " + faction.getTag();
            commandContext.sender.sendMessage(facName + " n'a aucun warp !");
        } else {
            String facName = faction == FactionUtil.getFaction(commandContext.sender) ? "votre faction" : "la faction " + faction.getTag();
            commandContext.sender.sendMessage("Liste des warps de " + facName + " : " + list);
        }
        int delta = warpManager.availableWarpsRemaining(faction);
        if (delta < 0) {
            if (faction == FactionUtil.getFaction(commandContext.sender)) {
                commandContext.sender.sendMessage("§cVous n'avez plus de warp disponible ! Supprimez-en " + delta + " " +
                        "pour pouvoir vous y téléporter !");
            } else {
                commandContext.sender.sendMessage("§cLa faction " + faction.getTag() + " n'a plus de warp disponible ! Elle doit en supprimer " + delta + " " +
                        "pour pouvoir s'y téléporter !");
            }
        }
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
