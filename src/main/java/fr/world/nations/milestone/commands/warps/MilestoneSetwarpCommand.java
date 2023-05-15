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
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MilestoneSetwarpCommand extends FCommand {

    private final WarpManager warpManager;

    public MilestoneSetwarpCommand(WonMilestone plugin) {
        this.warpManager = plugin.getWarpManager();
        aliases.add("setwarp");
        requiredArgs.add("name");
        optionalArgs.put("faction", "you");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
        requirements.playerOnly = true;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.warps")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        Faction faction = commandContext.argAsFaction(1, commandContext.faction);
        if (faction == null) return;
        if (!(commandContext.sender instanceof Player player)) {
            commandContext.sender.sendMessage("Vous ne pouvez pas effectuer cette commande depuis la console !");
            return;
        }
        Role rel = FPlayers.getInstance().getByPlayer(player).getRole();
        if (faction.getPermissions().get(rel).get(PermissableAction.SETWARP) != Access.ALLOW && !commandContext.sender.hasPermission("fac-milestone.warps.op")) {
            commandContext.sender.sendMessage("Vous n'avez pas les permissions !");
            return;
        }
        String warpName = commandContext.argAsString(0).toLowerCase();

        if (warpManager.hasWarp(faction, warpName)) {
            warpManager.setWarp(faction, warpName, player.getLocation());
            commandContext.sender.sendMessage("Warp " + warpName + " actualisé à votre position.");
            return;
        }
        if (warpManager.isNoWarpAvailable(faction)) {
            commandContext.sender.sendMessage("Vous n'avez plus de warp disponible !");
            return;
        }
        Location warp = warpManager.verifyWarpIsValid(faction, warpName, player.getLocation());
        if (warp == null) {
            commandContext.sender.sendMessage("§cVous devez être en territoire claim pour créer un warp !");
            return;
        }
        warpManager.setWarp(faction, warpName, warp);
        commandContext.sender.sendMessage("Warp " + warpName + " défini à votre position.");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
