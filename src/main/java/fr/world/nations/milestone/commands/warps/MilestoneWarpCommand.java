package fr.world.nations.milestone.commands.warps;

import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.zcore.util.TL;
import fr.world.nations.milestone.WonMilestone;
import fr.world.nations.milestone.warps.WarpManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MilestoneWarpCommand extends FCommand {

    private final WarpManager warpManager;

    public MilestoneWarpCommand(WonMilestone plugin) {
        this.warpManager = plugin.getWarpManager();
        aliases.add("warp");
        requiredArgs.add("name");
        optionalArgs.put("faction", "you");
//        this.setVisibilityMode(VisibilityMode.VISIBLE);
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.warps")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        Faction faction = commandContext.argAsFaction(1, commandContext.faction);
        if (faction == null) return;
        if (!(commandContext.sender instanceof Player)) {
            commandContext.sender.sendMessage("Vous ne pouvez pas effectuer cette commande depuis la console !");
            return;
        }
        int delta = warpManager.availableWarpsRemaining(faction);
        if (delta < 0) {
            commandContext.sender.sendMessage("§cVous n'avez plus de warp disponible ! Supprimez-en " + delta +
                    "pour réaccéder à cette commande !");
            return;
        }
        String warpName = commandContext.argAsString(0).toLowerCase();
        Location tp = warpManager.getWarp(faction, warpName);
        if (tp == null) {
            commandContext.sender.sendMessage("Vous n'avez pas de warp appelé " + warpName + " !");
            return;
        }
        perform(commandContext.sender, warpName, tp);
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }

    public void perform(CommandSender sender, String warpName, Location warp) {
        FPlayer uSender = FPlayers.getInstance().getByOfflinePlayer((Player) sender);
        Player player = (Player) sender;
        Faction uSenderFaction = uSender.getFaction();
        if (!Conf.homesEnabled) {
            uSender.msg("<b>Sorry, Faction homes are disabled on this server.");
        } else if (!Conf.homesTeleportCommandEnabled) {
            uSender.msg("<b>Sorry, the ability to teleport to Faction homes is disabled on this server.");
        } else if (!Conf.homesTeleportAllowedFromEnemyTerritory && uSender.isInEnemyTerritory()) {
            uSender.msg("<b>You cannot teleport to your faction warp while in the territory of an enemy faction.");
        } else if (!Conf.homesTeleportAllowedFromDifferentWorld && !player.getWorld().getName().equalsIgnoreCase(warp.getWorld().getName())) {
            uSender.msg("<b>You cannot teleport to your faction warp while in a different world.");
        } else {
            Faction faction = Board.getInstance().getFactionAt(FLocation.wrap(player));
            Location loc = player.getLocation().clone();
            if (Conf.homesTeleportAllowedEnemyDistance > 0.0 && !faction.noPvPInTerritory() && (!uSender.isInOwnTerritory() || uSender.isInOwnTerritory() && !Conf.homesTeleportIgnoreEnemiesIfInOwnTerritory)) {
                World w = loc.getWorld();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();

                for (Player p : player.getServer().getOnlinePlayers()) {
                    if (p != null && p.isOnline() && !p.isDead() && p != player && p.getWorld() == w) {
                        FPlayer fp = FPlayers.getInstance().getByPlayer(p);
                        if (uSender.getRelationTo(fp) == Relation.ENEMY) {
                            Location l = p.getLocation();
                            double dx = Math.abs(x - l.getX());
                            double dy = Math.abs(y - l.getY());
                            double dz = Math.abs(z - l.getZ());
                            double max = Conf.homesTeleportAllowedEnemyDistance;
                            if (!(dx > max) && !(dy > max) && !(dz > max)) {
                                uSender.msg("<b>You cannot teleport to your faction warp while an enemy is within " + Conf.homesTeleportAllowedEnemyDistance + " blocks of you.");
                                return;
                            }
                        }
                    }
                }
            }
            player.sendMessage("Téléportation au warp " + warpName + "...");
            player.teleport(warp);
        }
    }
}
