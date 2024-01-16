package fr.world.nations.pvp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PvpListener implements Listener {

    private final WonPvp plugin;
    private final PvpManager pvpManager;

    public PvpListener(WonPvp plugin) {
        this.plugin = plugin;
        this.pvpManager = plugin.getPvpManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (!pvpManager.isPvp(player)) return;

        player.setHealth(0);
    }

    @EventHandler
    public void onPlayerExecuteCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!pvpManager.isPvp(player)) return;
        if (pvpManager.commandIsBlocked(e.getMessage())) {
            e.setCancelled(true);
            player.sendMessage(plugin.getDefaultConfig().getString("command_blocked_message", (String) plugin.getDefaultConfigValues().get("command_blocked_message")));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if(!pvpManager.isPvp(player)) return;

        pvpManager.stopCountdown(player);

    }

    @EventHandler
    public void onPlayerPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player damaged) || !(e.getDamager() instanceof Player damager)) return;
        pvpManager.startCountdown(damaged, damager);
    }

}
