package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.Country;
import fr.world.nations.country.CountryManager;

public class CountryPosCommand extends FCommand {
    private final CountryManager countryManager;

    public CountryPosCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.aliases.add("pos");
        this.requiredArgs.add("name");
    }

    @Override
    public void perform(CommandContext commandContext) {
        String name = commandContext.argAsString(0);
        Country country = countryManager.getCountry(name);
        if (country == null) {
            commandContext.msg("That country doesn't exists");
            return;
        }

        country.setSpawn(commandContext.player.getLocation());
        commandContext.msg("Country spawn " + name + " set");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
