package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.assault.Assault;

public class AssaultModoCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultModoCommand(AssaultCommand rootCmd) {
        getAliases().add("modo");
        getRequiredArgs().add("faction");
        this.setRequirements(new CommandRequirements.Builder(Permission.HELP).build());
        this.rootCmd = rootCmd;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("assault.modo")) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        Faction faction = commandContext.argAsFaction(0);
        if (faction == null) return;
        if (!rootCmd.getPlugin().getAssaultManager().isInAssault(faction)) {
            commandContext.sender.sendMessage("§c" + faction.getTag() + " n'est actuellement pas dans un assaut !");
            return;
        }
        Assault assault = rootCmd.getPlugin().getAssaultManager().getAssault(faction);
        if (assault.getModerators().contains(commandContext.player)) {
            assault.removeModerator(commandContext.player);
            return;
        }
        assault.addModerator(commandContext.player);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
