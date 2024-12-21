package fr.world.nations.milestone.commands.xp;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;

import java.util.List;

public class MilestoneAddXpCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneAddXpCommand(WonMilestone plugin) {
        this.plugin = plugin;
        this.setRequirements(new CommandRequirements.Builder(Permission.HELP).build());
        getAliases().addAll(List.of("addmxp", "addxp"));
        getRequiredArgs().add("amount");
        getOptionalArgs().put("faction", "you");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.milestone.op")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        Faction faction = commandContext.argAsFaction(1, commandContext.faction);
        if (faction == null) return;

        Double arg = commandContext.argAsDouble(0);
        if (arg == null) {
            commandContext.sender.sendMessage("S'il vous plait spécifier un nombre entier !");
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        plugin.addOpModif(faction, arg);
        commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f level " + faction.getTag());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
