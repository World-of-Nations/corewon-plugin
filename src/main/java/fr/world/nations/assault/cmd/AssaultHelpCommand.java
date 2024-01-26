package fr.world.nations.assault.cmd;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;

import java.util.ArrayList;
import java.util.List;

public class AssaultHelpCommand extends FCommand {

    public AssaultHelpCommand() {
        aliases.add("help");
    }

    @Override
    public void perform(CommandContext commandContext) {
        List<String> lines = new ArrayList<>();
        lines.add("§eUtilisation de la commande assault : ");
        if (commandContext.sender.hasPermission("assault")) {
            lines.add("§e/f §cassault");
            lines.add("§e/f §cassault start <faction>");
            lines.add("§e/f §cassault join <allié en assaut>");
            lines.add("§e/f §cassault accept <allié>");
            lines.add("§e/f §cassault list");
            if (commandContext.sender.hasPermission("assault.modo")) {
                lines.add("§e/f §cassault modo <faction>");
            }
            if (commandContext.sender.isOp()) {
                lines.add("§e/f §cassault stop <faction> [natural]");
                lines.add("§e/f §cassault resetattackcd <attaquant> <défenseur>");
                lines.add("§e/f §cassault resetclaimcd <faction>");
            }
        }
        if (!commandContext.sender.hasPermission("assault")) {
            lines.add("§4Vous n'avez pas la permission d'utiliser cette commande !");
        }
        String msg = String.join("\n", lines);
        commandContext.sender.sendMessage(msg);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
