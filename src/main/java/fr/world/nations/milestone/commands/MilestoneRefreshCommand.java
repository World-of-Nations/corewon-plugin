package fr.world.nations.milestone.commands;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;

public class MilestoneRefreshCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneRefreshCommand(WonMilestone plugin) {
        this.plugin = plugin;
        aliases.add("levelrefresh");
    }

    @Override
    public void perform(CommandContext context) {
        if (!context.sender.isOp())
            context.msg(TL.ACTIONS_NOPERMISSION);
        for (Faction faction : Factions.getInstance().getAllNormalFactions()) {
            int currentMilestone = plugin.getMilestoneData(faction).getMilestone();
            faction.setUpgrade("Chest", currentMilestone);
            plugin.updateChests(faction);
            faction.setUpgrade("Warps", currentMilestone);
            plugin.updateWarps(faction);
        }
        context.msg("Paliers rechargés.");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
