package fr.world.nations.stats.events;

import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.StatsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListener implements Listener {


    @EventHandler
    public void onFactionCreate(FPlayerJoinEvent event) {
        if (event.getReason() != FPlayerJoinEvent.PlayerJoinReason.CREATE) return;
        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();
        statsManager.addFaction(event.getFaction());
    }

    @EventHandler
    public void onFactionDisband(FPlayerLeaveEvent event) {
        if (event.getReason() != FPlayerLeaveEvent.PlayerLeaveReason.DISBAND) return;
        StatsManager statsManager = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager();
        statsManager.removeFaction(event.getFaction());
    }

}
