package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.assault.Assault;

import java.util.List;

public class AssaultStopCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultStopCommand(AssaultCommand rootCmd) {
        aliases.add("stop");
        this.rootCmd = rootCmd;
        this.requiredArgs.add("faction");
        this.requirements.permission = Permission.HELP;
        this.optionalArgs.put("force", "no");
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("assault.op")) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        Faction faction = commandContext.argAsFaction(0, commandContext.faction);
        if (faction == null) return;
        if (!rootCmd.getPlugin().getAssaultManager().isInAssault(faction)) {
            commandContext.sender.sendMessage("§c" + faction.getTag() + " n'est actuellement pas dans un assaut !");
            return;
        }
        Assault assault = rootCmd.getPlugin().getAssaultManager().getAssault(faction);
        boolean force = List.of("yes", "true", "force").contains(commandContext.argAsString(1, "false"));
        assault.end(force, force);
        commandContext.sender.sendMessage("§4[Assaut] §eAssaut de la faction §6" + faction.getTag() + "§e fermé avec succès !");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
