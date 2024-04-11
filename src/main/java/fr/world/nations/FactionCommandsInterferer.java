package fr.world.nations;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.Aliases;
import fr.world.nations.country.CountryManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.awt.*;
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
        String cmdName = args[1].toLowerCase();
        //if (Aliases.show_show.contains(cmdName) || cmdName.equalsIgnoreCase("f")) {
        //    event.setCancelled(true);
            //TODO ouvrir interface
        //    return;
        //}
        if (toBlock.contains(cmdName)) {
            event.getPlayer().sendMessage("§cCette commande n'est pas disponible !");
            event.setCancelled(true);
        }

        if(Aliases.create.contains(cmdName)){
            if(args.length >= 3) {
                String countryName = args[2];

                //f create list <number>
                if(countryName.equalsIgnoreCase("list")){
                    event.setCancelled(true);
                    int number = 1;
                    if(args.length >= 4) {
                        Bukkit.getServer().getLogger().info("Creation list command by " + event.getPlayer().getName() +
                                ": /f " + cmdName + " " + args[2] + " " + args[3]);
                        number = Integer.parseInt(args[3]);
                    }
                    printList(number, event.getPlayer());
                }
                else if (!CountryManager.getInstance().getAvailableCountryNames().contains(countryName)){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("Ce pays n'est pas disponible.");
                    printList(1, event.getPlayer());
                }
            }
        }
    }

    //1 page = 10 pays
    private void printList(int page, Player player){
        Bukkit.getServer().getLogger().info("Printing creation list page " + page + " for " + player.getName());
        List<String> countryList = CountryManager.getInstance().getAvailableCountryNames();

        player.sendMessage("\n§6-= Liste des pays disponibles =- \n");
        for(int c=(page-1)*10; c<page*10; c++){
            String name = countryList.get(c);
            TextComponent country = new TextComponent("  §6● §e" + name + " [§a+§e]");
            country.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§6⇨ Créer le pays §e" + name).create()));
            country.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f create " + name));
            player.spigot().sendMessage(country);
        }
        int nextPageNumber = page+1;
        TextComponent nextPage = new TextComponent("   >> §6Page suivante [" + nextPageNumber + "] §f<<");
        nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f create list " + nextPageNumber));
        player.spigot().sendMessage(nextPage);
    }
}
