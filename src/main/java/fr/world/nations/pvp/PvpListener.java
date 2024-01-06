package fr.world.nations.pvp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        if(pvpManager.isPvp(player)) return;

        player.setHealth(0);
        player.kickPlayer(plugin.getDefaultConfig().getString("deco_combat_message", (String) plugin.getDefaultConfigValues().get("deco_combat_message")));
        pvpManager.stopCountdown(player);

    }

    @EventHandler
    public void onPlayerExecuteCommand(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        if(pvpManager.isPvp(player)) return;

        if(pvpManager.commandIsBlocked(e.getMessage())) {
            e.setCancelled(true);
            player.sendMessage(plugin.getDefaultConfig().getString("command_blocked_message", (String) plugin.getDefaultConfigValues().get("command_blocked_message")));
        }

    }

}
