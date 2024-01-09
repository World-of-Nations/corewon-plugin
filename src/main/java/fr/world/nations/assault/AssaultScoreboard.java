package fr.world.nations.assault;

import fr.world.nations.Core;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

public class AssaultScoreboard {
    private final Assault assault;
    private final ScoreboardManager manager;

    public AssaultScoreboard(Assault assault) {
        this.assault = assault;
        this.manager = Bukkit.getScoreboardManager();
    }

    public void setScoreboardActive(@NotNull Player p) {
        String title = "§4§lAssaut en cours";
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("WScoreboard", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        registerTeam(scoreboard, "space1", " ", 16);
        Team attacker = registerTeam(scoreboard, "attacker", "§c> Attaquant :§r ", 15);
        Team attacker_pts = registerTeam(scoreboard, "attacker_pts", "§6> Score att. :§r ", 14);
        registerTeam(scoreboard, "space2", "  ", 13);
        Team defender = registerTeam(scoreboard, "defender", "§c> Défenseur :§r ", 12);
        Team defender_pts = registerTeam(scoreboard, "defender_pts", "§6> Score def. :§r ", 11);
        registerTeam(scoreboard, "space3", "   ", 10);
        Team cd = registerTeam(scoreboard, "cd", "§c> Temps restant :§r ", 9);
        Team explosions = registerTeam(scoreboard, "explosions", "§c> Explosions activées :§r ", 8);
        registerTeam(scoreboard, "space5", "     ", 7);
        registerTeam(scoreboard, "chunk", "§4§l> Chunk à capturer", 6);
        registerTeam(scoreboard, "space6", "      ", 5);
        Team chunk_coords = registerTeam(scoreboard, "chunk_coords", "§c> Coordonnées :§r ", 4);
        Team chunk_arrow = registerTeam(scoreboard, "chunk_arrow", "§c> Direction :§r ", 3);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline() || !assault.isRunning() || !assault.contains(p)) {
                    p.setScoreboard(manager.getNewScoreboard());
                    this.cancel();
                    return;
                }
                attacker.setSuffix(assault.getAttacker().getTag() +
                        (assault.getAttackerList().size() == 1 ? "" : "(+" + (assault.getAttackerList().size() - 1)));
                attacker_pts.setSuffix("" + assault.getAttackerPoints());
                defender.setSuffix(assault.getDefendant().getTag() +
                        (assault.getDefendantList().size() == 1 ? "" : "(+" + (assault.getDefendantList().size() - 1)));
                defender_pts.setSuffix("" + assault.getDefendantPoints());
                long currentTimeMillis = System.currentTimeMillis();
                long assaultDurationMin = assault.getPlugin().getDefaultConfig().getLong("assault.duration-min");
                long assaultStartedMillis = assault.getAssaultStartedMillis();

                // Calculer le temps restant en millisecondes, puis convertir en minutes et secondes
                long timeLeftMillis = assaultStartedMillis + assaultDurationMin * 60_000 - currentTimeMillis;
                int minutesLeft = (int) (timeLeftMillis / 60_000);
                int secondsLeft = (int) (timeLeftMillis % 60_000) / 1000;
                String countdown = String.format("%02d:%02d", minutesLeft, secondsLeft);
                cd.setSuffix(countdown);
                explosions.setSuffix(assault.isExplosionsAllowed() ? "§aoui" : "§4non");
                String coords;
                Chunk targetedChunk = assault.getTargetedClaim() == null ? null : assault.getTargetedClaim().getChunk();
                Location midChunkLocation;
                if (targetedChunk != null) {
                    int x = (targetedChunk.getX() << 4) + 8; // Coordonnée X du bloc central
                    int y = 64; // Hauteur standard, peut être ajustée
                    int z = (targetedChunk.getZ() << 4) + 8; // Coordonnée Z du bloc central
                    midChunkLocation = new Location(targetedChunk.getWorld(), x, y, z);
                    coords = String.format("X: %d Y: %d Z: %d", x, y, z);
                } else {
                    midChunkLocation = null;
                    coords = "X: ? Y: ? Z: ?";
                }

                String arrow = getDirectionArrow(p, midChunkLocation);
                chunk_coords.setSuffix(coords);
                chunk_arrow.setSuffix(arrow);
            }
        }.runTaskTimer(Core.getInstance(), 0, 20);
        p.setScoreboard(scoreboard);
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
        // Obtenir la location actuelle du joueur
        Location playerLocation = player.getLocation();

        // Calculer la direction vers laquelle le joueur doit se diriger
        double deltaX = targetLocation.getX() - playerLocation.getX();
        double deltaZ = targetLocation.getZ() - playerLocation.getZ();
        double angleToTarget = Math.atan2(deltaZ, deltaX);
        double playerAngle = Math.toRadians(playerLocation.getYaw());

        // Normaliser les angles
        double deltaAngle = Math.PI - ((playerAngle - angleToTarget) % (2 * Math.PI));

        // Déterminer la direction générale
        if (deltaAngle < 0.3927 || deltaAngle > 5.8905) return "→"; // Est (45 degrés de marge)
        else if (deltaAngle >= 0.3927 && deltaAngle < 2.7489) return "↑"; // Nord
        else if (deltaAngle >= 2.7489 && deltaAngle < 3.5343) return "←"; // Ouest
        else return "↓"; // Sud
    }
}
