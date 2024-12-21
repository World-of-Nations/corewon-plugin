package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class AssaultResetattackcdCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultResetattackcdCommand(AssaultCommand rootCmd) {
        getAliases().add("resetattackcd");
        getRequiredArgs().add("attackerFaction");
        getRequiredArgs().add("defenderFaction");
        this.setRequirements(new CommandRequirements.Builder(Permission.HELP).build());
        this.rootCmd = rootCmd;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("assault.op")) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        Faction attacker = commandContext.argAsFaction(0);
        if (attacker == null) return;
        Faction defender = commandContext.argAsFaction(1);
        if (defender == null) return;
        boolean removed = rootCmd.getPlugin().removeAttackCoolDown(attacker, defender);
        if (removed) {
            commandContext.sender.sendMessage("§4[Assaut] §eLa faction §6" + attacker.getTag() + "§e peut de nouveau attaquer la faction §6" + defender.getTag() + "§e !");
        } else {
            commandContext.sender.sendMessage("§4[Assaut] §cLa faction §6" + attacker.getTag() + "§c peut déjà attaquer la faction §6" + defender.getTag() + "§c !");
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
