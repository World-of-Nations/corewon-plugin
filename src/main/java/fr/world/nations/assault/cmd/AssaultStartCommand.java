package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
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
        System.out.println(tokenOk + " " + enemyOk);
        String explosionArg = commandContext.argAsString(1);
        if (explosionArg != null) {
            if (List.of("yes", "true", "explosion", "explosions").contains(explosionArg.toLowerCase())) {
                if (!enemyOk) {
                    long daysRequired = rootCmd.getPlugin().getDefaultConfig().getLong("explosions.enemy-required-time-days");
                    DateFormat format = new SimpleDateFormat("dd hh mm");
                    String time = format.format(new Date(rootCmd.getPlugin().getExplosionManager().enemySinceMillis(commandContext.faction, faction)));
                    commandContext.sender.sendMessage("§cVos deux pays doivent être ennemis depuis au moins §6" + daysRequired + " §cjours !");
                    commandContext.sender.sendMessage("§6Vous §cet §6" + faction.getTag() + " §cl'êtes depuis seulement §6" + time + " §c !");
                } else explosions = true;
            } else if (!List.of("no", "false", "normal").contains(explosionArg.toLowerCase()) && enemyOk && tokenOk) {
                commandContext.sender.sendMessage("§cMauvais argument ! Faites sois §e/f assault " + faction.getTag()
                        + " true§c, sois §e/f assault " + faction.getTag() + " no§c.");
                return;
            }
        } else if (tokenOk && enemyOk) {
            commandContext.sender.sendMessage("§cLes explosions sont disponibles dans cet assaut ! Voulez-vous les utiliser ? Si " +
                    "c'est le cas faites : §e/f assault " + faction.getTag() + " explosions§c,");
            commandContext.sender.sendMessage("§csinon faites §e/f assault " + faction.getTag() + " false§c.");
            return;
        }
        if (explosions) {
            if (!rootCmd.getPlugin().getExplosionManager().withdrawToken(commandContext.faction)) {
                commandContext.sender.sendMessage("§cVous n'avez plus de token disponible ! Attendez la semaine prochaine pour en récupérer !");
                return;
            }
        }
        rootCmd.getPlugin().getAssaultManager().startAssault(commandContext.faction, faction, explosions);
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}