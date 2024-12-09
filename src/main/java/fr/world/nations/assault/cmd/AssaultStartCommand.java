package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.util.TimerUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AssaultStartCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultStartCommand(AssaultCommand rootCmd) {
        aliases.add("start");
        this.rootCmd = rootCmd;
        requiredArgs.add("faction");
        this.requirements.permission = Permission.HELP;
        optionalArgs.put("enableExplosions", "no");
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
        Faction faction = commandContext.argAsFaction(0);
        if (faction == null) {
            return;
        }
        if (faction == commandContext.faction) {
            commandContext.sender.sendMessage("§cVous ne pouvez pas attaquer votre propre faction ! ;)");
            return;
        }
        if (commandContext.faction.getRelationTo(faction) != Relation.ENEMY) {
            commandContext.sender.sendMessage("§cVous ne pouvez qu'attaquer des pays ennemis !");
            return;
        }
        if (rootCmd.getPlugin().getAssaultManager().isInAssault(commandContext.faction)) {
            commandContext.sender.sendMessage("§cVous êtes déjà en assaut !");
            return;
        }
        if (rootCmd.getPlugin().getAssaultManager().isInAssault(faction)) {
            commandContext.sender.sendMessage("§cLe pays §6 " + faction.getTag() + " §cest déjà en assaut !");
            return;
        }
        if (!rootCmd.getPlugin().canAttack(commandContext.faction, faction)) {
            long remaining = rootCmd.getPlugin().getAttackRemaining(commandContext.faction, faction);
            DateFormat format = new SimpleDateFormat("mm:ss");
            String time = format.format(new Date(remaining));
            commandContext.sender.sendMessage("§cVous ne pouvez pas attaquer le pays §6" + faction.getTag() +
                    " §cen raison de votre défaite récente d'un assaut ! Temps restant : §c" + time);
            return;
        }
        int requiredOnlinePlayersNumb = rootCmd.getPlugin().getDefaultConfig().getInt("assault.required-online-players", 2);
        if (commandContext.faction.getOnlinePlayers().size() < requiredOnlinePlayersNumb) {
            commandContext.sender.sendMessage("§cVous devez être au moins " + requiredOnlinePlayersNumb + " joueurs connectés dans votre pays pour lancer un assaut !");
            return;
        }
        int factionAgeRequiredDays = rootCmd.getPlugin().getDefaultConfig().getInt("assault.faction-age-required-days");
        if (!TimerUtil.deltaUpDays(commandContext.faction.getFoundedDate(), factionAgeRequiredDays)) {
            commandContext.sender.sendMessage("§cVotre pays doit avoir été créé depuis au moins §6" + factionAgeRequiredDays + "§c jours pour lancer un assaut !");
            return;
        }
        int targetRequiredOnlinePlayersNumb = rootCmd.getPlugin().getDefaultConfig().getInt("assault.target-required-online-players", 2);
        if (faction.getOnlinePlayers().size() < targetRequiredOnlinePlayersNumb) {
            commandContext.sender.sendMessage("§cLe pays §6" + faction.getTag() + " §cdoit avoir au moins " + targetRequiredOnlinePlayersNumb + " joueurs connectés pour que vous puissiez lancer un assaut contre !");
            return;
        }
        boolean explosions = false;
        boolean tokenOk = rootCmd.getPlugin().getExplosionManager().tokenAvailable(commandContext.faction);
        boolean enemyOk = rootCmd.getPlugin().getExplosionManager().enemiesSinceSufficientTime(commandContext.faction, faction);
        String explosionArg = commandContext.argAsString(1);
        if (explosionArg != null) {
            explosionArg = explosionArg.toLowerCase();
            if (List.of("yes", "true", "explosion", "explosions").contains(explosionArg)) {
                explosions = true;
            } else if (!List.of("no", "false", "normal").contains(explosionArg)) {
                commandContext.sender.sendMessage("§cMauvais argument ! Faites sois §e/f assault " + faction.getTag()
                        + " true§c, sois §e/f assault " + faction.getTag() + " false§c.");
                return;
            }
            if (explosions) {
                if (!enemyOk) {
                    double daysRequired = rootCmd.getPlugin().getDefaultConfig().getDouble("explosions.enemy-required-time-days");
                    long milliseconds = rootCmd.getPlugin().getExplosionManager().enemySinceMillis(commandContext.faction, faction);
                    long second = (milliseconds / 1000) % 60;
                    long minute = (milliseconds / (1000 * 60)) % 60;
                    long hour = (milliseconds / (1000 * 60 * 60)) % 24;
                    long days = milliseconds / (1000 * 60 * 60 * 24);
                    String time = String.format("%d jours %02d heures %02d minutes %02d secondes", days, hour, minute, second);
                    commandContext.sender.sendMessage("§cVos deux pays doivent être ennemis depuis au moins §6" + daysRequired + " §cjours !");
                    commandContext.sender.sendMessage("§6Vous §cet §6" + faction.getTag() + " §cl'êtes depuis seulement §6" + time + " §c !");
                    return;
                }
                if (!tokenOk) {
                    commandContext.sender.sendMessage("§cVous n'avez plus de token disponible ! " +
                            "Attendez la semaine prochaine pour en récupérer !");
                    return;
                }
            }
        } else if (tokenOk && enemyOk) {
            commandContext.sender.sendMessage("§cLes explosions sont disponibles dans cet assaut ! Voulez-vous les utiliser ? Si " +
                    "c'est le cas faites : §e/f assault " + faction.getTag() + " true§c,");
            commandContext.sender.sendMessage("§csinon faites §e/f assault " + faction.getTag() + " false§c.");
            return;
        }
        rootCmd.getPlugin().getExplosionManager().withdrawToken(commandContext.faction);
        rootCmd.getPlugin().getAssaultManager().startAssault(commandContext.faction, faction, explosions);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
