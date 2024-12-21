package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class AssaultGiveTokenCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultGiveTokenCommand(AssaultCommand rootCmd) {
        getAliases().add("givetoken");
        this.rootCmd = rootCmd;
        this.setRequirements(new CommandRequirements.Builder(Permission.ADMIN).build());
        this.getRequiredArgs().add("amount");
        this.getOptionalArgs().put("faction", "you");
    }

    @Override
    public void perform(CommandContext commandContext) {
        int amount = commandContext.argAsInt(0);
        Faction faction = commandContext.argAsFaction(1, commandContext.faction);
        rootCmd.getPlugin().getExplosionManager().giveToken(faction, amount);
        commandContext.msg(amount + " tokens ont été ajoutés à la faction " + faction.getTag());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
