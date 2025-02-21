package fr.world.nations.util;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FactionUtil {

    public static Faction getFaction(String name) {
        if (name == null) return null;
        Faction faction = Factions.getInstance().getByTag(name);
        if (!isPlayerFaction(faction)) return null;
        return faction;
    }

    @NonNull
    public static Map.Entry<Faction, Integer> getFaction(CommandSender sender, List<String> args) {
        Faction faction;
        if (args.size() >= 2 && sender.hasPermission("fac-milestone.warps.op")) {
            faction = getFaction(args.get(0));
            if (!isPlayerFaction(faction)) {
                sender.sendMessage("La faction " + args.get(0) + " n'existe pas !");
                return new AbstractMap.SimpleEntry<>(null, -1);
            }
            return new AbstractMap.SimpleEntry<>(faction, 1);
        }
        if (sender instanceof Player) {
            FPlayer uPlayer = FPlayers.getInstance().getByPlayer((Player) sender);
            faction = uPlayer.getFaction();
            if (!isPlayerFaction(faction)) {
                sender.sendMessage("Vous n'êtes dans aucune faction !");
                return new AbstractMap.SimpleEntry<>(null, -1);
            }
            return new AbstractMap.SimpleEntry<>(faction, 0);
        }

        sender.sendMessage("La faction n'a pas pu être trouvée !");
        return new AbstractMap.SimpleEntry<>(null, -1);
    }

    public static Faction getFaction(CommandSender sender) {
        if (sender == null) return null;
        if (!(sender instanceof Player)) return null;
        return getFaction((Player) sender);
    }

    public static Faction getFaction(Player player) {
        //System.out.println("Getting " + player + "'s faction...");
        Faction wilderness = Factions.getInstance().getWilderness();
        if (player == null) return wilderness;
        FPlayer uPlayer = FPlayers.getInstance().getByPlayer(player);
        if (uPlayer == null) return wilderness;
        Faction faction = uPlayer.getFaction();
        if (faction == null) return wilderness;
        //System.out.println("Found " + faction.getName());
        return faction;
    }

    public static boolean isPlayerFaction(Faction faction) {
        if (faction == null) return false;
        return !faction.isWilderness()
                && !faction.isWarZone()
                && !faction.isSafeZone();
    }

    public static List<Faction> getAllPlayerFactions() {
        return Factions.getInstance().getAllFactions().stream().filter(FactionUtil::isPlayerFaction).collect(Collectors.toList());
    }
}
