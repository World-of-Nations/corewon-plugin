package fr.world.nations.stats.events;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import fr.world.nations.stats.data.StatsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void playerKillEvent(PlayerDeathEvent event) {

        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();

        Player killed = event.getEntity();
        Player killer = killed.getKiller();

        FPlayer fKilled = FPlayers.getInstance().getByPlayer(killed);
        Faction factionKilled = fKilled.getFaction();

        FPlayer fKiller = FPlayers.getInstance().getByPlayer(killer);
        Faction factionKiller = fKiller.getFaction();

        if (killer == null) return;
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

        statsManager.saveData();

    }

}
