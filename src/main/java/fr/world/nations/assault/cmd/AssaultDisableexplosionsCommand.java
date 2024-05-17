package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.util.FactionUtil;

import java.util.ArrayList;
import java.util.List;

public class AssaultDisableexplosionsCommand extends FCommand {
    @Override
    public void perform(CommandContext commandContext) {
        aliases.add("disableexplosions");
        if (!commandContext.sender.hasPermission("assault.op")) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        List<String> affectedFactions = new ArrayList<>();
        for (Faction faction1 : FactionUtil.getAllPlayerFactions()) {
            //faction1.setExplosionsEnabled(false); //TODO : A FIX pour les explosions
            affectedFactions.add(faction1.getTag());
        }
        commandContext.sender.sendMessage("Explosions désactivées pour les factions : " + String.join(" ", affectedFactions));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
