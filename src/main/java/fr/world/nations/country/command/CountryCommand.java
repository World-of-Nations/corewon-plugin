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
        this.getAliases().addAll(Arrays.asList("country", "co"));
        this.setRequirements(new CommandRequirements.Builder(Permission.ADMIN)
                .playerOnly()
                .brigadier(CountryBrigadier.class)
                .build());
        addSubCommand(new CountryAddCommand(countryManager));
        addSubCommand(new CountryPosCommand(countryManager));
        addSubCommand(new CountryRemoveCommand(countryManager));
        addSubCommand(new CountrySetidCommand(countryManager));
        addSubCommand(new CountrySetSpawnsWorld(countryManager));

    }

    @Override
    public void perform(CommandContext commandContext) {
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }

    public static class CountryBrigadier implements BrigadierProvider {
        public CountryBrigadier() {
        }

        public ArgumentBuilder<Object, ?> get(ArgumentBuilder<Object, ?> parent) {
            return parent
                    .then(LiteralArgumentBuilder.literal("add"))
                    .then(LiteralArgumentBuilder.literal("pos"))
                    .then(LiteralArgumentBuilder.literal("remove"))
                    .then(LiteralArgumentBuilder.literal("setid"));
        }
    }
}
