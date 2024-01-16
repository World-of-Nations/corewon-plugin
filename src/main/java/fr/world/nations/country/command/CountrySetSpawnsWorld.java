package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.Country;
import fr.world.nations.country.CountryManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class CountrySetSpawnsWorld extends FCommand {

    private final CountryManager countryManager;

    public CountrySetSpawnsWorld(CountryManager countryManager) {
        this.countryManager = countryManager;
        this.aliases.add("setspawnsworld");
        this.requiredArgs.add("name");
    }

    @Override
    public void perform(CommandContext commandContext) {
        String worldName = commandContext.argAsString(0);
        if (worldName == null || worldName.isEmpty()) {
            commandContext.msg("§cVeuillez préciser le nom du monde !");
            return;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            commandContext.msg("§cLe monde " + worldName + " n'existe pas !");
            return;
        }
        for (Country country : countryManager.getAllCountries()) {
            if (country.getSpawn() == null) return;
            Location spawn = country.getSpawn();
            country.setSpawn(new Location(world, spawn.getX(), spawn.getY(), spawn.getZ()));
        }
        commandContext.msg("Le monde de tous les spawns des pays a bien été (re)défini.");
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
