package fr.world.nations.milestone.commands.xp;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;

import java.util.List;

public class MilestoneAddXpCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneAddXpCommand(WonMilestone plugin) {
        this.plugin = plugin;
        aliases.addAll(List.of("addmxp", "addxp"));
        requiredArgs.add("amount");
        optionalArgs.put("faction", "you");
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
        commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f minfo " + faction.getTag());
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
