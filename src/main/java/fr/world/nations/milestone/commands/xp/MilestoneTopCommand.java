package fr.world.nations.milestone.commands.xp;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.MilestoneCalculator;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.util.FactionUtil;
import fr.world.nations.util.StringUtil;

import java.util.List;

public class MilestoneTopCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneTopCommand(WonMilestone plugin) {
        this.plugin = plugin;
        this.requirements.permission = Permission.HELP;
        aliases.addAll(List.of("mtop", "milestonetop", "top"));
        optionalArgs.put("page", "1");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.milestone")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }

        Factions factions = Factions.getInstance();

        List<Faction> factionList = FactionUtil.getAllPlayerFactions().stream()
                .filter(faction -> faction != factions.getSafeZone() && faction != factions.getWarZone())
                .sorted((f1, f2) -> (int) (plugin.getMilestoneData(f2).getTotalXp() - plugin.getMilestoneData(f1).getTotalXp()))
                .toList();

        int page = commandContext.argAsInt(0, 1);

        if ((page - 1) * 10 > factionList.size()) {
            page = (int) Math.floor(factionList.size() / 10.0) + 1;
        }
        if (page <= 0) page = 1;

        int max = Math.min(page * 10, factionList.size());

        StringBuilder builder = new StringBuilder("§cMeilleures factions §e(Palier)§4 - §cPage " + page + " : \n");
        for (int i = (page - 1) * 10; i < max; i++) {
            Faction faction = factionList.get(i);
            MilestoneCalculator data = plugin.getMilestoneData(faction);
            builder.append("§e").append(i + 1).append(". §6")
                    .append(faction.getTag()).append("§c")
                    .append(" - Palier ").append(data.getMilestone())
                    .append(" §4(").append(StringUtil.round(data.getTotalXp(), 2)).append("xp)§r\n");
        }
        commandContext.sender.sendMessage(builder.toString());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
