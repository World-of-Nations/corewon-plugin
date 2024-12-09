package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;

public class AssaultTokensCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultTokensCommand(AssaultCommand rootCmd) {
        aliases.add("tokens");
        this.rootCmd = rootCmd;
        this.requirements.permission = Permission.HELP;
        this.optionalArgs.put("faction", "you");
    }

    @Override
    public void perform(CommandContext commandContext) {
        Faction faction = commandContext.argAsFaction(0, commandContext.faction);
        if (faction == null) return;
        if (!commandContext.sender.hasPermission("assault.modo") && faction != commandContext.faction) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        int tokenAmount = rootCmd.getPlugin().getExplosionManager().getTokenAmount(faction);
        commandContext.sender.sendMessage("Nombre de tokens de la faction " + faction.getTag() + " : " + tokenAmount);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
