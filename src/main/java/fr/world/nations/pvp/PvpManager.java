package fr.world.nations.pvp;

import fr.world.nations.Core;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
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
        }

        countdown_pvp.put(player, System.currentTimeMillis());

        BossBar bossBar = Bukkit.createBossBar("§cVous êtes en combat !", BarColor.RED, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(Core.getInstance(), () -> {
            if (!isPvp(player)) {
                stopCountdown(player);
            } else {
                bossBar.setTitle("§cVous êtes en combat ! §7(" + getCountdown(player) + "s)");
            }
        }, 0, 20);

        HashMap<BossBar, BukkitTask> map = new HashMap<>();
        map.put(bossBar, task);
        countdown_visual.put(player, map);

    }

    public void startCountdown(Player... players) {
        for (Player player : players) {
            startCountdown(player);
        }
    }

    public void stopCountdown(Player player) {
        countdown_pvp.remove(player);

        if (countdown_visual.containsKey(player)) {
            for (BossBar bossBar : countdown_visual.get(player).keySet()) {
                bossBar.setVisible(false);
                bossBar.removeAll();
                countdown_visual.get(player).get(bossBar).cancel();
                countdown_visual.get(player).remove(bossBar);
            }
        }

    }

    public void stopCountdown(Player... players) {
        for (Player player : players) {
            stopCountdown(player);
        }
    }

    public int getCountdown(Player player) {
        return (int) ((countdown_pvp.getOrDefault(player, 0L) + countdown) - System.currentTimeMillis());
    }

    public boolean isPvp(Player player) {
        return !countdown_pvp.containsKey(player) || getCountdown(player) > 0;
    }

    public boolean commandIsBlocked(String command) {
        for (String cmd : commands) {
            if (command.startsWith(cmd) || command.equalsIgnoreCase(cmd)) {
                return true;
            }
        }
        return false;
    }

}
