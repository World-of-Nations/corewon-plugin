package fr.world.nations.assault.cmd;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.assault.Assault;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AssaultListCommand extends FCommand {

    private final AssaultCommand rootCmd;

    public AssaultListCommand(AssaultCommand rootCmd) {
        aliases.add("list");
        this.requirements.permission = Permission.HELP;
        this.rootCmd = rootCmd;
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (rootCmd.getPlugin().getAssaultManager().getAssaults().isEmpty()) {
            commandContext.sender.sendMessage("§cAucun assault en cours");
            return;
        }

        StringBuilder builder = new StringBuilder("§6Liste des assauts en cours :\n \n");

        for (Assault assault : rootCmd.getPlugin().getAssaultManager().getAssaults()) {
            long timeMillis = System.currentTimeMillis() - assault.getAssaultStartedMillis();
            DateFormat format = new SimpleDateFormat("mm:ss");
            String time = format.format(new Date(timeMillis));

            String line = ChatColor.translateAlternateColorCodes('&', String.format("- %s (&c%d&6) &f/ &6(&c%d&6) %s  &c%s", assault.getAttacker().getTag(),
                    assault.getAttackerPoints(), assault.getDefendantPoints(),
                    assault.getDefendant().getTag(), time));

            builder.append("§6").append(line).append("\n");
        }
        commandContext.sender.sendMessage(builder.toString());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
