package fr.world.nations.assault;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AssaultListener implements Listener {

    private final WonAssault plugin;

    public AssaultListener(WonAssault plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getAssaultManager().isInAssault(player)) return;
        Assault assault = plugin.getAssaultManager().getAssault(player);
        assault.onLogout(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AssaultManager assaultManager = plugin.getAssaultManager();
        if (!assaultManager.isInAssault(player)) {
            //assaultManager.updateCTag(player);
            return;
        }
        Assault assault = assaultManager.getAssault(player);
        assault.onLogin(player);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        if (!plugin.getAssaultManager().isInAssault(killed)) return;
        Assault assault = plugin.getAssaultManager().getAssault(killed);
        assault.onDeath(killed);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player attacked)) return;
        if (!plugin.getAssaultManager().isInAssault(attacker)) return;
        Assault assault = plugin.getAssaultManager().getAssault(attacker);
        if (assault.getModerators().contains(attacked)) {
            event.setCancelled(true);
            //Sécurité
            attacked.setHealth(20);
            attacker.sendMessage("§4[Assaut] §cVous ne pouvez pas attacker un modérateur qui surveille l'assaut !");
            return;
        }
        if (!plugin.getAssaultManager().isInAssault(attacked)) return;
        if (assault.areEnemies(attacker, attacked)) {
            event.setCancelled(false);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClaim(LandClaimEvent event) {
        final Faction faction = event.getFaction();
        if (!plugin.canClaim(faction)) {
            long remaining = plugin.getClaimRemaining(faction);
            DateFormat format = new SimpleDateFormat("mm:ss");
            String time = format.format(new Date(remaining));
            faction.msg("§cVous ne pouvez pas claim en raison de votre défaite récente d'un assaut ! Temps restant : §c" + time);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeaveFaction(FPlayerLeaveEvent event) {
        if (event.getReason() != FPlayerLeaveEvent.PlayerLeaveReason.LEAVE) return;
        Faction faction = event.getFaction();
        if (!plugin.getAssaultManager().isInAssault(faction)) return;
        if (faction.getOnlinePlayers().size() < 2) {
            Assault assault = plugin.getAssaultManager().getAssault(faction);
            assault.broadcast("§4[Assaut] §cLe pays §6" + faction.getTag() + " §cn'a plus assez de joueur en ligne pour participer " +
                    "à l'assaut ! Il en est donc retiré !");
            assault.onFactionQuit(faction);
        }
    }
}
