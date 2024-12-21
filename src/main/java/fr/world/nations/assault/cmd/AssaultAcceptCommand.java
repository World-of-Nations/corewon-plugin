package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;

public class AssaultAcceptCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultAcceptCommand(AssaultCommand rootCmd) {
        getAliases().add("accept");
        this.getRequiredArgs().add("faction");
        this.setRequirements(new CommandRequirements.Builder(Permission.RELATION).build());
        this.rootCmd = rootCmd;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (commandContext.faction == null || commandContext.faction.isWilderness()) {
            commandContext.sender.sendMessage("§cVous n'êtes dans aucune faction !");
            return;
        }
        if (!commandContext.fPlayer.getRole().isAtLeast(Role.MODERATOR)) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions pour effectuer cette commande !");
            return;
        }
        if (!rootCmd.getJoinRequests().containsValue(commandContext.faction)) {
            commandContext.sender.sendMessage("§cVous n'avez aucune requête en attente !");
            return;
        }
        if (!rootCmd.getPlugin().getAssaultManager().isInAssault(commandContext.faction)) {
            commandContext.sender.sendMessage("§cVous n'êtes actuellement pas dans un assaut !");
            return;
        }
        Faction faction = commandContext.argAsFaction(0);
        if (faction == null) return;
        if (rootCmd.getJoinRequests().get(faction) != commandContext.faction) {
            commandContext.sender.sendMessage("§cCette faction ne vous a pas envoyé de demande !");
            return;
        }
        commandContext.sender.sendMessage("§4[Assaut] §eVous avez accepté la demande de la faction §6" + faction.getTag() + " §e!");
        rootCmd.getPlugin().getAssaultManager().getAssault(commandContext.faction).join(commandContext.faction, faction);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
