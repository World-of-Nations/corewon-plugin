package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.Country;
import fr.world.nations.country.CountryManager;

public class CountryPosCommand extends FCommand {
    private final CountryManager countryManager;

    public CountryPosCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.getAliases().add("pos");
        this.getRequiredArgs().add("id or name");
        this.setRequirements(new CommandRequirements.Builder(Permission.ADMIN).build());

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

        country.setSpawn(commandContext.player.getLocation());
        commandContext.msg("Le spawn du pays " + country.getName() + " a été défini !");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
