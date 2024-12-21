package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.CountryManager;

public class CountryAddCommand extends FCommand {
    private final CountryManager countryManager;

    public CountryAddCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.getAliases().add("add");
        this.getRequiredArgs().add("id");
        this.getRequiredArgs().add("name");
        this.setRequirements(new CommandRequirements.Builder(Permission.ADMIN).build());
    }

    @Override
    public void perform(CommandContext commandContext) {
        String id = commandContext.argAsString(0);
        if (!id.matches("[a-z]+")) {
            commandContext.sendMessage("§cVeuillez entrer une id sous forme de succession de lettres de l'alphabet non majuscules ! Exemple : fr pour France");
            return;
        }
        if (countryManager.getCountryById(id) != null) {
            commandContext.sendMessage("§cLe pays " + countryManager.getCountryById(id).getName() + " possède déjà cette id !");
            return;
        }
        String name = commandContext.argAsString(1);

        countryManager.addCountry(id, name);
        commandContext.msg("Country " + name + " added");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
