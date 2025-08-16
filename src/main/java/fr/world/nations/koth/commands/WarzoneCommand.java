package fr.world.nations.koth.commands;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.world.nations.Core;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.handlers.KothHandler;
import fr.world.nations.koth.inventory.WarzoneInventory;
import fr.world.nations.koth.managers.PowerManager;
import fr.world.nations.koth.models.KothModel;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WarzoneCommand implements CommandExecutor {

    private final WonKoth plugin;

    public WarzoneCommand(WonKoth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender.hasPermission("koth-won.warzone")) {

            // Open GUI
            if (args.length == 0) {
                if (sender instanceof Player) {
                    new WarzoneInventory().open((Player) sender);
                }
                return true;
            }

            // /warzone stats
            if (args.length == 1 && args[0].equalsIgnoreCase("stats")) {
                if (sender instanceof Player) {
                    Bukkit.getScheduler().runTask(Core.getInstance(), () -> {
                        List<FactionData> factionDatas = Core.getInstance().getModuleManager().getModule(WonStats.class)
                                .getStatsManager().getFactionDatas();
                        factionDatas.sort(Collections.reverseOrder(Comparator.comparingDouble(FactionData::getScoreZone)));

                        FPlayer uplayer = FPlayers.getInstance().getByPlayer((Player) sender);
                        String playerFactionName = uplayer.getFaction().getTag();
                        boolean topContainPlayerFaction = false;

                        sender.sendMessage("§c-= §6Classement Warzone §c=-");
                        int i = 0;

                        for (FactionData factionData : factionDatas) {
                            int pos = i + 1;

                            if (pos >= 11) break;

                            Faction faction = factionData.getFaction();
                            if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) continue;

                            if (faction == uplayer.getFaction()) topContainPlayerFaction = true;

                            String points = new DecimalFormat("###.##").format(factionData.getScoreZone());

                            if (pos == 1)
                                sender.sendMessage("§b" + pos + ". " + faction.getTag() + " " + points + " Points");
                            else if (pos == 2)
                                sender.sendMessage("§e" + pos + ". " + faction.getTag() + " " + points + " Points");
                            else if (pos == 3)
                                sender.sendMessage("§7" + pos + ". " + faction.getTag() + " " + points + " Points");
                            else
                                sender.sendMessage("§f" + pos + ". " + faction.getTag() + " " + points + " Points");

                            i++;
                        }

                        if (!topContainPlayerFaction && uplayer.hasFaction()) {
                            int j = 1;
                            for (FactionData factionData : factionDatas) {
                                if (factionData.getFaction().getTag().equalsIgnoreCase(playerFactionName)) {
                                    sender.sendMessage("§c" + j + ". " + factionData.getFaction().getTag() + " " + factionData.getScoreZone() + " Points");
                                    return;
                                }
                                j++;
                            }
                        }
                    });
                }
                return true;
            }
        }

        // Nouvelle commande accessible aux joueurs : /warzone tp <nom>
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande !");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("koth-won.warzone.tp")) {
                player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            KothModel kothModel = plugin.getKothManager().getKothFromCache(args[1]);
            if (kothModel == null) {
                player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth")
                        .replace("%area_name%", args[1]));
                return true;
            }

            if (kothModel.getTeleportPoint() == null) {
                player.sendMessage("§cAucun point de téléportation n'a été défini pour cette warzone !");
                return true;
            }

            boolean hasArmor = false;
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    hasArmor = true;
                    break;
                }
            }

            if (!hasArmor) {
                for (String msg : plugin.getDefaultConfig().getStringList("messages.players.tp-warzone-noarmor")) {
                    player.sendMessage(msg);
                }
                return true;
            }

            player.teleport(kothModel.getTeleportPoint());
            player.sendMessage("§aTéléporté à la warzone §e" + kothModel.getKothName() + "§a !");
            return true;
        }

        // Commandes réservées aux administrateurs
        if (sender.hasPermission("koth-won.warzone.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    sendHelpMessage(sender);
                    return true;
                }

                if (args[0].equalsIgnoreCase("list")) {
                    for (KothModel model : plugin.getKothManager().getKothModelList()) {
                        sender.sendMessage(model.getKothName());
                    }
                    return true;
                }
            }

            if (args.length == 2) {
                if (!(sender instanceof Player player)) return true;

                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        KothHandler.createKothFromRegion(args[1], player);
                        player.sendMessage("Une warzone a été créée à votre emplacement ! (" + args[1] + ")");
                        return true;
                    }

                    case "settp" -> {
                        KothModel model = plugin.getKothManager().getKothFromCache(args[1]);
                        if (model == null) {
                            player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth")
                                    .replace("%area_name%", args[1]));
                            return false;
                        }
                        model.setTeleportPoint(player.getLocation());
                        plugin.getKothManager().saveKoth(model, false);
                        player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.koth-spawn-set")
                                .replace("%area_name%", args[1]));
                        return true;
                    }

                    case "start" -> {
                        KothModel model = plugin.getKothManager().getKothFromCache(args[1]);
                        if (model == null) {
                            player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth")
                                    .replace("%area_name%", args[1]));
                            return false;
                        }
                        if (model.isStarted()) {
                            player.sendMessage("§cKoth already started");
                            return false;
                        }
                        model.start();
                        player.sendMessage("§aKoth started");
                        return true;
                    }

                    case "stop" -> {
                        KothModel model = plugin.getKothManager().getKothFromCache(args[1]);
                        if (model == null) {
                            player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth")
                                    .replace("%area_name%", args[1]));
                            return false;
                        }
                        if (!model.isStarted()) {
                            player.sendMessage("§cKoth is not started");
                            return false;
                        }
                        model.stop();
                        player.sendMessage("§aKoth stopped");
                        return true;
                    }

                    case "delete" -> {
                        KothModel model = plugin.getKothManager().getKothFromCache(args[1]);
                        if (model == null) {
                            player.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth")
                                    .replace("%area_name%", args[1]));
                            return false;
                        }
                        KothHandler.deleteKothFromName(args[1], sender);
                        return true;
                    }

                    case "resettimer" -> {
                        Faction faction = getFaction(args[1]);
                        if (faction == null) {
                            sender.sendMessage("§cCette faction n'existe pas !");
                            return true;
                        }
                        long result = PowerManager.getInstance().resetTimer(faction);
                        if (result < 0) {
                            player.sendMessage("§cLa faction " + args[1] + " n'a jamais reçu de récompense koth !");
                            return true;
                        }
                        player.sendMessage("La faction " + args[1] + " reçoit à nouveau le maximum de power par récompense !");
                        return true;
                    }

                    case "deletetimer" -> {
                        Faction faction = getFaction(args[1]);
                        if (faction == null) {
                            sender.sendMessage("§cCette faction n'existe pas !");
                            return true;
                        }
                        PowerManager.getInstance().deleteTimer(faction);
                        player.sendMessage("Le timer de la faction " + args[1] + " recommencera dès sa prochaine récompense KOTH !");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Faction getFaction(String name) {
        return Factions.getInstance().getByTag(name);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§c/warzone help - show this message");
        sender.sendMessage("§c/warzone start <kothName> - Start a KOTH");
        sender.sendMessage("§c/warzone stop <kothName> - Stop a KOTH");
        sender.sendMessage("§c/warzone create <kothName> - Create a new KOTH");
        sender.sendMessage("§c/warzone delete <kothName> - Delete a KOTH");
        sender.sendMessage("§c/warzone settp <kothName> - Set teleport point");
        sender.sendMessage("§c/warzone tp <kothName> - Teleport to a KOTH");
        sender.sendMessage("§c/warzone stats - Show warzone ranking");
    }
}
