package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.Country;
import fr.world.nations.country.CountryManager;

public class CountryRemoveCommand extends FCommand {

    private final CountryManager countryManager;

    public CountryRemoveCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.requirements.permission = Permission.HELP;
        this.aliases.add("remove");
        this.aliases.add("delete");
        this.requiredArgs.add("name");
    }

    @Override
    public void perform(CommandContext commandContext) {
        String id = commandContext.argAsString(0);
        Country country = countryManager.getCountryById(id);
        if (country == null) {
            country = countryManager.getCountry(id);
            if (country == null) {
                commandContext.msg("That country doesn't exists");
                return;
            }
        }

        countryManager.removeCountry(country);
        commandContext.msg("Country " + country.getName() + " removed");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
