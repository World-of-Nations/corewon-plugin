package fr.world.nations.stats;

import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.cmd.FCommand;
import fr.world.nations.modules.WonModule;
import fr.world.nations.stats.commands.StatsCommand;
import fr.world.nations.stats.data.StatsManager;
import fr.world.nations.stats.events.FactionListener;
import fr.world.nations.stats.events.PlayerListener;
import fr.world.nations.stats.sql.SQLManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WonStats extends WonModule {

    private static WonStats instance;
    private SQLManager sqlManager;
    private StatsManager statsManager;

    public WonStats(Plugin loader, String name) {
        super(loader, name);
    }

    @Override
    public void load() {
        sqlManager = new SQLManager();
        statsManager = new StatsManager();

//        File dbFile = new File(getConfigFolder(), "wonstats.db");
//        if (!dbFile.exists()) {
//            try {
//                dbFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void unload() {
        try {
            sqlManager.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    @Override
    public List<Listener> registerListeners() {
        ArrayList<Listener> listeners = new ArrayList<>();
        listeners.add(new FactionListener());
        listeners.add(new PlayerListener());
        FCmdRoot.instance.cmdShow.aliases.add("f");

        return listeners;
    }

    @Override
    public List<FCommand> registerFCommands() {
        ArrayList<FCommand> commands = new ArrayList<>();
        commands.add(new StatsCommand());

        return commands;
    }

    @Override
    protected Map<String, Object> getDefaultConfigValues() {
        return Map.of(
                "player-kills.allowed-worlds", List.of("world"),
                "player-kills.no-world-warn", true
        );
    }
}
