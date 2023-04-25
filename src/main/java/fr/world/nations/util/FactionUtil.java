package fr.world.nations.util;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.milestone.MilestoneCalculator;
import fr.world.nations.milestone.WonMilestone;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FactionUtil {

    private static final WonMilestone plugin = WonMilestone.getInstance();

    public static void sendFactionInfo(CommandSender sender, Faction faction) {
        MilestoneCalculator data = plugin.getMilestoneData(faction);
        String msg = "\n§cInformations sur la faction §6" + faction.getTag() + "§c\n";
        msg += "§4Palier : §6" + data.getMilestone() + "\n";
        msg += "§4Avancement : §6" + data.getProgressXp() + "§c/§6" + data.getNextMilestoneXp() + "\n";

        Map<String, Double> bonuses = plugin.getBonuses(faction);

        if (bonuses.isEmpty()) msg += "§4Bonus : §6aucun\n";
        else {
            StringBuilder toAdd = new StringBuilder("§4Bonus : \n");
            for (String key : bonuses.keySet()) {
                toAdd.append("§c - ").append(key).append(" : §6").append(bonuses.get(key)).append("\n");
            }
            msg += toAdd.toString();
        }

        sender.sendMessage(msg);
    }

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
            if (!isPlayerFaction(faction)) { //TODO a tester
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
