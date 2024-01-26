package fr.world.nations.assault.cmd;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.assault.WonAssault;

import java.util.HashMap;

public class AssaultCommand extends FCommand {

    private final WonAssault plugin;
    private final HashMap<Faction, Faction> joinRequests; //Demandant - Demandé

    public AssaultCommand(WonAssault plugin) {
        aliases.add("assault");
        this.plugin = plugin;
        this.joinRequests = new HashMap<>();
        this.requirements.playerOnly = true;
        this.addSubCommand(new AssaultAcceptCommand(this));
        this.addSubCommand(new AssaultDisableexplosionsCommand());
        this.addSubCommand(new AssaultHelpCommand());
        this.addSubCommand(new AssaultJoinCommand(this));
        this.addSubCommand(new AssaultListCommand(this));
        this.addSubCommand(new AssaultModoCommand(this));
        this.addSubCommand(new AssaultResetattackcdCommand(this));
        this.addSubCommand(new AssaultResetclaimcdCommand(this));
        this.addSubCommand(new AssaultStartCommand(this));
        this.addSubCommand(new AssaultStopCommand(this));
        this.addSubCommand(new AssaultTokensCommand(this));
        this.addSubCommand(new AssaultGiveTokenCommand(this));
//        this.addSubCommand(new AssaultDebugCmd());
    }

    public HashMap<Faction, Faction> getJoinRequests() {
        return joinRequests;
    }

    public WonAssault getPlugin() {
        return plugin;
    }

    @Override
    public void perform(CommandContext commandContext) {
        commandContext.commandChain.add(this);
        FactionsPlugin.getInstance().cmdAutoHelp.execute(commandContext);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }
}
