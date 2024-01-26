package fr.world.nations.assault;

import dev.jcsoftware.jscoreboards.JPerPlayerScoreboard;
import fr.world.nations.Core;
import fr.world.nations.util.StringUtil;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AssaultScoreboard {
    private final Assault assault;
    private final JPerPlayerScoreboard scoreboard;
    private final TabAPI tabAPI;
    private final NameTagManager nameTagManager;
    private final TabListFormatManager tabListFormatManager;
    private final Map<UUID, String> previousPrefixes;
    private final List<Player> players;

    public AssaultScoreboard(Assault assault) {
        players = new ArrayList<>();
        this.assault = assault;
        scoreboard = new JPerPlayerScoreboard(
                (player) -> "§4§lAssaut en cours", // The title supplier
                (player) -> { // The lines supplier
                    if (!player.isOnline() || !assault.isRunning() || !assault.contains(player)) {
                        return new ArrayList<>();
                    }
                    List<String> lines = new ArrayList<>();
                    lines.add(" ");
                    String attackerSuffix = assault.getAttacker().getTag() +
                            (assault.getAttackerList().size() == 1 ? "" : " (+" + (assault.getAttackerList().size() - 1) + ")");
                    String attackerPointSuffix = "" + assault.getAttackerPoints();
                    String defenderSuffix = assault.getAttacker().getTag() +
                            (assault.getAttackerList().size() == 1 ? "" : " (+" + (assault.getAttackerList().size() - 1) + ")");
                    String defenderPointSuffix = "" + assault.getDefendantPoints();
                    lines.add("§c> Attaquant :§r " + attackerSuffix);
                    lines.add("§6> Score :§r " + attackerPointSuffix);
                    lines.add(" ");
                    lines.add("§c> Défenseur :§r " + defenderSuffix);
                    lines.add("§6> Score :§r " + defenderPointSuffix);
                    lines.add(" ");

                    long currentTimeMillis = System.currentTimeMillis();
                    long assaultDurationMin = assault.getPlugin().getDefaultConfig().getLong("assault.duration-min");
                    long assaultStartedMillis = assault.getAssaultStartedMillis();

                    // Calculer le temps restant en millisecondes, puis convertir en minutes et secondes
                    long timeLeftMillis = assaultStartedMillis + assaultDurationMin * 60_000 - currentTimeMillis;
                    int minutesLeft = (int) (timeLeftMillis / 60_000);
                    int secondsLeft = (int) (timeLeftMillis % 60_000) / 1000;
                    String countdown = String.format("%02d:%02d", minutesLeft, secondsLeft);
                    String explosionSuffix = assault.isExplosionsAllowed() ? "§aoui" : "§4non";
                    lines.add("§c> Temps restant :§r " + countdown);
                    lines.add("§c> Explosions activées :§r " + explosionSuffix);
                    lines.add(" ");
                    lines.add("§4§l> Chunk à capturer");
                    lines.add(" ");
                    if (assault.getTargetedClaim() == null) {
                        double targetChunkStartDelayMins = assault.getPlugin().getDefaultConfig().getDouble("assault.target-chunk-start-delay-mins");
                        long timeUntilChunkChosenMillis = (long) (targetChunkStartDelayMins * 60 * 1000);
                        timeLeftMillis = assaultStartedMillis + timeUntilChunkChosenMillis - currentTimeMillis;
                        minutesLeft = (int) (timeLeftMillis / 60_000);
                        secondsLeft = (int) (timeLeftMillis % 60_000) / 1000;
                        countdown = String.format("%02d:%02d", minutesLeft, secondsLeft);
                        lines.add("§c> Disponible dans :§r " + countdown);
                    } else {
                        if (assault.claimCaptured()) {
                            lines.add((assault.isAttacker(player) ? "§a" : "§c") + "Chunk capturé");
                            return lines;
                        }
                        Chunk targetedChunk = assault.getTargetedClaim().getChunk();
                        int x = (targetedChunk.getX() << 4) + 8; // Coordonnée X du bloc central
                        int y = 64; // Hauteur standard, peut être ajustée
                        int z = (targetedChunk.getZ() << 4) + 8; // Coordonnée Z du bloc central
                        Location midChunkLocation = new Location(targetedChunk.getWorld(), x, y, z);
                        String coords = String.format("X: %d Y: %d Z: %d", x, y, z);
                        String arrow = getDirectionArrow(player, midChunkLocation);
                        lines.add("§c> Coordonnées :§r " + coords);
                        lines.add("§c> Direction :§r " + arrow);
                        lines.add("§c> Pourcentage :§r " + StringUtil.round(assault.getChunkCapturePercentage(), 2) + "%");
                    }
                    return lines;
                }
        );

        Bukkit.getScheduler().runTaskTimer(Core.getInstance(), scoreboard::updateScoreboard, 0, 20);
        Bukkit.getScheduler().runTaskTimer(Core.getInstance(),
                () -> {
                    List<Player> toRemove = new ArrayList<>();
                    for (Player player : players)
                        if (!player.isOnline() || !assault.isRunning() || !assault.contains(player)) {
                            removeScoreboard(player);
                            toRemove.add(player);
                        }
                    toRemove.forEach(players::remove);
                }, 0, 20);
        tabAPI = TabAPI.getInstance();
        nameTagManager = tabAPI.getNameTagManager();
        tabListFormatManager = TabAPI.getInstance().getTabListFormatManager();
        previousPrefixes = new HashMap<>();
    }

    public void setScoreboardActive(@NotNull Player p) {
//        String title = "§4§lAssaut en cours";
//        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
//        Objective obj = scoreboard.registerNewObjective("WScoreboard", "dummy", title);
//        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
//        registerTeam(scoreboard, "space1", " ", 16);
//        Team attacker = registerTeam(scoreboard, "attacker", "§c> Attaquant :§r ", 15);
//        Team attacker_pts = registerTeam(scoreboard, "attacker_pts", "§6> Score att. :§r ", 14);
//        registerTeam(scoreboard, "space2", "  ", 13);
//        Team defender = registerTeam(scoreboard, "defender", "§c> Défenseur :§r ", 12);
//        Team defender_pts = registerTeam(scoreboard, "defender_pts", "§6> Score def. :§r ", 11);
//        registerTeam(scoreboard, "space3", "   ", 10);
//        Team cd = registerTeam(scoreboard, "cd", "§c> Temps restant :§r ", 9);
//        Team explosions = registerTeam(scoreboard, "explosions", "§c> Explosions activées :§r ", 8);
//        registerTeam(scoreboard, "space5", "     ", 7);
//        registerTeam(scoreboard, "chunk", "§4§l> Chunk à capturer", 6);
//        registerTeam(scoreboard, "space6", "      ", 5);
//        final Team[] chunk_coords = {null};
//        final Team[] chunk_arrow = {null};
//        final Team[] chunk_percentage = {null};
//        Team claim_countdown = registerTeam(scoreboard, "claim_cd", "§c> Disponible dans :§r ", 4);
//
//        final boolean[] claimInfoDisplayed = {false};
//        final boolean[] claimDoneDisplayed = {false};
        players.add(p);
        scoreboard.addPlayer(p);
        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
        if (nameTagManager.getCustomPrefix(tabPlayer) != null)
            previousPrefixes.put(p.getUniqueId(), nameTagManager.getCustomPrefix(tabPlayer));
        String prefix;
        if (assault.isAttacker(p)) {
            prefix = assault.getPlugin().getDefaultConfig().getString("assault-attacker-prefix", "[Assaut - attaquant]");
        } else {
            prefix = assault.getPlugin().getDefaultConfig().getString("assault-defender-prefix", "[Assaut - défenseur]");
        }
        nameTagManager.setPrefix(tabPlayer, prefix);
        tabListFormatManager.setPrefix(tabPlayer, prefix);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (!p.isOnline() || !assault.isRunning() || !assault.contains(p)) {
//                    if (p.isValid())
//                        p.setScoreboard(manager.getNewScoreboard());
//                    this.cancel();
//                    return;
//                }
//                attacker.setSuffix(assault.getAttacker().getTag() +
//                        (assault.getAttackerList().size() == 1 ? "" : " (+" + (assault.getAttackerList().size() - 1) + ")"));
//                attacker_pts.setSuffix("" + assault.getAttackerPoints());
//                defender.setSuffix(assault.getDefendant().getTag() +
//                        (assault.getDefendantList().size() == 1 ? "" : " (+" + (assault.getDefendantList().size() - 1) + ")"));
//                defender_pts.setSuffix("" + assault.getDefendantPoints());
//                long currentTimeMillis = System.currentTimeMillis();
//                long assaultDurationMin = assault.getPlugin().getDefaultConfig().getLong("assault.duration-min");
//                long assaultStartedMillis = assault.getAssaultStartedMillis();
//
//                // Calculer le temps restant en millisecondes, puis convertir en minutes et secondes
//                long timeLeftMillis = assaultStartedMillis + assaultDurationMin * 60_000 - currentTimeMillis;
//                int minutesLeft = (int) (timeLeftMillis / 60_000);
//                int secondsLeft = (int) (timeLeftMillis % 60_000) / 1000;
//                String countdown = String.format("%02d:%02d", minutesLeft, secondsLeft);
//                cd.setSuffix(countdown);
//                explosions.setSuffix(assault.isExplosionsAllowed() ? "§aoui" : "§4non");
//
//                if (assault.getTargetedClaim() == null) {
//                    double targetChunkStartDelayMins = assault.getPlugin().getDefaultConfig().getDouble("assault.target-chunk-start-delay-mins");
//                    long timeUntilChunkChosenMillis = (long) (targetChunkStartDelayMins * 60 * 1000);
//                    timeLeftMillis = assaultStartedMillis + timeUntilChunkChosenMillis - currentTimeMillis;
//                    minutesLeft = (int) (timeLeftMillis / 60_000);
//                    secondsLeft = (int) (timeLeftMillis % 60_000) / 1000;
//                    countdown = String.format("%02d:%02d", minutesLeft, secondsLeft);
//                    claim_countdown.setSuffix(countdown);
//                } else {
//                    if (assault.claimCaptured()) {
//                        if (!claimDoneDisplayed[0]) {
//                            chunk_coords[0].getEntries().forEach(chunk_coords[0]::removeEntry);
//                            chunk_arrow[0].getEntries().forEach(chunk_arrow[0]::removeEntry);
//                            chunk_percentage[0].getEntries().forEach(chunk_percentage[0]::removeEntry);
//                            chunk_coords[0].unregister();
//                            chunk_arrow[0].unregister();
//                            chunk_percentage[0].unregister();
//                            registerTeam(scoreboard, "claim_done", (assault.isAttacker(p) ? "§a" : "§c") + "Chunk capturé", 4);
//                            claimDoneDisplayed[0] = true;
//                        }
//                        return;
//                    }
//                    if (!claimInfoDisplayed[0]) {
//                        claim_countdown.getEntries().forEach(claim_countdown::removeEntry);
//                        claim_countdown.unregister();
//                        chunk_coords[0] = registerTeam(scoreboard, "chunk_coords", "§c> Coordonnées :§r ", 4);
//                        chunk_arrow[0] = registerTeam(scoreboard, "chunk_arrow", "§c> Direction :§r ", 3);
//                        chunk_percentage[0] = registerTeam(scoreboard, "chunk_percentage", "§c> Pourcentage :§r ", 2);
//                        claimInfoDisplayed[0] = true;
//                    }
//                    Chunk targetedChunk = assault.getTargetedClaim().getChunk();
//                    int x = (targetedChunk.getX() << 4) + 8; // Coordonnée X du bloc central
//                    int y = 64; // Hauteur standard, peut être ajustée
//                    int z = (targetedChunk.getZ() << 4) + 8; // Coordonnée Z du bloc central
//                    Location midChunkLocation = new Location(targetedChunk.getWorld(), x, y, z);
//                    String coords = String.format("X: %d Y: %d Z: %d", x, y, z);
//                    String arrow = getDirectionArrow(p, midChunkLocation);
//                    chunk_coords[0].setSuffix(coords);
//                    chunk_arrow[0].setSuffix(arrow);
//                    chunk_percentage[0].setSuffix(assault.getChunkCapturePercentage() + "%");
//                }
//
//            }
//        }.runTaskTimer(Core.getInstance(), 0, 20);
//        p.setScoreboard(scoreboard);
    }

    public void removeScoreboard(@NotNull Player p) {
        scoreboard.removePlayer(p);
        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
        String newPrefix = previousPrefixes.get(p.getUniqueId());
        if (newPrefix == null) newPrefix = "";
        nameTagManager.setPrefix(tabPlayer, newPrefix);
        tabListFormatManager.setPrefix(tabPlayer, newPrefix);
    }

    public Team registerTeam(@NotNull Scoreboard scoreboard, String id, String entry, int score) {
        Team team = scoreboard.registerNewTeam(id);
        team.addEntry(entry);
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        assert objective != null;
        objective.getScore(entry).setScore(score);
        return team;
    }

    private String getDirectionArrow(Player player, Location targetLocation) {
        if (targetLocation == null || player == null) return "?";

        Location targetLoc = targetLocation.clone();
        Location pLoc = player.getLocation().clone();

        Vector pDir = new Vector(pLoc.getDirection().getX(), 0, pLoc.getDirection().getZ()).normalize();
        Vector toDir = targetLoc.clone().subtract(pLoc).toVector().normalize();
        toDir = new Vector(toDir.getX(), 0, toDir.getZ()).normalize();

        double angle = Math.acos(pDir.dot(toDir));

        Vector crossProduct = pDir.crossProduct(toDir);
        angle *= (crossProduct.getY() / (Math.abs(crossProduct.getY())));

        double yaw = Math.toDegrees(angle);

        System.out.println(yaw);

        if (-45 < yaw && yaw <= 45) return "↑";
        if (-135 < yaw && yaw <= -45) return "→";
        if (45 < yaw && yaw <= 135) return "←";
        return "↓";
    }
}
