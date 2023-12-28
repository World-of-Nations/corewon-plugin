package fr.world.nations.assault;

import com.google.common.collect.Lists;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.Core;
import fr.world.nations.assault.database.DatabaseManager;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import fr.world.nations.stats.data.StatsManager;
import fr.world.nations.util.FactionUtil;
import fr.world.nations.util.StringUtil;
import fr.world.nations.util.TimerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class Assault {

    private final int msgLength = 30;
    private final WonAssault plugin;
    private final Faction defendant;
    private final List<Faction> defendantList;
    private final Faction attacker;
    private final List<Faction> attackerList;
    private final List<String> attackerDeaths;
    private final List<String> defendantDeaths;
    private final List<Player> moderators;
    private final boolean explosionsAllowed;
    private final boolean initExplosionAllowed;
    private final Map<UUID, Integer> logoutTaskIds = new HashMap<>();
    /*
    @Getter
    private final Map<UUID, Nametag> cTags = new HashMap<>();
    */
    private final String SHOULD_CLEAR_PREFIX = "CLEAR";
    private long assaultStartedMillis;
    private int taskId;
    private FLocation targetedClaim;
    private boolean targetedClaimSuccess;
    private float targetedClaimPercentage;
    private int defendantPoints;
    private int attackerPoints;
    private int stopTaskId = 0;
    private boolean running = false;

    public Assault(WonAssault plugin, Faction attacker, Faction defendant, boolean explosionsAllowed) {
        this.plugin = plugin;
        this.attacker = attacker;
        this.defendant = defendant;
        this.attackerList = Lists.newArrayList(attacker);
        this.defendantList = Lists.newArrayList(defendant);
        //setPrefixes(attacker);
        //setPrefixes(defendant);
        this.attackerDeaths = Lists.newArrayList();
        this.defendantDeaths = Lists.newArrayList();
        this.attackerPoints = 0;
        this.defendantPoints = 0;
        this.moderators = Lists.newArrayList();
        this.explosionsAllowed = explosionsAllowed;
        this.initExplosionAllowed = defendant.getPeacefulExplosionsEnabled();
        this.targetedClaimSuccess = false;
        this.targetedClaim = null;
        this.targetedClaimPercentage = 0;
        if (explosionsAllowed) {
            defendant.setPeacefulExplosionsEnabled(true);
        }
    }

    public void run() {
        running = true;
        Bukkit.broadcastMessage("§4[Assaut] §cUn assaut entre §6" + attacker.getTag() + " §cet §6" + defendant.getTag() + "§c a commencé !");
        broadcastRaw("§4" + StringUtil.mult("-", msgLength));
        broadcastRaw("");
        broadcastRaw("§4Assaut commencé");
        broadcastRaw("§6" + attacker.getTag() + " §cvs§c §6" + defendant.getTag());
        broadcastRaw("");
        broadcastRaw("§cFaites des kills pour gagner des points !");
        broadcastRaw("");
        broadcastRaw("§4Que le meilleur gagne !");
        long durationMin = plugin.getDefaultConfig().getLong("assault.duration-min");
        broadcastRaw("§cFin dans §6" + durationMin + " §cminutes");
        broadcastRaw("");


        broadcastRaw("");
        broadcastRaw("§4" + StringUtil.mult("-", msgLength));

        double targetChunkStartDelayMins = plugin.getDefaultConfig().getDouble("assault.target-chunk-start-delay-mins");
        broadcast("§cChunk à capturer désigné dans §6" + StringUtil.numb(targetChunkStartDelayMins) + " §cminutes !");

        if (explosionsAllowed) {
            broadcast("§cAttention, les attaquants ont utilisé leur §6jeton d'explosion§c, les explosions sont " +
                    "donc activées dans le territoire des défenseurs !");
        }

        this.assaultStartedMillis = System.currentTimeMillis();

        if (Board.getInstance().getAllClaims(defendant).isEmpty()) return; //TODO
        int delayTick = 5;
        this.taskId = new BukkitRunnable() {
            final long scoreBroadCastDelayMillis = plugin.getDefaultConfig().getLong("assault.score-broadcast-delay-secs") * 1000;
            long lastTimeBroadCastedScoreMillis = System.currentTimeMillis();
            long targetedClaimScoreMillis = 0;
            boolean claiming = false;

            @Override
            public void run() {
                try {
                    //Si l'event est terminé la tâche s'annule
                    long assaultDurationMin = plugin.getDefaultConfig().getLong("assault.duration-min");
                    if (TimerUtil.deltaUpMins(assaultStartedMillis, assaultDurationMin) || !running) {
                        end(false, true);
                        this.cancel();
                        return;
                    }
                    if (TimerUtil.deltaUpMillis(lastTimeBroadCastedScoreMillis, scoreBroadCastDelayMillis)) {
                        broadcastScore();
                        lastTimeBroadCastedScoreMillis = System.currentTimeMillis();
                    }
                    if (!targetedClaimSuccess) {
                        double targetChunkStartDelayMins = plugin.getDefaultConfig().getDouble("assault.target-chunk-start-delay-mins");
                        //System.out.println("targetChunkStartDelayMins : " + targetChunkStartDelayMins);
                        long targetChunkStartDelayMillis = (long) (targetChunkStartDelayMins * 60 * 1000);
                        if (TimerUtil.deltaUpMillis(assaultStartedMillis, targetChunkStartDelayMillis)) {
                            if (targetedClaim == null) {
                                //Sélectionner un chunk au hasard dans les claims de la faction attaquée
                                List<FLocation> claims = Lists.newArrayList(Board.getInstance().getAllClaims(defendant));
                                if (claims.isEmpty()) return;
                                Random random = new Random();
                                targetedClaim = claims.get(random.nextInt(claims.size()));
                                String locString = "§c(§6x:§r " + (targetedClaim.getChunk().getX() * 16 + 8)
                                        + " §6z:§r " + (targetedClaim.getChunk().getZ() * 16 + 8) + "§c)§r";
                                String chunkString = targetedClaim.toString() + locString;
                                broadcast("§cChunk à capturer désigné ! Il s'agit du chunk §6" + chunkString);
                            } else {
                                int attackerNumb = 0;
                                int defenderNumb = 0;
                                //Scanner toutes les entités dans le chunk
                                for (Entity entity : targetedClaim.getChunk().getEntities()) {
                                    if (!(entity instanceof Player player)) continue;
                                    if (!contains(player)) continue;
                                    Faction faction = FactionUtil.getFaction(player);
                                    //Si un seul n'appartient pas aux attaquants le timer est reset
                                    if (defendantList.contains(faction)) {
                                        defenderNumb += 1;
                                    } else if (attackerList.contains(faction)) {
                                        attackerNumb += 1;
                                    }
                                }
                                long requiredTimeMillis = plugin.getDefaultConfig().getLong("assault.target-chunk-unclaim-delay-sec") * 1000;
                                if ((defenderNumb >= attackerNumb)) {
                                    if (claiming) {
                                        broadcast("§cIl n'y a plus assez d'attaquant dans le claim cible ! Le compteur redescend...");
                                    }
                                    if (targetedClaimScoreMillis < 0) return;
                                    targetedClaimScoreMillis -= (50L * delayTick);
                                    if (targetedClaimScoreMillis < 0)
                                        targetedClaimScoreMillis = 0;
                                    targetedClaimPercentage = (float) targetedClaimScoreMillis / requiredTimeMillis * 100;
                                    claiming = false;
                                } else {
                                    claiming = true;
                                    if (targetedClaimScoreMillis == 0) {
                                        broadcast("§6Les attaquants §ccommencent à prendre le claim cible...");
                                    }
                                    targetedClaimScoreMillis += (50L * delayTick);
                                    targetedClaimPercentage = (float) targetedClaimScoreMillis / requiredTimeMillis * 100;
                                    if (targetedClaimScoreMillis >= requiredTimeMillis) {
                                        targetedClaimScoreMillis = requiredTimeMillis;
                                        targetedClaimPercentage = 1;
                                        //Envoi du message de log
                                        String chunkString = targetedClaim.toString();
                                        broadcast("§6Les attaquants §4ont détruit le claim cible ! §6(" + chunkString + ")");
                                        double cdHours = plugin.getDefaultConfig().getDouble("assault.claim-disabled-cooldown-hours");
                                        broadcast("§cLe pays §6" + defendant.getTag() + " §cn'a pas réussi à défendre" +
                                                " le claim cible ! Il est incapable de claim pendant §6" + StringUtil.numb(cdHours) +
                                                " §cheures.");
                                        plugin.addClaimCoolDown(defendant);

                                        //Ajout des points
                                        addAttackerPoint(plugin.getDefaultConfig().getInt("assault.target-chunk-success-points"));
                                        //Unclaim
                                        if (Board.getInstance().getFactionAt(targetedClaim) == defendant) {
                                            Board.getInstance().setFactionAt(Factions.getInstance().getWilderness(), targetedClaim);
                                        }
                                        targetedClaimSuccess = true;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(plugin.getLoader(), delayTick, delayTick).getTaskId();
    }

    public void end(boolean force, boolean calculateChunkPoints) {
        //Au cas où l'assaut devait être annulé
        Bukkit.getScheduler().cancelTask(taskId);

        /*for (Player player : getOnlinePlayers()) {
            UUID uniqueId = player.getUniqueId();
            if (cTags.containsKey(uniqueId)) {
                NametagEdit.getApi().setNametag(player, cTags.get(uniqueId).getPrefix(), cTags.get(uniqueId).getSuffix());
                cTags.remove(uniqueId);
            } else {
                NametagEdit.getApi().clearNametag(player);
            }
        }*/

        //Si le chunk n'a pas réussi à être capturé
        if (!targetedClaimSuccess && calculateChunkPoints) {
            broadcast("§cLes attaquants n'ont pas réussi à détruire le claim cible !");
            addDefendantPoint(plugin.getDefaultConfig().getInt("assault.target-chuck-fail-points"));
        }

        if (attackerPoints == defendantPoints) {
            broadcastRaw("§4" + StringUtil.mult("-", msgLength));
            broadcastRaw("");
            broadcastRaw("§4Assaut terminé");
            broadcastRaw("");
            broadcastRaw("§4A" + getFormattedScore() + "§4D");
            broadcastRaw("§6Egalité !");
            broadcastRaw("");
            broadcastRaw("§4" + StringUtil.mult("-", msgLength));

            if (!force) {
                plugin.addAttackCoolDown(attacker, defendant);
                saveDB("");
            }
        } else {
            //Comportement selon le gagnant
            Faction fWinner = attackerPoints > defendantPoints ? attacker : defendant;
            Faction fLoser = attackerPoints > defendantPoints ? defendant : attacker;
            String winner = attackerPoints > defendantPoints ? "Attaquants" : "Défenseurs";
            winner += " (" + fWinner.getTag() + ")";

            //Stats plugin principal
            StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();

            FactionData winnerData = statsManager.getFactionData(fWinner);
            FactionData loserData = statsManager.getFactionData(fLoser);

            int bankTransferPercentage = 0;
            double toTransfer = 0;
            if (!force) {
                winnerData.addAssaultWin();
                loserData.addAssaultLose();
                bankTransferPercentage = plugin.getDefaultConfig().getInt("assault.bank-transfer-percentage");
                double winnerBalance = fWinner.getFactionBalance();
                double loserBalance = fLoser.getFactionBalance();
                toTransfer = (bankTransferPercentage / 100d) * fLoser.getFactionBalance();
                fWinner.setFactionBalance(winnerBalance + toTransfer);
                fLoser.setFactionBalance(loserBalance - toTransfer);
            }


            broadcastRaw("§4" + StringUtil.mult("-", msgLength));
            broadcastRaw("");
            broadcastRaw("§4Assaut terminé");
            broadcastRaw("");
            broadcastRaw("§4A" + getFormattedScore() + "§4D");
            broadcastRaw("§6Gagnants : §c" + winner);
            if (!force) {
                broadcastRaw("");
                broadcastRaw("§6" + bankTransferPercentage + "% de la banque du pays §c" + fLoser.getTag());
                broadcastRaw("§6leur a été transféré");
            }
            broadcastRaw("");
            broadcastRaw("§4" + StringUtil.mult("-", msgLength));

            if (!force) {
                fWinner.msg("§a" + ((int) toTransfer) + "$ ont été ajoutés à votre banque de pays");
                fLoser.msg("§c" + ((int) toTransfer) + "$ ont été retirés de votre banque de pays");

                plugin.addAttackCoolDown(attacker, defendant);

                //Database
                saveDB(fWinner.getTag());


                if (loserData.getAssaultScore() <= -30) {
                    broadcast("§cLe pays §6" + fLoser.getTag() + " §ca un score de §6-30§c, il est donc réduit à néant !");

                    fLoser.disband(fLoser.getFPlayerLeader().getPlayer());
                }
            }
        }

        defendant.setPeacefulExplosionsEnabled(initExplosionAllowed);
        plugin.getAssaultManager().remove(this);
    }

    private void saveDB(String winnerName) {
        try {
            final Connection connection = DatabaseManager.WON_DB.getDatabaseAccess().getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `woncore_assaults` (attackerName, defenderName, attackerKill, defenderKill, winner, date_time) VALUES (?, ?, ?, ?, ?, ?)");

            preparedStatement.setString(1, attacker.getTag());
            preparedStatement.setString(2, defendant.getTag());
            preparedStatement.setInt(3, attackerPoints);
            preparedStatement.setInt(4, defendantPoints);
            preparedStatement.setString(5, winnerName);
            preparedStatement.setLong(6, Instant.now().getEpochSecond());

            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            System.out.println("Informations transférée à la base de données");
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    private void addAttackerPoint(int amount) {
        attackerPoints += amount;
        for (Player player : attacker.getOnlinePlayers()) {
            player.sendMessage("§4[Assaut] §6Votre faction §ea gagné §6" + amount + " §e points !");
        }
        for (Player player : defendant.getOnlinePlayers()) {
            player.sendMessage("§4[Assaut] §6Les attaquants §4ont gagné §c" + amount + " §4 points !");
        }
        broadcastScore();
    }

    private void addDefendantPoint(int amount) {
        defendantPoints += amount;
        for (Player player : defendant.getOnlinePlayers()) {
            player.sendMessage("§4[Assaut] §eVotre faction §ca gagné §6" + amount + " §c points !");
        }
        for (Player player : attacker.getOnlinePlayers()) {
            player.sendMessage("§4[Assaut] §6Les défenseurs §4ont gagné §c" + amount + " §4 points !");
        }
        broadcastScore();
    }

    private void broadcastScore() {
        String msg = "§6Score total (a-d) : " + getFormattedScore();
        broadcast(msg);
    }

    private String getFormattedScore() {
        String score;
        if (attackerPoints > defendantPoints) {
            score = "§a" + attackerPoints + "§6 - §c" + defendantPoints;
        } else if (defendantPoints > attackerPoints) {
            score = "§c" + attackerPoints + "§6 - §a" + defendantPoints;
        } else {
            score = "§7" + attackerPoints + "§6 - §7" + defendantPoints;
        }
        return score + "§r";
    }

    public void broadcast(String msg) {
        this.broadcastRaw("§4[Assaut] §r" + msg);
    }

    public void broadcastRaw(String msg) {
        for (Faction defFac : defendantList) {
            for (Player player : defFac.getOnlinePlayers()) {
                player.sendMessage(msg);
            }
        }
        for (Faction attackFac : attackerList) {
            for (Player player : attackFac.getOnlinePlayers()) {
                player.sendMessage(msg);
            }
        }
    }

    public void onLogout(Player player) {
        if (!contains(player)) return;
        int timeBfEndMin = 1;
        long assaultDurationMin = plugin.getDefaultConfig().getLong("assault.duration-min");
        if (TimerUtil.deltaUpMins(assaultStartedMillis, assaultDurationMin - timeBfEndMin))
            return;
        Faction faction = FactionUtil.getFaction(player);
        if (faction != null) {
            if (faction.getOnlinePlayers().size() == 0) {
                broadcast("§6" + player.getName() + " §cs'est déconecté !");
                broadcast("§4[Assaut] §cLe pays §6" + faction.getTag() + " §cn'a plus assez de joueur en ligne pour participer " +
                        "à l'assaut ! Il en est donc retiré !");
                onFactionQuit(faction);
                return;
            }
        }
        double logoutPenTime = plugin.getDefaultConfig().getDouble("assault.logout-penality-time-minutes");
        broadcast("§6" + player.getName() + " §cs'est déconecté ! Il a §6" + (int) logoutPenTime + " §cminutes pour se reconnecter ou il sera considéré comme mort !");
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                broadcast("§6" + player.getName() + " §cs'est déconecté depuis plus de §6" + logoutPenTime + " §cminutes !");
                onDeath(player, null);
            }
        }.runTaskLater(plugin.getLoader(), (long) (logoutPenTime * 60 * 20)).getTaskId();

        logoutTaskIds.put(player.getUniqueId(), taskId);
    }

    public void onDeath(Player killed) {
        onDeath(killed, killed.getKiller());
    }

    public void onDeath(Player killed, Player killer) {
        if (!contains(killed)) return;
        if (!contains(killer)) killer = null;
        Faction faction = FactionUtil.getFaction(killed);
        if (attackerList.contains(faction)) {
            attackerDeaths.add(killed.getName());
            defendantPoints += 1;
            if (killer == null) {
                broadcast("§cL'attaquant §6" + killed.getName() + " §cest mort ! §6(" + getFormattedScore() + "§6)");
            } else {
                broadcast("§cL'attaquant §6" + killed.getName() + " §ca été tué par le défenseur §6" + killer.getName() + " §c! §6(" + getFormattedScore() + "§6)");
            }
        }
        if (defendantList.contains(faction)) {
            defendantDeaths.add(killed.getName());
            attackerPoints += 1;
            if (killer == null) {
                broadcast("§cLe défenseur §6" + killed.getName() + " §cest mort ! §6(" + getFormattedScore() + "§6)");
            } else {
                broadcast("§cLe défenseur §6" + killed.getName() + " §ca été tué par l'attaquant §6" + killer.getName() + " §c! §6(" + getFormattedScore() + "§6)");
            }
        }
    }

    public boolean contains(Player player) {
        if (moderators.contains(player)) return true;
        return contains(FactionUtil.getFaction(player));
    }

    public boolean contains(Faction faction) {
        return attackerList.contains(faction) || defendantList.contains(faction);
    }

    public boolean areEnemies(Player p1, Player p2) {
        if (!contains(p1) || !contains(p2)) return false;
        if (moderators.contains(p1) || moderators.contains(p2)) return false;
        boolean p1Attacker = attackerList.contains(FactionUtil.getFaction(p1));
        boolean p2Defender = defendantList.contains(FactionUtil.getFaction(p2));
        return (p1Attacker && p2Defender) || (!p1Attacker && !p2Defender);
    }

    public void join(Faction inviter, Faction arrivant) {
        if (!contains(inviter)) return;
        if (contains(arrivant)) return;
        //setPrefixes(arrivant);
        if (attackerList.contains(inviter)) {
            attackerList.add(arrivant);
            broadcast("§cLa faction §6" + arrivant.getTag() + " §ca rejoint les attaquants !");
        } else {
            defendantList.add(arrivant);
            broadcast("§cLa faction §6" + arrivant.getTag() + " §ca rejoint les défenseurs !");
        }
    }

    /*private void setPrefixes(Faction faction) {
        faction.getOnlinePlayers().forEach(this::setAssaultCTag);
    }*/

    /*private void setAssaultCTag(Player player) {
        String attackerPrefix = "&4[&cAttaquant&4] &c";
        String defenderPrefix = "&2[&aDefenseur&2] &a";
        Nametag playerNt = NametagEdit.getApi().getNametag(player);
        if (playerNt != null && !(playerNt.getPrefix().equals("") && playerNt.getSuffix().equals(""))) {
            cTags.put(player.getUniqueId(), playerNt);
        } else {
            cTags.put(player.getUniqueId(), new Nametag(SHOULD_CLEAR_PREFIX, ""));
        }

        if (isAttacker(player)) {
            NametagEdit.getApi().setPrefix(player, attackerPrefix);
        } else {
            NametagEdit.getApi().setPrefix(player, defenderPrefix);
        }
    }*/

    /*public boolean removeCTag(Player player) {
        UUID uniqueId = player.getUniqueId();
        if (!cTags.containsKey(uniqueId)) return false;
        Nametag nametag = cTags.get(uniqueId);
        if (nametag.getPrefix().equals(SHOULD_CLEAR_PREFIX)) {
            NametagEdit.getApi().clearNametag(player);
        } else {
            NametagEdit.getApi().setNametag(player, cTags.get(uniqueId).getPrefix(), cTags.get(uniqueId).getSuffix());
        }
        cTags.remove(uniqueId);
        return true;
    }*/

    public FLocation getTargetedClaim() {
        return targetedClaim;
    }

    public List<Faction> getAttackerList() {
        return attackerList;
    }

    public List<Faction> getDefendantList() {
        return defendantList;
    }

    public List<Player> getModerators() {
        return moderators;
    }

    public void addModerator(Player player) {
        if (!player.hasPermission("assault.modo")) return;
        moderators.add(player);
        Bukkit.getServer().broadcastMessage("§4[Assaut] §6" + player.getName() + " §csurveille l'assaut §6"
                + attacker.getTag() + "§c VS §6" + defendant.getTag() + "§c !");
    }

    public void removeModerator(Player player) {
        moderators.remove(player);
        Bukkit.getServer().broadcastMessage("§4[Assaut] §6" + player.getName() + " §cne surveille plus l'assaut §6"
                + attacker.getTag() + "§c VS §6" + defendant.getTag() + "§c !");
    }

    public long getAssaultStartedMillis() {
        return assaultStartedMillis;
    }

    public Faction getAttacker() {
        return attacker;
    }

    public Faction getDefendant() {
        return defendant;
    }

    public int getAttackerPoints() {
        return attackerPoints;
    }

    public int getDefendantPoints() {
        return defendantPoints;
    }

    public float getTargetedClaimPercentage() {
        return targetedClaimPercentage;
    }

    public boolean isExplosionsAllowed() {
        return explosionsAllowed;
    }

    public List<Player> getOnlinePlayers() {
        List<Player> players = Lists.newArrayList();
        for (Faction faction : attackerList) {
            players.addAll(faction.getOnlinePlayers());
        }
        for (Faction faction : defendantList) {
            players.addAll(faction.getOnlinePlayers());
        }
        players.addAll(moderators);
        List<Player> finalList = Lists.newArrayList();
        players.stream().filter(Player::isOnline).forEach(finalList::add);
        return finalList;
    }

    public void onFactionQuit(Faction faction) {
        int stopCdMin = plugin.getDefaultConfig().getInt("assault.quit-stop-min");
        if (faction == attacker || faction == defendant) {
            boolean isAttacker = faction == attacker;
            String facState = isAttacker ? "Les attaquants" : "Les défenseurs";
            broadcast("§c" + facState + " ont quitté l'assaut ! Ils ont §6" + stopCdMin + "§c minutes pour se reconnecter !");
            this.stopTaskId = new BukkitRunnable() {
                @Override
                public void run() {
                    broadcast("§c" + facState + " ne se sont pas reconnectés à temps ! Ils ont donc perdu !");
                    int maxPoints = Math.max(attackerPoints, defendantPoints);
                    if (isAttacker) {
                        defendantPoints = maxPoints + 1;
                    } else {
                        attackerPoints = maxPoints + 1;
                    }
                    end(false, false);
                }
            }.runTaskLater(plugin.getLoader(), stopCdMin * 60 * 20L).getTaskId();
            return;
        }
        if (attackerList.contains(faction)) {
            broadcast("§cLes attaquants §6" + faction.getTag() + "§c ont quitté l'assaut !");
            attackerList.remove(faction);
            return;
        }
        if (defendantList.contains(faction)) {
            broadcast("§cLes défenseurs §6" + faction.getTag() + "§c ont quitté l'assaut !");
            defendantList.remove(faction);
        }
    }

    public void onLogin(Player player) {
        boolean isAttacker = isAttacker(player);
        //setAssaultCTag(player);
        if (isAttacker) {
            broadcast("§cL'attaquant §6" + player.getName() + "§c s'est reconnecté !");
        } else {
            broadcast("§cLe défenseur §6" + player.getName() + "§c s'est reconnecté !");
        }

        if (logoutTaskIds.containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(logoutTaskIds.get(player.getUniqueId()));
        }
        if (stopTaskId != 0) {
            if (isAttacker) {
                if (attacker.getOnlinePlayers().size() == 1) {
                    Bukkit.getScheduler().cancelTask(stopTaskId);
                    broadcast("§cLes attaquants se sont reconnectés, le combat reprend...");
                }
            } else {
                if (defendant.getOnlinePlayers().size() == 1) {
                    Bukkit.getScheduler().cancelTask(stopTaskId);
                    broadcast("§cLes défenseurs se sont reconnectés, le combat reprend...");
                }
            }
        }
    }

    public boolean isAttacker(Player player) {
        Faction faction = FactionUtil.getFaction(player);
        if (faction == null) return false;
        return attackerList.contains(faction);
    }
}
