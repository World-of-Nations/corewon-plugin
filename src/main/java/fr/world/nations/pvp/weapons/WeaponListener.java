package fr.world.nations.pvp.weapons;

import fr.world.nations.pvp.PvpManager;
import fr.world.nations.pvp.WonPvp;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class WeaponListener implements Listener {

    private final PvpManager pvpManager;
    private final FixedSizeCache<Entity, Player> bulletShooter = new FixedSizeCache<>(100);

    public WeaponListener(WonPvp plugin) {
        this.pvpManager = plugin.getPvpManager();
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        Entity bullet = event.getDamager();
        if (!bullet.getName().contains("entity.cgm.projectile")) return;

        Player shooter = bulletShooter.get(bullet);
        if (shooter == null) return;

        if (!(event.getEntity() instanceof Player damaged)) return;

        pvpManager.onPlayerIsHitByPlayer(shooter, damaged, event);
    }


    @EventHandler
    public void EntitySpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        if (!entity.getName().contains("entity.cgm.projectile")) return;

        Player shooter = (Player) entity.getNearbyEntities(0, 0, 0).stream().filter(e -> e instanceof Player).findFirst().orElse(null);
        if (shooter == null) return;

        bulletShooter.add(entity, shooter);
    }

}
