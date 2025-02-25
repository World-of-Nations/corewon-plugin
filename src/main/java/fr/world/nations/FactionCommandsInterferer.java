package fr.world.nations;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.cmd.Aliases;
import com.massivecraft.factions.event.FactionEvent;
import fr.world.nations.assault.AssaultManager;
import fr.world.nations.assault.WonAssault;
import fr.world.nations.country.CountryManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FactionCommandsInterferer implements Listener {

    private final List<String> toBlock = new ArrayList<>();

    public FactionCommandsInterferer() {
        toBlock.addAll(Aliases.weewoo);
        toBlock.addAll(Aliases.corner_list);
        toBlock.addAll(Aliases.logout);
        toBlock.addAll(Aliases.rally);
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
        toBlock.addAll(Aliases.vault);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (FPlayers.getInstance().getByPlayer(player).isAdminBypassing()) return;
        String msg = event.getMessage().substring(1);
        String[] args = msg.split(" ");
        if (!(args[0].equalsIgnoreCase("f") || args[0].equalsIgnoreCase("faction"))) return;
        if (args.length == 1) return;
        String cmdName = args[1].toLowerCase();

        //FOR INTERFACES:
        //if (Aliases.show_show.contains(cmdName) || cmdName.equalsIgnoreCase("f")) {
        //    event.setCancelled(true);
        //TODO ouvrir interface
        //    return;
        //}

        if (toBlock.contains(cmdName)) {
            player.sendMessage("§4[§cErreur§4] §cCette commande a été désactivée !");
            event.setCancelled(true);
        } else if (Aliases.home.contains(cmdName)) {
            if (player.hasPermission("faction.home.bypass-cooldown")) {
                event.setCancelled(true);
                Location home = FPlayers.getInstance().getByPlayer(player).getFaction().getHome();
                player.teleport(home);
                player.sendMessage("§eYou have been teleported to §aFaction home§e.");
            }
        } else if (Aliases.create.contains(cmdName)) {
            if (args.length >= 3) {
                String countryName = args[2];

                //f create list <number>
                if (countryName.equalsIgnoreCase("list")) {
                    event.setCancelled(true);
                    int number = 1;
                    if (args.length >= 4)
                        number = Integer.parseInt(args[3]);
                    printList(number, player);
                } else if (!CountryManager.getInstance().getAvailableCountryNames().contains(countryName)) {
                    event.setCancelled(true);
                    player.sendMessage("§4[§cPays§4] §cCe pays n'est pas disponible.");
                    printList(1, player);
                }
            } else printList(1, player);
        }
    }

    //1 page = 10 pays
    private void printList(int page, Player player) {
        Bukkit.getServer().getLogger().info("Printing creation list page " + page + " for " + player.getName());
        List<String> countryList = CountryManager.getInstance().getAvailableCountryNames();

        Object[] list = countryList.toArray();
        Arrays.sort(list);

        player.sendMessage("\n§6-= Liste des pays disponibles =- \n");
        for (int c = (page - 1) * 10; c < page * 10; c++) {
            String name = (String) list[c];
            TextComponent country = new TextComponent("  §6● §e" + name + " [§a+§e]");
            country.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§6⇨ Créer le pays §e" + name).create()));
            country.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f create " + name));
            player.spigot().sendMessage(country);
        }
        int nextPageNumber = page + 1;
        TextComponent nextPage = new TextComponent("   >> §6Page suivante [" + nextPageNumber + "] §f<<");
        nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f create list " + nextPageNumber));
        player.spigot().sendMessage(nextPage);
    }
}
