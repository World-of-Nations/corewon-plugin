package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.CountryManager;

public class CountryRemoveCommand extends FCommand {

    private final CountryManager countryManager;

    public CountryRemoveCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.aliases.add("remove");
        this.aliases.add("delete");
        this.requiredArgs.add("name");
    }

    @Override
    public void perform(CommandContext commandContext) {
        String name = commandContext.argAsString(0);
        if (countryManager.getCountry(name) == null) {
            commandContext.msg("That country does not exists");
            return;
        }

        countryManager.removeCountry(name);
        commandContext.msg("Country " + name + " removed");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
