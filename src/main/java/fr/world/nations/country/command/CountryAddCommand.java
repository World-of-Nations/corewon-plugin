package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.CountryManager;

public class CountryAddCommand extends FCommand {
    private final CountryManager countryManager;

    public CountryAddCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.aliases.add("add");
        this.requiredArgs.add("name");
    }

    @Override
    public void perform(CommandContext commandContext) {
        String name = commandContext.argAsString(0);
        if (countryManager.getCountry(name) != null) {
            commandContext.msg("That country already exists");
            return;
        }

        countryManager.addCountry(name);
        commandContext.msg("Country " + name + " added");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
