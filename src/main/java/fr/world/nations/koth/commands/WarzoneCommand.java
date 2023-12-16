package fr.world.nations.koth.commands;

/*
 *  * @Created on 05/05/2022
 *  * @Project AresiaMoneyTime
 *  * @Author Jimmy  / SKAH#7513
 */

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class WarzoneCommand implements CommandExecutor {

    private final WonKoth plugin;

    public WarzoneCommand(WonKoth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("koth-won.warzone")) {

            //Open gui
            if (args.length == 0) {
                if (sender instanceof Player) {
                    new WarzoneInventory().open((Player) sender);
                }
                return true;
            }
            //Stats command
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("stats")) {
                    if (sender instanceof Player) {

                        Bukkit.getScheduler().runTask(Core.getInstance(), () -> {
                            List<FactionData> factionDatas = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager().getFactionDatas();
                            factionDatas.sort(Collections.reverseOrder(Comparator.comparingDouble(FactionData::getScoreZone)));
                            FPlayer uplayer = FPlayers.getInstance().getByPlayer((Player) sender);
                            String playerFactionName = uplayer.getFaction().getTag();
                            boolean topContainPlayerFaction = false;
                            sender.sendMessage("§c-= §6Classement Warzone §c=-");

                            int i = 0;
                            for (FactionData factionData : factionDatas) {
                                int pos = i + 1;

                                if (pos >= 11) {
                                    System.out.println("Pos breaked");
                                    break;
                                }

                                Factions factions = Factions.getInstance();

                                if (factionData.getFaction() == factions.getWilderness()
                                        || factionData.getFaction() == factions.getSafeZone()
                                        || factionData.getFaction() == factions.getWarZone()) {
                                    continue;
                                }

                                if (factionData.getFaction() == uplayer.getFaction())
                                    topContainPlayerFaction = true;

                                String points = new DecimalFormat("###.##").format(factionData.getScoreZone());

                                if (pos == 1)
                                    sender.sendMessage("§b" + pos + ". " + factionData.getFaction().getTag() + ' ' + points + " Points");
                                else if (pos == 2)
                                    sender.sendMessage("§e" + pos + ". " + factionData.getFaction().getTag() + ' ' + points + " Points");
                                else if (pos == 3)
                                    sender.sendMessage("§7" + pos + ". " + factionData.getFaction().getTag() + ' ' + points + " Points");
                                else
                                    sender.sendMessage("§f" + pos + ". " + factionData.getFaction().getTag() + ' ' + points + " Points");
                                i = ++i;
                            }

                            int j = 1;
                            if (!topContainPlayerFaction && uplayer.hasFaction()) {
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
                }
            }

        }

        if (sender.hasPermission("koth-won.warzone.admin")) {

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("help")) {
                    sendHelpMessage(sender);
                    return true;
                }
                if (args[0].equalsIgnoreCase("list")) {
                    for (KothModel model : plugin.getKothManager().getKothModelList())
                        sender.sendMessage(model.getKothName());
                    return true;
                }
            }

            if (args.length == 2) {

                if (sender instanceof Player player) {

                    if (args[0].equalsIgnoreCase("create")) {
                        KothHandler.createKothFromRegion(args[1], player);
                        player.sendMessage("Une warzone a été créée à votre emplacement ! (" + args[1] + ")");
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("settp")) {

                        KothModel kothModel = plugin.getKothManager().getKothFromCache(args[1]);
                        if (kothModel == null) {
                            sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth").replace("%area_name%", args[1]));
                            return false;
                        }
                        kothModel.setTeleportPoint(player.getLocation());
                        plugin.getKothManager().saveKoth(kothModel, false);
                        sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.koth-spawn-set").replace("%area_name%", args[1]));

                        return true;
                    }

                    if (args[0].equalsIgnoreCase("start")) {

                        KothModel kothModel = plugin.getKothManager().getKothFromCache(args[1]);
                        if (kothModel == null) {
                            sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth").replace("%area_name%", args[1]));
                            return false;
                        }
                        if (kothModel.isStarted()) {
                            sender.sendMessage("§cKoth already started");
                            return false;
                        }

                        kothModel.start();
                        sender.sendMessage("§aKoth started");

                        return true;
                    }

                    if (args[0].equalsIgnoreCase("resettimer")) {
                        Faction faction = getFaction(args[1]);
                        if (faction == null) {
                            sender.sendMessage("§cCette faction n'existe pas !");
                            return true;
                        }
                        long result = PowerManager.getInstance().resetTimer(faction);
                        if (result < 0) {
//                            BaseComponent component = new TextComponent("§cLa faction " + args[1] + " n'a jamais reçu de récompense koth !");
//                            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "warzone settiming " + args[1] + " " + System.currentTimeMillis());
//                            BaseComponent component1 = new TextComponent("§e[Lancer tout de même le décompte maintenant]");
//                            component1.setClickEvent(clickEvent);
//                            component.addExtra(component1);
//                            player.spigot().sendMessage(component);
                            player.sendMessage("§cLa faction " + args[1] + " n'a jamais reçu de récompense koth !");
                            return true;
                        }
//                        BaseComponent component = new TextComponent("La faction " + args[1] + " reçoit à nouveau le maximum de power par récompense ! ");
//                        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "warzone settiming " + args[1] + " " + result);
//                        BaseComponent component1 = new TextComponent("§c[Annuler]");
//                        component1.setClickEvent(clickEvent);
//                        component.addExtra(component1);
//                        player.spigot().sendMessage(component);
                        player.sendMessage("La faction " + args[1] + " reçoit à nouveau le maximum de power par récompense ! ");
                    }

                    if (args[0].equalsIgnoreCase("deletetimer")) {
                        Faction faction = getFaction(args[1]);
                        if (faction == null) {
                            sender.sendMessage("§cCette faction n'existe pas !");
                            return true;
                        }
                        long result = PowerManager.getInstance().deleteTimer(faction);
//                        BaseComponent component = new TextComponent("Le timer de la faction " + args[1] + " recommencera dès que celle-ci recevra sa prochaine récompense KOTH ! ");
//                        if (result > 0) {
//                            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "warzone settiming " + args[1] + " " + result);
//                            BaseComponent component1 = new TextComponent("§c[Annuler]");
//                            component1.setClickEvent(clickEvent);
//                            component.addExtra(component1);
//                        }
//                        player.spigot().sendMessage(component);
                        player.sendMessage("Le timer de la faction " + args[1] + " recommencera dès que celle-ci recevra sa prochaine récompense KOTH ! ");
                        return true;
                    }

                }

                if (args[0].equalsIgnoreCase("stop")) {

                    KothModel kothModel = plugin.getKothManager().getKothFromCache(args[1]);
                    if (kothModel == null) {
                        sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth").replace("%area_name%", args[1]));
                        return false;
                    }
                    if (!kothModel.isStarted()) {
                        sender.sendMessage("§cKoth is not started");
                        return false;
                    }

                    kothModel.stop();
                    sender.sendMessage("§aKoth stopped");

                    return true;
                }

                if (args[0].equalsIgnoreCase("delete")) {
                    KothModel kothModel = plugin.getKothManager().getKothFromCache(args[1]);
                    if (kothModel == null) {
                        sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth").replace("%area_name%", args[1]));
                        return false;
                    }
                    KothHandler.deleteKothFromName(args[1], sender);
                    return true;
                }


            }

            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("settimer")) {
                    Faction faction = getFaction(args[1]);
                    if (faction == null) {
                        sender.sendMessage("§cCette faction n'existe pas !");
                        return true;
                    }
                    long specifiedDate;
                    try {
                        specifiedDate = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        DateFormat format = new SimpleDateFormat("dd-MM-yyyy-hh:mm:ss");
                        try {
                            Date date = format.parse(args[2]);
                            specifiedDate = date.getTime();
                        } catch (ParseException ex) {
                            sender.sendMessage("§cL'argument 2 est incorrect ! Il doit être une date, en millisecondes ou selon le pattern : dd-MM-yyyy-hh:mm:ss");
                            return true;
                        }
                    }
                    long result = PowerManager.getInstance().setTimer(faction, specifiedDate);
                    Date resultDate = new Date(specifiedDate);
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy à hh:mm:ss");
                    String format = dateFormat.format(resultDate);
                    String fb = "Le timer de la faction " + args[1] + " commence désormais à l'instant : " + format + " ! ";
//                    if (sender instanceof Player) {
//                        Player player = (Player) sender;
//                        BaseComponent component = new TextComponent(fb);
//                        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "warzone settiming " + args[1] + " " + result);
//                        BaseComponent component1 = new TextComponent("§c[Annuler]");
//                        component1.setClickEvent(clickEvent);
//                        component.addExtra(component1);
//                        player.spigot().sendMessage(component);
//                        return true;
//                    }
                    sender.sendMessage(fb);
                }
            }

            if (args.length == 5) {
                if (args[0].equalsIgnoreCase("setreward")) {

                    KothModel kothModel = plugin.getKothManager().getKothFromCache(args[4]);
                    if (kothModel == null) {
                        sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.no-koth").replace("%area_name%", args[4]));
                        return false;
                    }

                    int amount = 0;
                    int second = 0;

                    try {
                        second = Integer.parseInt(args[2]);
                        amount = Integer.parseInt(args[3]);

                    } catch (NumberFormatException ignored) {
                        sender.sendMessage("§cAmount/Second Number error");
                        return false;
                    }

                    kothModel.setRewardType(args[1]);
                    kothModel.setRewardAmount(amount);
                    kothModel.setRewardTime(second);
                    sender.sendMessage(plugin.getDefaultConfig().getString("messages.admins.koth-reward-set")
                            .replace("%area_name%", args[4]));
                    plugin.getKothManager().saveKoth(kothModel, false);
                }
            }
            return false;
        }
        return false;
    }

    private Faction getFaction(String name) {
        return Factions.getInstance().getByTag(name);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§c/warzone help - show this message");

        sender.sendMessage("§c/warzone start <kothName> - create a new koth");
        sender.sendMessage("§c/warzone stop <kothName> - create a new koth");


        sender.sendMessage("§c/warzone create <kothName> - create a new koth");
        sender.sendMessage("§c/warzone delete <kothName> - delete a koth");
        sender.sendMessage("§c/warzone settp <kothName> - Set telport point of specified Koth");

        sender.sendMessage("§c/warzone setreward <money/power> <seconds> <amount> <kothName> - Set a reward");


    }
}
