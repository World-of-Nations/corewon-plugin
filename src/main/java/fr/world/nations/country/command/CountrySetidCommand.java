package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.Country;
import fr.world.nations.country.CountryManager;

public class CountrySetidCommand extends FCommand {

    private final CountryManager countryManager;

    public CountrySetidCommand(CountryManager countryManager) {
        this.countryManager = countryManager;
        aliases.add("setid");
        requiredArgs.add("country_name");
        requiredArgs.add("id");
        optionalArgs.put("force", "no");
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.isOp()) {
            commandContext.sendMessage("§cVous n'avez pas les permissions de faire ça !");
            return;
        }
        String countryName = commandContext.argAsString(0);
        Country country = countryManager.getCountry(countryName);
        if (country == null) {
            commandContext.sendMessage("§cLe pays " + countryName + " n'existe pas !");
            return;
        }
        String id = commandContext.argAsString(1);
        boolean force = commandContext.argAsString(2, "").equalsIgnoreCase("force");
        if (country.getId() != null && !force) {
            commandContext.sendMessage("§cLe pays " + country.getName() + " possède déjà une id ! " +
                    "Si vous voulez tout de même la changer, veuillez effectuer la commande " +
                    "/f country setid " + country.getName() + " " + id + " force");
        }
        Country country1 = countryManager.getCountryById(id);
        if (country1 != null) {
            commandContext.sendMessage("§cL'id " + id + " est déjà possédée par le pays " + country1.getName() + " !");
        }
        String oldId = country.getId();
        try {
            countryManager.setId(country, id);
            commandContext.sendMessage("Id du pays " + country.getName() + " : " + oldId + " -> " + id);
        } catch (IllegalArgumentException e) {
            commandContext.sendMessage("§cIl y a eu une erreur : " + e.getMessage());
        }
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
