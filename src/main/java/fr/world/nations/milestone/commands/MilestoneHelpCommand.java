package fr.world.nations.milestone.commands;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;

import java.util.ArrayList;
import java.util.List;

public class MilestoneHelpCommand extends FCommand {

    public MilestoneHelpCommand() {
        super();
        this.requirements.permission = Permission.HELP;
        this.aliases.add("mhelp");

    }

    @Override
    public void perform(CommandContext context) {
        List<String> lines = new ArrayList<>();
        lines.add("§eUtilisation de la commande factionmilestone : ");
        if (context.player.hasPermission("fac-milestone.warps")) {
            if (context.player.hasPermission("fac-milestone.warps.op")) {
                lines.add("§e/f §cwarps [faction]");
                lines.add("§e/f §csetwarp <nom> [faction]");
                lines.add("§e/f §cdelwarp <nom> [faction]");
                lines.add("§e/f §cwarp <nom> [faction]");
            } else {
                lines.add("§e/f §cwarps");
                lines.add("§e/f §cwarp <nom>");
                FPlayer uPlayer = context.fPlayer;
                Faction faction = uPlayer.getFaction();
                Role rel = FPlayers.getInstance().getByPlayer(context.player).getRole();
                if (faction.getPermissions().get(rel).get(PermissableAction.SETHOME) == Access.ALLOW) {
                    lines.add("§e/f §cdelwarp <nom>");
                    lines.add("§e/f §cwarp <nom>");
                }
            }
        }
        if (context.sender.hasPermission("fac-milestone.milestone")) {
            if (context.sender.hasPermission("fac-milestone.milestone.op")) {
                lines.add("§e/f §caddxp <nombre> [faction]");
                lines.add("§e/f §cremovexp <nombre> [faction]");
                lines.add("§e/f §cresetopmodifier [faction]");
                lines.add("§e/f §cpalier [faction]");
                lines.add("§e/f §cdiagnosis [faction]");
            } else {
                lines.add("§e/f §clevel");
            }
            lines.add("§e/f §cmtop");
        }
        if (!context.sender.hasPermission("fac-milestone.warps") && !context.sender.hasPermission("fac-milestone.op-modifier")) {
            lines.add("§4Vous n'avez pas la permission d'utiliser cette commande !");
        }
        String msg = String.join("\n", lines);
        context.sender.sendMessage(msg);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
