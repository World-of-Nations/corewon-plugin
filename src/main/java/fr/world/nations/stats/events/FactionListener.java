package fr.world.nations.stats.events;

import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.StatsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListener implements Listener {


    @EventHandler
    public void onFactionCreate(FactionCreateEvent faction) {
        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();
        statsManager.addFaction(faction.getFPlayer().getFaction());
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent faction) {
        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();
        statsManager.removeFaction(faction.getFaction());
    }

}
