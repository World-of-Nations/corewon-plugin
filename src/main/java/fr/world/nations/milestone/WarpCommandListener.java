package fr.world.nations.milestone;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.FCmdRoot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class WarpCommandListener implements Listener {

    private final WonMilestone wonMilestone;

    public WarpCommandListener(WonMilestone wonMilestone) {
        this.wonMilestone = wonMilestone;
    }

    @EventHandler
    public void onWarpCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (!Conf.baseCommandAliases.contains(args[0].toLowerCase())) return;
        if (!FCmdRoot.instance.cmdFWarp.aliases.contains(args[1].toLowerCase())) return;
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(event.getPlayer());
        if (fPlayer.isAdminBypassing()) return;
        if (!fPlayer.hasFaction()) return;
        Faction faction = fPlayer.getFaction();
        int currentMilestone = wonMilestone.getMilestoneData(faction).getMilestone();
        int maxWarps = wonMilestone.getDefaultConfig().getInt("warps.limit." + currentMilestone);
        int delta = faction.getWarps().size() - maxWarps;
        if (delta <= 0) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cLe pays §6" + faction.getTag() + "§ca plus de warp qu'autorisé ! Veuillez en supprimer au moins " + delta);
    }
}
