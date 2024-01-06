package fr.world.nations.pvp.weapons;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import fr.world.nations.Core;
import fr.world.nations.assault.Assault;
import fr.world.nations.assault.WonAssault;
import fr.world.nations.pvp.PvpManager;
import fr.world.nations.pvp.WonPvp;
import fr.world.nations.util.FactionUtil;
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
        Entity bullet = event.getDamager();
        if (!bullet.getName().contains("entity.cgm.projectile")) return;

        Player shooter = bulletShooter.get(bullet);
        if (shooter == null) return;

        if (!(event.getEntity() instanceof Player damaged)) return;


        this.onPlayerIsHitByBullet(damaged, shooter, event);
    }


    @EventHandler
    public void EntitySpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        if (!entity.getName().contains("entity.cgm.projectile")) return;

        Player shooter = (Player) entity.getNearbyEntities(0, 0, 0).stream().filter(e -> e instanceof Player).findFirst().orElse(null);
        if (shooter == null) return;

        bulletShooter.add(entity, shooter);
    }

    public void onPlayerIsHitByBullet(Player damaged, Player shooter, EntityDamageByEntityEvent e) {
        Faction damagedFaction = FactionUtil.getFaction(damaged);
        Faction shooterFaction = FactionUtil.getFaction(shooter);

        Relation relation = damagedFaction.getRelationTo(shooterFaction);

        Faction atDamagedFaction = Board.getInstance().getFactionAt(FLocation.wrap(damaged.getLocation()));
        Faction atShooterFaction = Board.getInstance().getFactionAt(FLocation.wrap(shooter.getLocation()));

        if (damagedFaction.equals(shooterFaction)) {
            e.setCancelled(true);
            return;
        }

        if (relation.equals(Relation.ALLY) || relation.equals(Relation.TRUCE)) {
            e.setCancelled(true);
            return;
        }

        if (relation.equals(Relation.NEUTRAL)) {
            if (damagedFaction.equals(atDamagedFaction)) {
                e.setCancelled(true);
                return;
            }
        }

        if (relation.equals(Relation.ENEMY)) {
            WonAssault wonAssault = Core.getInstance().getModuleManager().getModule(WonAssault.class);
            if (wonAssault == null) return;

            Assault assault = wonAssault.getAssaultManager().getAssault(damagedFaction);
            if (assault == null || !assault.contains(shooterFaction)) {
                if (damagedFaction.equals(atDamagedFaction)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        pvpManager.startCountdown(damaged, shooter);
    }

}
