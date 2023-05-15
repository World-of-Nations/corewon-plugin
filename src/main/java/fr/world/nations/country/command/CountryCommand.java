package fr.world.nations.country.command;

import com.massivecraft.factions.cmd.BrigadierProvider;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.world.nations.country.CountryManager;

import java.util.Arrays;

public class CountryCommand extends FCommand {
    public CountryCommand(CountryManager countryManager) {
        super();
        this.aliases.addAll(Arrays.asList("country", "co"));
        addSubCommand(new CountryAddCommand(countryManager));
        addSubCommand(new CountryPosCommand(countryManager));
        this.requirements = new CommandRequirements.Builder(Permission.ADMIN)
                .playerOnly()
                .brigadier(CountryBrigadier.class)
                .build();
    }
    @Override
    public void perform(CommandContext commandContext) {
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }

    public static class CountryBrigadier implements BrigadierProvider {
        public CountryBrigadier() {
        }

        public ArgumentBuilder<Object, ?> get(ArgumentBuilder<Object, ?> parent) {
            return parent.then(LiteralArgumentBuilder.literal("add")).then(LiteralArgumentBuilder.literal("pos"));
        }
    }
}
