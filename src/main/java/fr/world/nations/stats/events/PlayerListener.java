package fr.world.nations.stats.events;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import fr.world.nations.stats.data.StatsManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler
    public void playerKillEvent(PlayerDeathEvent event) {

        Player killed = event.getEntity();
        Player killer = killed.getKiller();
        if (killer == null) return;

        WonStats wonStats = Core.getInstance().getModuleManager().getModule(WonStats.class);
        StatsManager statsManager = wonStats.getStatsManager();

        World world = killed.getWorld();
        List<String> allowedWorlds = wonStats.getConfig().getStringList("player-kills.allowed-worlds");
        if (allowedWorlds.isEmpty()) {
            boolean shouldWarn = wonStats.getConfig().getBoolean("player-kills.no-world-warn");
            if (shouldWarn) {
                Core.getInstance().getLogger().warning("No world specified for player kills tracking ! " +
                        "If done on purpose, you can disable this warning in the config file");
            }
        }
        if (!allowedWorlds.contains(world.getName())) return;

        FPlayer fKilled = FPlayers.getInstance().getByPlayer(killed);
        Faction factionKilled = fKilled.getFaction();

        FPlayer fKiller = FPlayers.getInstance().getByPlayer(killer);
        Faction factionKiller = fKiller.getFaction();

        if (factionKilled == factionKiller) return;

        if (factionKilled != null) {
            FactionData factionData = statsManager.getFactionData(factionKilled.getTag());
            if (factionData != null) {
                factionData.setDeaths(factionData.getDeaths() + 1);
            }
        }

        if (factionKiller != null) {
            FactionData factionData = statsManager.getFactionData(factionKiller.getTag());
            if (factionData != null) {
                factionData.setKills(factionData.getKills() + 1);
            }
        }
    }
}
