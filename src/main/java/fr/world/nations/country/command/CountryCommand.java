package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.country.CountryManager;

import java.util.ArrayList;
import java.util.Arrays;

public class CountryCommand extends FCommand {
    public CountryCommand(CountryManager countryManager) {
        super();
        this.aliases.addAll(Arrays.asList("country", "c"));
        this.subCommands = new ArrayList<>();
        this.subCommands.add(new CountryAddCommand(countryManager));
        this.subCommands.add(new CountryPosCommand(countryManager));
        this.requirements = new CommandRequirements.Builder(Permission.ADMIN)
                .playerOnly()
                .build();
    }
    @Override
    public void perform(CommandContext commandContext) {
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
