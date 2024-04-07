package fr.world.nations;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.Aliases;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class FactionCommandsInterferer implements Listener {

    private final List<String> toBlock = new ArrayList<>();

    public FactionCommandsInterferer() {
        toBlock.addAll(Aliases.alts_alts);
        toBlock.addAll(Aliases.alts_list);
        toBlock.addAll(Aliases.weewoo);
        toBlock.addAll(Aliases.corner_list);
        toBlock.addAll(Aliases.logout);
        toBlock.addAll(Aliases.rally);
        toBlock.addAll(Aliases.points_balance);
        toBlock.addAll(Aliases.points_points);
        toBlock.addAll(Aliases.points_add);
        toBlock.addAll(Aliases.points_remove);
        toBlock.addAll(Aliases.points_set);
        toBlock.addAll(Aliases.tnt_tnt);
        toBlock.addAll(Aliases.boosters);
        toBlock.addAll(Aliases.giveBooster);
        toBlock.addAll(Aliases.tnt_tntfill);
        toBlock.addAll(Aliases.wild);
        toBlock.addAll(Aliases.banner);
        toBlock.addAll(Aliases.boom);
        toBlock.addAll(Aliases.roster);
        toBlock.addAll(Aliases.drain);
        toBlock.addAll(Aliases.focus);
        toBlock.addAll(Aliases.fly);
        toBlock.addAll(Aliases.killholograms);
        toBlock.addAll(Aliases.getvault);
        toBlock.addAll(Aliases.scoreboard);
        toBlock.addAll(Aliases.paypal_see);
        toBlock.addAll(Aliases.paypal_set);
        toBlock.addAll(Aliases.spawnerChunks);
        toBlock.addAll(Aliases.setTnt);
        toBlock.addAll(Aliases.scoreboard);
        toBlock.addAll(Aliases.setBanner);
        toBlock.addAll(Aliases.spawnerlock);
        toBlock.addAll(Aliases.stealth);
        toBlock.addAll(Aliases.strikes_strikes);
        toBlock.addAll(Aliases.strikes_give);
        toBlock.addAll(Aliases.strikes_info);
        toBlock.addAll(Aliases.strikes_set);
        toBlock.addAll(Aliases.strikes_take);
        toBlock.addAll(Aliases.stuck);
        toBlock.addAll(Aliases.tpBanner);
        toBlock.addAll(Aliases.upgrades);
        toBlock.addAll(Aliases.vault);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (FPlayers.getInstance().getByPlayer(event.getPlayer()).isAdminBypassing()) return;
        String msg = event.getMessage().substring(1);
        String[] args = msg.split(" ");
        //if (!args[0].equalsIgnoreCase("f")) return;
        //String cmdName = args[1].toLowerCase();
        //if (Aliases.show_show.contains(cmdName) || cmdName.equalsIgnoreCase("f")) {
        //    event.setCancelled(true);
            //TODO ouvrir interface
        //    return;
        //}
        if (toBlock.contains(cmdName)) {
            event.getPlayer().sendMessage("§cCette commande n'est pas disponible !");
            event.setCancelled(true);
        }
    }
}
