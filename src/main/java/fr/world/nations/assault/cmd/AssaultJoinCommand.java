package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.assault.Assault;
import fr.world.nations.util.TimerUtil;
import org.bukkit.Bukkit;

import java.util.List;

public class AssaultJoinCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultJoinCommand(AssaultCommand rootCmd) {
        getAliases().add("join");
        this.getRequiredArgs().add("faction");
        this.getOptionalArgs().put("force", "no");
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
        int factionAgeRequiredDays = rootCmd.getPlugin().getDefaultConfig().getInt("assault.faction-age-required-days");
        if (!TimerUtil.deltaUpDays(commandContext.faction.getFoundedDate(), factionAgeRequiredDays)) {
            commandContext.sender.sendMessage("§cVotre pays doit avoir été créé depuis au moins §6" + factionAgeRequiredDays + "§c jours pour lancer un assaut !");
            return;
        }
        Faction faction = commandContext.argAsFaction(0);
        if (faction == null) return;
        if (faction == commandContext.faction) {
            commandContext.sender.sendMessage("§cVous ne pouvez pas rejoindre votre propre faction !");
            return;
        }
        if (faction.getRelationTo(commandContext.faction) != Relation.ALLY) {
            commandContext.sender.sendMessage("§cVous devez être alliés avec cette faction pour rejoindre son assaut !");
            return;
        }
        if (rootCmd.getPlugin().getAssaultManager().isInAssault(commandContext.faction)) {
            if (rootCmd.getPlugin().getAssaultManager().isInAssault(faction)) {
                Assault factionAssault = rootCmd.getPlugin().getAssaultManager().getAssault(faction);
                if (factionAssault.contains(commandContext.faction)) {
                    commandContext.sender.sendMessage("§cVous êtes déjà en assaut avec cette faction !");
                    return;
                }
            }
            commandContext.sender.sendMessage("§cVous ne pouvez pas rejoindre un assaut pendant que vous en effectuez un !");
            return;
        }
        if (!rootCmd.getPlugin().getAssaultManager().isInAssault(faction)) {
            commandContext.sender.sendMessage("§cLa faction §6" + faction.getTag() + " §cn'est actuellement pas en assaut !");
            return;
        }
        Assault assault = rootCmd.getPlugin().getAssaultManager().getAssault(faction);
        Faction oppositeFaction = assault.isAttacker(faction) ? assault.getDefendant() : assault.getAttacker();
        if (oppositeFaction.getRelationTo(commandContext.faction) != Relation.ENEMY) {
            commandContext.msg("§cVotre faction doit être ennemie avec " + oppositeFaction.getTag() + ",  la faction opposée à votre allié !");
            return;
        }
        if (rootCmd.getJoinRequests().containsKey(commandContext.faction)) {
            String forceArg = commandContext.argAsString(1, "false");
            if (List.of("true", "yes", "force").contains(forceArg)) {
                Faction removed = rootCmd.getJoinRequests().remove(commandContext.faction);
                commandContext.sender.sendMessage("§4[Assaut] §cVous avez annulé votre demande à la faction §6" + removed.getTag() + " §c!");
                Bukkit.getServer().dispatchCommand(commandContext.sender, "f assault join " + faction.getTag());
                return;
            }
            commandContext.sender.sendMessage("§cVous avez déjà une demande en attente ! Faites §c/f assault join " + faction.getTag() + " force §c" +
                    "pour l'annuler et envoyer une demande à cette faction !");
            return;
        }
        String msg = "§4[Assaut] §eLa faction §6" + commandContext.faction.getTag() + " vous propose son aide ! " +
                "Faites §c/f assault accept " + commandContext.faction.getTag() + " §e pour accepter !";
        faction.getFPlayersWhereOnline(true).stream()
                .filter(fPlayer -> fPlayer.getRole().isAtLeast(Role.MODERATOR))
                .forEach(fPlayer -> fPlayer.sendMessage(msg));

        rootCmd.getJoinRequests().put(commandContext.faction, faction);
        commandContext.sender.sendMessage("§4[Assaut] §eVous avez envoyé votre demande à la faction §6" + faction.getTag() + " §e!");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
