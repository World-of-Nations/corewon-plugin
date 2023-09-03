package fr.world.nations.country;

import com.massivecraft.factions.cmd.FCommand;
import fr.world.nations.country.command.CountryCommand;
import fr.world.nations.country.listener.FactionListener;
import fr.world.nations.modules.WonModule;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class WonContry extends WonModule {
    private CountryManager countryManager;

    public WonContry(Plugin loader, String name) {
        super(loader, name);
    }

    public CountryManager getCountryManager() {
        return countryManager;
    }

    @Override
    public void load() {
        countryManager = new CountryManager(this);
    }

    @Override
    public void unload() {
        countryManager.saveData();
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
