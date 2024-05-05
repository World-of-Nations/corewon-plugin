package fr.world.nations.pvp;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import fr.world.nations.Core;
import fr.world.nations.assault.Assault;
import fr.world.nations.assault.WonAssault;
import fr.world.nations.util.FactionUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;

public class PvpManager {
    private final WonPvp plugin;
    private final int countdown;

    private final List<String> commands;
    private final HashMap<Player, Long> countdown_pvp = new HashMap<>();
    private final HashMap<Player, HashMap<BossBar, BukkitTask>> countdown_visual = new HashMap<>();

    public PvpManager(WonPvp plugin, int countdown, List<String> commands) {
        this.plugin = plugin;
        this.countdown = countdown;
        this.commands = commands;
    }

    public void startCountdown(Player player) {

        if (!isPvp(player)) {
            player.sendMessage(plugin.getDefaultConfig().getString("pvp_message", (String) plugin.getDefaultConfigValues().get("pvp_message")));
        } else {
            cancelBossBar(player);
        }

        countdown_pvp.put(player, System.currentTimeMillis());

        BossBar bossBar = Bukkit.createBossBar(plugin.getDefaultConfig().getString("pvp_bar_message", (String) plugin.getDefaultConfigValues().get("pvp_bar_message")), BarColor.RED, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
            if (!isPvp(player)) {
                stopCountdown(player);
            } else {
                bossBar.setTitle(plugin.getDefaultConfig().getString("pvp_bar_message", (String) plugin.getDefaultConfigValues().get("pvp_bar_message")) + "§7(" + getCountdown(player) + "s)");
            }
        }, 0, 20);

        HashMap<BossBar, BukkitTask> map = new HashMap<>();
        map.put(bossBar, task);
        countdown_visual.put(player, map);

    }

    public void startCountdown(Player... players) {
        for (Player player : players) {
            if (player.isOp()) return;
            startCountdown(player);
        }
    }

    public void stopCountdown(Player player) {
        countdown_pvp.remove(player);
        cancelBossBar(player);
    }

    public void stopCountdown(Player... players) {
        for (Player player : players) {
            stopCountdown(player);
        }
    }

    public void cancelBossBar(Player player) {
        if (countdown_visual.containsKey(player)) {
            for (BossBar bossBar : countdown_visual.get(player).keySet()) {
                bossBar.setVisible(false);
                bossBar.removeAll();
                countdown_visual.get(player).get(bossBar).cancel();
                countdown_visual.get(player).remove(bossBar);
            }
        }
    }

    public int getCountdown(Player player) {
        return countdown - (int) ((System.currentTimeMillis() - countdown_pvp.get(player)) / 1000);
    }

    public boolean isPvp(Player player) {
        return countdown_pvp.containsKey(player) && getCountdown(player) > 0;
    }

    public boolean commandIsBlocked(String command) {
        for (String cmd : this.commands) {
            if (command.startsWith(cmd + " ") || command.equalsIgnoreCase(cmd)) {
                return true;
            }
        }
        return false;
    }

    public void onPlayerIsHitByPlayer(Player damaged, Player shooter, EntityDamageByEntityEvent e) {
        Faction damagedFaction = FactionUtil.getFaction(damaged);
        Faction shooterFaction = FactionUtil.getFaction(shooter);

        Relation relation = damagedFaction.getRelationTo(shooterFaction);

        Faction atDamagedFaction = Board.getInstance().getFactionAt(FLocation.wrap(damaged.getLocation()));
        Faction atShooterFaction = Board.getInstance().getFactionAt(FLocation.wrap(shooter.getLocation()));

        if(atDamagedFaction.isSafeZone() || atShooterFaction.isSafeZone()) {
            if (e != null) e.setCancelled(true);
            return;
        }

        if (damagedFaction.isWilderness()) {
            startCountdown(damaged, shooter);
            return;
        }

        if (damagedFaction.equals(shooterFaction)) {
            if (e != null) e.setCancelled(true);
            return;
        }

        if (relation.equals(Relation.ALLY) || relation.equals(Relation.TRUCE)) {
            if (e != null) e.setCancelled(true);
            return;
        }

        if (relation.equals(Relation.NEUTRAL)) {
            if (damagedFaction.equals(atDamagedFaction)) {
                if (e != null) e.setCancelled(true);
                return;
            }
        }

        startCountdown(damaged, shooter);
    }

    public void onPlayerIsHitByPlayer(Player damaged, Player shooter) {
        onPlayerIsHitByPlayer(damaged, shooter, null);
    }

}
