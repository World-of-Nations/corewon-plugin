package fr.world.nations.assault.explosion;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FactionRelationEvent;
import com.massivecraft.factions.struct.Relation;
import fr.world.nations.assault.Assault;
import fr.world.nations.assault.WonAssault;
import fr.world.nations.util.FactionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ExplosionListener implements Listener {

    private final WonAssault plugin;

    public ExplosionListener(WonAssault plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnemy(FactionRelationEvent event) {
        if (event.getOldRelation() == Relation.ENEMY && event.getRelation() != Relation.ENEMY) {
            plugin.getExplosionManager().setEnemies(event.getFaction(), event.getTargetFaction(), false);
            return;
        }
        if (event.getRelation() != Relation.ENEMY) return;
        plugin.getExplosionManager().setEnemies(event.getFaction(), event.getTargetFaction(), true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFlagChange(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length < 5) return;
        if (!args[0].equalsIgnoreCase("/f")) return;
        if (!args[1].equalsIgnoreCase("flag")) return;
        if (!args[3].toLowerCase().contains("explosion")) return;
        String factionName = args[2];
        Faction faction = FactionUtil.getFaction(factionName);
        if (faction == null) return;
        if (!plugin.getAssaultManager().isInAssault(faction)) return;
        Assault assault = plugin.getAssaultManager().getAssault(faction);
        if (assault.getDefendant() == faction && assault.isExplosionsAllowed()) {
            event.getPlayer().sendMessage("§cBien tenté !");
            event.setCancelled(true);
        }
    }
}
