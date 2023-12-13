package fr.world.nations.assault;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.util.FactionUtil;
import fr.world.nations.util.TimerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AssaultCommand extends FCommand {

    private final WonAssault plugin;
    private final HashMap<Faction, Faction> joinRequests; //Demandant - Demandé

    public AssaultCommand(WonAssault plugin) {
        aliases.add("assault");
        this.plugin = plugin;
        this.joinRequests = new HashMap<>();
        optionalArgs.put("arg1", "");
        optionalArgs.put("arg2", "");
        optionalArgs.put("arg3", "");
        this.requirements.playerOnly = true;
    }

    @Override
    public void perform(CommandContext commandContext) {
        /*if (!commandContext.sender.hasPermission("assault")) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }*/
        if (commandContext.argAsString(0) == null) {
            if (plugin.getAssaultManager().isInAssault(commandContext.player)) {
                commandContext.sender.sendMessage("§cLes interfaces d'assaut arrivent bientôt !");
                //TODO ouverture de l'interface
            } else sendHelp(commandContext);
            return;
        }
        switch (commandContext.argAsString(0)) {
            case "join":
                if (commandContext.faction == null || commandContext.faction.isWilderness()) {
                    commandContext.sender.sendMessage("§cVous n'êtes dans aucune faction !");
                    return;
                }
                if (!commandContext.fPlayer.getRole().isAtLeast(Role.MODERATOR)) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions pour effectuer cette commande !");
                    return;
                }
                int factionAgeRequiredDays = plugin.getDefaultConfig().getInt("assault.faction-age-required-days");
                if (!TimerUtil.deltaUpDays(commandContext.faction.getFoundedDate(), factionAgeRequiredDays)) {
                    commandContext.sender.sendMessage("§cVotre pays doit avoir été créé depuis au moins §6" + factionAgeRequiredDays + "§c jours pour lancer un assaut !");
                    return;
                }
                Faction faction = commandContext.argAsFaction(1);
                if (faction == null) return;
                if (faction == commandContext.faction) {
                    commandContext.sender.sendMessage("§cVous ne pouvez pas rejoindre votre propre faction !");
                    return;
                }
                if (faction.getRelationTo(commandContext.faction) != Relation.ALLY) {
                    commandContext.sender.sendMessage("§cVous devez être alliés avec cette faction pour rejoindre son assaut !");
                    return;
                }
                if (plugin.getAssaultManager().isInAssault(commandContext.faction)) {
                    if (plugin.getAssaultManager().isInAssault(faction)) {
                        Assault factionAssault = plugin.getAssaultManager().getAssault(faction);
                        if (factionAssault.contains(commandContext.faction)) {
                            commandContext.sender.sendMessage("§cVous êtes déjà en assaut avec cette faction !");
                            return;
                        }
                    }
                    commandContext.sender.sendMessage("§cVous ne pouvez pas rejoindre un assaut pendant que vous en effectuez un !");
                    return;
                }
                if (!plugin.getAssaultManager().isInAssault(faction)) {
                    commandContext.sender.sendMessage("§cLa faction §6" + faction.getTag() + " §cn'est actuellement pas en assaut !");
                    return;
                }
                if (joinRequests.containsKey(commandContext.faction)) {
                    if (commandContext.argAsString(2) != null && commandContext.argAsString(2).equalsIgnoreCase("force")) {
                        Faction removed = joinRequests.remove(commandContext.faction);
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

                joinRequests.put(commandContext.faction, faction);
                commandContext.sender.sendMessage("§4[Assaut] §eVous avez envoyé votre demande à la faction §6" + faction.getTag() + " §e!");
                break;
            case "accept":
                if (commandContext.faction == null || commandContext.faction.isWilderness()) {
                    commandContext.sender.sendMessage("§cVous n'êtes dans aucune faction !");
                    return;
                }
                if (!commandContext.fPlayer.getRole().isAtLeast(Role.MODERATOR)) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions pour effectuer cette commande !");
                    return;
                }
                if (!joinRequests.containsValue(commandContext.faction)) {
                    commandContext.sender.sendMessage("§cVous n'avez aucune requête en attente !");
                    return;
                }
                if (!plugin.getAssaultManager().isInAssault(commandContext.faction)) {
                    commandContext.sender.sendMessage("§cVous n'êtes actuellement pas dans un assaut !");
                    return;
                }
                faction = commandContext.argAsFaction(1);
                if (faction == null) return;
                if (joinRequests.get(faction) != commandContext.faction) {
                    commandContext.sender.sendMessage("§cCette faction ne vous a pas envoyé de demande !");
                    return;
                }
                commandContext.sender.sendMessage("§4[Assaut] §eVous avez accepté la demande de la faction §6" + faction.getTag() + " §e!");
                plugin.getAssaultManager().getAssault(commandContext.faction).join(commandContext.faction, faction);
                break;
            case "list":
                if (this.plugin.getAssaultManager().getAssaults().isEmpty()) {
                    commandContext.sender.sendMessage("§cAucun assault en cours");
                    return;
                }

                StringBuilder builder = new StringBuilder("§6Liste des assauts en cours :\n \n");

                for (Assault assault : this.plugin.getAssaultManager().getAssaults()) {
                    long timeMillis = System.currentTimeMillis() - assault.getAssaultStartedMillis();
                    DateFormat format = new SimpleDateFormat("mm:ss");
                    String time = format.format(new Date(timeMillis));

                    String line = ChatColor.translateAlternateColorCodes('&', String.format("- %s (&c%d&6) &f/ &6(&c%d&6) %s  &c%s", assault.getAttacker().getTag(),
                            assault.getAttackerPoints(), assault.getDefendantPoints(),
                            assault.getDefendant().getTag(), time));

                    builder.append("§6").append(line).append("\n");
                }
                commandContext.sender.sendMessage(builder.toString());
                break;
            case "stop":
                if (!commandContext.sender.hasPermission("assault.op")) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
                    return;
                }
                faction = commandContext.argAsFaction(1, commandContext.faction);
                if (faction == null) return;
                if (!plugin.getAssaultManager().isInAssault(faction)) {
                    commandContext.sender.sendMessage("§c" + faction.getTag() + " n'est actuellement pas dans un assaut !");
                    return;
                }
                Assault assault = plugin.getAssaultManager().getAssault(faction);
                boolean force = !((commandContext.argAsString(1) != null) && (commandContext.argAsString(1).equalsIgnoreCase("natural")));
                assault.end(force, force);
                commandContext.sender.sendMessage("§4[Assaut] §eAssaut de la faction §6" + faction.getTag() + "§e fermé avec succès !");
                break;
            case "modo":
                if (!commandContext.sender.hasPermission("assault.modo")) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
                    return;
                }
                faction = commandContext.argAsFaction(1);
                if (faction == null) return;
                if (!plugin.getAssaultManager().isInAssault(faction)) {
                    commandContext.sender.sendMessage("§c" + faction.getTag() + " n'est actuellement pas dans un assaut !");
                    return;
                }
                assault = plugin.getAssaultManager().getAssault(faction);
                if (assault.getModerators().contains(commandContext.player)) {
                    assault.removeModerator(commandContext.player);
                    return;
                }
                assault.addModerator(commandContext.player);
                break;
            case "resetattackcd":
                if (!commandContext.sender.hasPermission("assault.op")) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
                    return;
                }
                if (commandContext.argAsString(1) == null) {
                    sendHelp(commandContext);
                    return;
                }
                Faction attacker = commandContext.argAsFaction(1);
                if (attacker == null) return;
                if (commandContext.argAsString(2) == null) {
                    sendHelp(commandContext);
                    return;
                }
                Faction defender = commandContext.argAsFaction(2);
                if (defender == null) return;
                boolean removed = plugin.removeAttackCoolDown(attacker, defender);
                if (removed) {
                    commandContext.sender.sendMessage("§4[Assaut] §eLa faction §6" + attacker.getTag() + "§e peut de nouveau attaquer la faction §6" + defender.getTag() + "§e !");
                } else {
                    commandContext.sender.sendMessage("§4[Assaut] §cLa faction §6" + attacker.getTag() + "§c peut déjà attaquer la faction §6" + defender.getTag() + "§c !");
                }
                break;
            case "resetclaimcd":
                if (!commandContext.sender.hasPermission("assault.op")) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
                    return;
                }
                attacker = commandContext.argAsFaction(1, commandContext.faction);
                if (attacker == null) return;
                removed = plugin.removeClaimCoolDown(attacker);
                if (removed) {
                    commandContext.sender.sendMessage("§4[Assaut] §eLa faction §6" + attacker.getTag() + " §e peut de nouveau claim !");
                } else {
                    commandContext.sender.sendMessage("§4[Assaut] §cLa faction §6" + attacker.getTag() + " §c peut déjà claim !");
                }
                break;
            case "tokens":
                faction = commandContext.argAsFaction(1, commandContext.faction);
                if (faction == null) return;
                if (!commandContext.sender.hasPermission("assault.modo") && faction != commandContext.faction) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
                    return;
                }
                int tokenAmount = plugin.getExplosionManager().getTokenAmount(faction);
                commandContext.sender.sendMessage("Nombre de tokens de la faction " + faction.getTag() + " : " + tokenAmount);
                break;
            case "disableexplosions":
                if (!commandContext.sender.hasPermission("assault.op")) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
                    return;
                }
                List<String> affectedFactions = new ArrayList<>();
                for (Faction faction1 : FactionUtil.getAllPlayerFactions()) {
                    faction1.setPeacefulExplosionsEnabled(false);
                    affectedFactions.add(faction1.getTag());
                }
                commandContext.sender.sendMessage("Explosions désactivées pour les factions : " + String.join(" ", affectedFactions));
                break;
            default:
                faction = commandContext.argAsFaction(0);
                if (commandContext.faction == null || commandContext.faction.isWilderness()) {
                    commandContext.sender.sendMessage("§cVous n'êtes dans aucune faction !");
                    return;
                }
                if (!commandContext.fPlayer.getRole().isAtLeast(Role.MODERATOR)) {
                    commandContext.sender.sendMessage("§cVous n'avez pas les permissions pour effectuer cette commande !");
                    return;
                }
                if (faction == null) {
                    commandContext.sender.sendMessage("§cLa faction §6" + commandContext.argAsString(0) + " §cn'existe pas !");
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
                if (plugin.getAssaultManager().isInAssault(commandContext.faction)) {
                    commandContext.sender.sendMessage("§cVous êtes déjà en assaut !");
                    return;
                }
                if (plugin.getAssaultManager().isInAssault(faction)) {
                    commandContext.sender.sendMessage("§cLe pays §6 " + faction.getTag() + " §cest déjà en assaut !");
                    return;
                }
                if (!plugin.canAttack(commandContext.faction, faction)) {
                    long remaining = plugin.getAttackRemaining(commandContext.faction, faction);
                    DateFormat format = new SimpleDateFormat("mm:ss");
                    String time = format.format(new Date(remaining));
                    commandContext.sender.sendMessage("§cVous ne pouvez pas attaquer le pays §6" + faction.getTag() +
                            " §cen raison de votre défaite récente d'un assaut ! Temps restant : §c" + time);
                    return;
                }
                int requiredOnlinePlayersNumb = plugin.getDefaultConfig().getInt("assault.required-online-players", 2);
                if (commandContext.faction.getOnlinePlayers().size() < requiredOnlinePlayersNumb) {
                    commandContext.sender.sendMessage("§cVous devez être au moins " + requiredOnlinePlayersNumb + " joueurs connectés dans votre pays pour lancer un assaut !");
                    return;
                }
                factionAgeRequiredDays = plugin.getDefaultConfig().getInt("assault.faction-age-required-days");
                if (!TimerUtil.deltaUpDays(commandContext.faction.getFoundedDate(), factionAgeRequiredDays)) {
                    commandContext.sender.sendMessage("§cVotre pays doit avoir été créé depuis au moins §6" + factionAgeRequiredDays + "§c jours pour lancer un assaut !");
                    return;
                }
                int targetRequiredOnlinePlayersNumb = plugin.getDefaultConfig().getInt("assault.target-required-online-players", 2);
                if (faction.getOnlinePlayers().size() < targetRequiredOnlinePlayersNumb) {
                    commandContext.sender.sendMessage("§cLe pays §6" + faction.getTag() + " §cdoit avoir au moins " + targetRequiredOnlinePlayersNumb + " joueurs connectés pour que vous puissiez lancer un assaut contre !");
                    return;
                }
                boolean explosions = false;
                boolean tokenOk = plugin.getExplosionManager().tokenAvailable(commandContext.faction);
                boolean enemyOk = plugin.getExplosionManager().enemiesSinceSufficientTime(commandContext.faction, faction);
                System.out.println(tokenOk + " " + enemyOk);
                if (commandContext.argAsString(1) != null) {
                    if (commandContext.argAsString(1).equalsIgnoreCase("explosions")) {
                        if (!enemyOk) {
                            long daysRequired = plugin.getDefaultConfig().getLong("explosions.enemy-required-time-days");
                            DateFormat format = new SimpleDateFormat("dd hh mm");
                            String time = format.format(new Date(plugin.getExplosionManager().enemySinceMillis(commandContext.faction, faction)));
                            commandContext.sender.sendMessage("§cVos deux pays doivent être ennemis depuis au moins §6" + daysRequired + " §cjours !");
                            commandContext.sender.sendMessage("§6Vous §cet §6" + faction.getTag() + " §cl'êtes depuis seulement §6" + time + " §c !");
                        }
                        explosions = true;
                    } else if (!commandContext.argAsString(1).equalsIgnoreCase("normal") && enemyOk && tokenOk) {
                        commandContext.sender.sendMessage("§cMauvais argument ! Faites sois §e/f assault " + faction.getTag()
                                + " explosions§c, sois §e/f assault " + faction.getTag() + " normal§c.");
                        return;
                    }
                } else if (tokenOk && enemyOk) {
                    commandContext.sender.sendMessage("§cLes explosions sont disponibles dans cet assaut ! Voulez-vous les utiliser ? Si " +
                            "c'est le cas faites : §e/f assault " + faction.getTag() + " explosions§c,");
                    commandContext.sender.sendMessage("§csinon faites §e/f assault " + faction.getTag() + " normal§c.");
                    return;
                }
                if (explosions) {
                    if (!plugin.getExplosionManager().withdrawToken(commandContext.faction)) {
                        commandContext.sender.sendMessage("§cVous n'avez plus de token disponible ! Attendez la semaine prochaine pour en récupérer !");
                        return;
                    }
                }
                plugin.getAssaultManager().startAssault(commandContext.faction, faction, explosions);
                break;
        }
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }

    private void sendHelp(CommandContext commandContext) {
        List<String> lines = new ArrayList<>();
        lines.add("§eUtilisation de la commande assault : ");
        if (commandContext.sender.hasPermission("assault")) {
            lines.add("§e/f §cassault");
            lines.add("§e/f §cassault <faction>");
            lines.add("§e/f §cassault join <allié en assaut>");
            lines.add("§e/f §cassault accept <allié>");
            lines.add("§e/f §cassault list");
            if (commandContext.sender.hasPermission("assault.modo")) {
                lines.add("§e/f §cassault modo <faction>");
            }
            if (commandContext.sender.isOp()) {
                lines.add("§e/f §cassault stop <faction> [natural]");
                lines.add("§e/f §cassault resetattackcd <attaquant> <défenseur>");
                lines.add("§e/f §cassault resetclaimcd <faction>");
            }
        }
        if (!commandContext.sender.hasPermission("assault")) {
            lines.add("§4Vous n'avez pas la permission d'utiliser cette commande !");
        }
        String msg = String.join("\n", lines);
        commandContext.sender.sendMessage(msg);
    }
}
