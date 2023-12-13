package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;

public class AssaultResetclaimcdCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultResetclaimcdCommand(AssaultCommand rootCmd) {
        aliases.add("resetattackcd");
        requiredArgs.add("faction");
        this.rootCmd = rootCmd;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("assault.op")) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        Faction attacker = commandContext.argAsFaction(0, commandContext.faction);
        if (attacker == null) return;
        boolean removed = rootCmd.getPlugin().removeClaimCoolDown(attacker);
        if (removed) {
            commandContext.sender.sendMessage("§4[Assaut] §eLa faction §6" + attacker.getTag() + " §e peut de nouveau claim !");
        } else {
            commandContext.sender.sendMessage("§4[Assaut] §cLa faction §6" + attacker.getTag() + " §c peut déjà claim !");
        }
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
