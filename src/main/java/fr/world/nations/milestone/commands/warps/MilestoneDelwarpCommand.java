package fr.world.nations.milestone.commands.warps;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.milestone.warps.WarpManager;
import org.bukkit.entity.Player;

import java.util.List;

public class MilestoneDelwarpCommand extends FCommand {

    private final WarpManager warpManager;

    public MilestoneDelwarpCommand(WonMilestone plugin) {
        this.warpManager = plugin.getWarpManager();
        aliases.addAll(List.of("delwarp", "deletewarp"));
        requiredArgs.add("name");
        optionalArgs.put("faction", "you");
//        visibility = CommandVisibility.VISIBLE;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.warps")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        Faction faction = commandContext.argAsFaction(1, commandContext.faction);
        if (faction == null) return;
        if (!commandContext.sender.hasPermission("fac-milestone.warps.op")) {
            faction = commandContext.faction;
        }
        if (commandContext.sender instanceof Player) {
            Role rel = FPlayers.getInstance().getByOfflinePlayer((Player) commandContext.sender).getRole();

            if (faction.getPermissions().get(rel).get(PermissableAction.SETWARP) != Access.ALLOW && !commandContext.sender.hasPermission("fac-milestone.warps.op")) {
                commandContext.sender.sendMessage("Vous n'avez pas les permissions !");
                return;
            }
        } else if (!commandContext.sender.hasPermission("fac-milestone.warps.op")) {
            commandContext.sender.sendMessage("Vous n'avez pas les permissions !");
            return;
        }
        String warpName = commandContext.argAsString(0).toLowerCase();
        warpManager.deleteWarp(faction, warpName);
        commandContext.sender.sendMessage("Warp " + warpName + " supprimé.");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
