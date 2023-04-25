package fr.world.nations.milestone.commands.xp;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;

import java.util.List;

public class MilestoneResetOMCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneResetOMCommand(WonMilestone plugin) {
        this.plugin = plugin;
        aliases.addAll(List.of("resetom", "resetopmodifier"));
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
        plugin.setOpModif(faction, 0);
        commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f minfo " + faction.getTag());
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
