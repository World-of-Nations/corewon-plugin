package fr.world.nations.milestone.commands.xp;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.util.FactionUtil;

public class MilestoneInfoCommand extends FCommand {

    public MilestoneInfoCommand() {
        getAliases().add("palier");
        this.setRequirements(new CommandRequirements.Builder(Permission.HELP).build());
        getOptionalArgs().put("faction", "you");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.milestone")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }

        Faction faction = commandContext.argAsFaction(0, commandContext.faction);
        if (!commandContext.sender.hasPermission("fac-milestone.warps.op")) {
            faction = commandContext.faction;
        }

        if (!FactionUtil.isPlayerFaction(faction)) {
            if (faction == commandContext.faction) {
                commandContext.sender.sendMessage("§cVous n'avez pas de pays !");
            } else {
                commandContext.sender.sendMessage("§cLe pays " + commandContext.argAsString(0) + " n'existe pas !");
            }
            return;
        }

        FactionUtil.sendFactionInfo(commandContext.sender, faction);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
