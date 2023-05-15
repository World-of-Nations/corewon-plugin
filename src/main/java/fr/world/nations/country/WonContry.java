package fr.world.nations.country;

import com.massivecraft.factions.cmd.FCommand;
import fr.world.nations.country.command.CountryCommand;
import fr.world.nations.country.listener.FactionListener;
import fr.world.nations.country.sql.SQLManager;
import fr.world.nations.modules.WonModule;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class WonContry extends WonModule {
    private CountryManager countryManager;
    private SQLManager sqlManager;

    public WonContry(Plugin loader, String name) {
        super(loader, name);
    }

    @Override
    public void load() {
        sqlManager = new SQLManager(this);
        countryManager = new CountryManager(sqlManager);
    }

    @Override
    public void unload() {
        countryManager.saveData();
        try {
            sqlManager.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<FCommand> registerFCommands() {
        return Collections.singletonList(new CountryCommand(this.countryManager));
    }

    @Override
    public List<Listener> registerListeners() {
        return Collections.singletonList(new FactionListener(this.countryManager));
    }
}
