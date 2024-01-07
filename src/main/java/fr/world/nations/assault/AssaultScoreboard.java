package fr.world.nations.assault;

import fr.world.nations.Core;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class AssaultScoreboard {
    private final Assault assault;
    private final ScoreboardManager manager;
    private final Scoreboard board;
    private final Objective objective;

    public AssaultScoreboard(Assault assault) {
        this.assault = assault;
        this.manager = Bukkit.getScoreboardManager();
        this.board = manager.getNewScoreboard();
        this.objective = board.registerNewObjective("custom", "dummy", "Titre du Scoreboard");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!assault.isRunning()) {
                    assault.getOnlinePlayers().forEach(p -> p.setScoreboard(manager.getNewScoreboard()));
                    cancel();
                    return;
                }
                for (Player player : assault.getOnlinePlayers()) {
                    //Should not happen
                    if (!player.isOnline() || !assault.contains(player)) continue;
                    setScoreboardLines(player);
                }
            }
        }.runTaskTimer(Core.getInstance(), 0L, 20L);
    }

    private void setScoreboardLines(Player player) {
        // Vérifier si l'assaut est toujours en cours
        if (!assault.isRunning()) {
            player.setScoreboard(manager.getNewScoreboard()); // Cacher le scoreboard
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        long assaultDurationMin = assault.getPlugin().getDefaultConfig().getLong("assault.duration-min");
        long assaultStartedMillis = assault.getAssaultStartedMillis();

        // Calculer le temps restant en millisecondes, puis convertir en minutes et secondes
        long timeLeftMillis = assaultStartedMillis + assaultDurationMin * 60_000 - currentTimeMillis;
        int minutesLeft = (int) (timeLeftMillis / 60_000);
        int secondsLeft = (int) (timeLeftMillis % 60_000) / 1000;
        String countdown = String.format("%02d:%02d", minutesLeft, secondsLeft);

        // Coordonnées
        String coords;
        Chunk targetedChunk = assault.getTargetedClaim().getChunk();
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

        String arrow = getDirectionArrow(player, midChunkLocation);

        // Mise à jour du scoreboard
        objective.getScore("Temps restant: " + countdown).setScore(4);
        objective.getScore("Chunk déterminé: " + (targetedChunk != null ? "Oui" : "Non")).setScore(3);
        objective.getScore("Coords: " + coords).setScore(2);
        objective.getScore("Direction: " + arrow).setScore(1);

        player.setScoreboard(board);
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
