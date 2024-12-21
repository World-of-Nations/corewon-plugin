package fr.world.nations.milestone.commands;

import com.google.common.collect.Lists;
import com.massivecraft.factions.*;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.util.SpiralTask;
import com.massivecraft.factions.zcore.fperms.Access;
import com.massivecraft.factions.zcore.fperms.PermissableAction;
import com.massivecraft.factions.zcore.util.TL;
import com.massivecraft.factions.zcore.util.TextUtil;
import fr.world.nations.Core;
import fr.world.nations.milestone.MilestoneAccess;
import fr.world.nations.milestone.MilestoneCalculator;
import fr.world.nations.milestone.WonMilestone;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MilestoneExpandCommand extends FCommand {

    private final WonMilestone plugin;

    public MilestoneExpandCommand(WonMilestone plugin) {
        this.plugin = plugin;
        this.setRequirements(new CommandRequirements.Builder(Permission.HELP).build());
        getAliases().add("expand");
        getOptionalArgs().put("radius", "1");
        getOptionalArgs().put("faction", "you");
    }

    @Override
    public void perform(CommandContext commandContext) {
        if (!commandContext.sender.hasPermission("fac-milestone.aclaim")) {
            commandContext.sender.getServer().dispatchCommand(commandContext.sender, "f mhelp");
            return;
        }
        if (!(commandContext.sender instanceof Player)) return;
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(commandContext.player);
        if (commandContext.faction.getAccess(fPlayer, PermissableAction.TERRITORY) != Access.ALLOW) {
            commandContext.sender.sendMessage("§cVous n'avez pas les permissions !");
            return;
        }
        MilestoneCalculator data = plugin.getMilestoneData(commandContext.faction);
        if (!MilestoneAccess.fromLevel(data.getMilestone()).isExpandAccess()) {
            commandContext.sender.sendMessage("§cVous devez être au moins au Palier " + MilestoneAccess.getMinimumLevelForExpand() + " pour exécuter cette commande ! " +
                    "Palier actuel : " + data.getMilestone());
            return;
        }
        final Faction forFaction = commandContext.argAsFaction(1, commandContext.faction);
        int claimInt = getClaimGroups(forFaction).size();
        if (claimInt >= 2) {
            commandContext.sender.sendMessage("Trop de claims séparés !");
            return;
        }
        Integer radius = commandContext.argAsInt(0, 1);
        if (radius != null) {
            if (forFaction != null) {
                if (!forFaction.isNormal() || commandContext.faction.getAccess(fPlayer, PermissableAction.TERRITORY) == Access.ALLOW) {
                    if (radius < 1) {
                        commandContext.msg("<b>If you specify a radius, it must be at least 1.");
                    } else if (radius > Conf.claimRadiusLimit && !commandContext.fPlayer.isAdminBypassing()) {
                        commandContext.msg("<b>The maximum radius allowed is <h>%s<b>.", Conf.claimRadiusLimit);
                    } else if (radius < 2) {
                        tryClaim(commandContext, commandContext.fPlayer, forFaction, FLocation.wrap(commandContext.player));
                    } else if (!Permission.CLAIM_RADIUS.has(commandContext.sender, false)) {
                        commandContext.msg("<b>You do not have permission to claim in a radius.");
                    } else {
                        new SpiralTask(FLocation.wrap(commandContext.player), radius) {
                            private final int limit;
                            private int failCount = 0;

                            {
                                this.limit = Conf.radiusClaimFailureLimit - 1;
                            }

                            public boolean work() {
                                boolean success = tryClaim(commandContext, commandContext.fPlayer, forFaction, FLocation.wrap(this.currentLocation()));
                                if (success) {
                                    this.failCount = 0;
                                } else if (this.failCount++ >= this.limit) {
                                    this.stop();
                                    return false;
                                }

                                return true;
                            }
                        };
                    }
                }
            }
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOHELP_HELPFOR;
    }

    public boolean tryClaim(CommandContext commandContext, FPlayer uPlayer, Faction newFaction, FLocation ps) {
        if (uPlayer == null) {
            return false;
        }
        if (newFaction == null) {
            return false;
        }
        if (ps == null) {
            return false;
        }
        Faction oldFaction = Board.getInstance().getFactionAt(ps);
        if (newFaction == oldFaction) {
            commandContext.msg("%s<i> already owns this land.", newFaction.describeTo(uPlayer, true));
            return true;
        } else {
            if (!uPlayer.isAdminBypassing()) {
                if (newFaction.isNormal()) {
                    if (Conf.worldsNoClaiming.contains(ps.getWorld().getName())) {
                        commandContext.msg("<b>Sorry, this world has land claiming disabled.");
                        return false;
                    }

                    if (newFaction.getAccess(uPlayer, PermissableAction.TERRITORY) != Access.ALLOW) {
                        return false;
                    }

                    if (newFaction.getFPlayers().size() < Conf.claimsRequireMinFactionMembers) {
                        commandContext.msg("Factions must have at least <h>%s<b> members to claim land.", Conf.claimsRequireMinFactionMembers);
                        return false;
                    }

                    int ownedLand = newFaction.getLandRounded();
                    if (Conf.claimedLandsMax != 0 && ownedLand >= Conf.claimedLandsMax && !newFaction.hasPermanentPower()) { //TODO power infini ?
                        commandContext.msg("<b>Limit reached. You can't claim more land.");
                        return false;
                    }

                    if (ownedLand >= newFaction.getPowerRounded()) {
                        commandContext.msg("<b>You can't claim more land. You need more power.");
                        return false;
                    }

                }

                if (oldFaction.isNormal() && oldFaction.getAccess(uPlayer, PermissableAction.TERRITORY) != Access.ALLOW) {
//                    if (!Conf.claimingFromOthersAllowed) {
//                        commandContext.msg("<b>You may not claim land from others.");
//                        return false;
//                    }
                    //TODO juste en dessus
                    if (oldFaction.getRelationTo(newFaction).isAtLeast(Relation.TRUCE)) {
                        commandContext.msg("<b>You can't claim this land due to your relation with the current owner.");
                        return false;
                    }

                    if (!oldFaction.hasLandInflation()) {
                        commandContext.msg("%s<i> owns this land and is strong enough to keep it.", oldFaction.getTag(uPlayer));
                        return false;
                    }

                    if (!Board.getInstance().isBorderLocation(ps)) {
                        commandContext.msg("<b>You must start claiming land at the border of the territory.");
                        return false;
                    }
                }
            }
            LandClaimEvent event = new LandClaimEvent(ps, newFaction, uPlayer);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() && !uPlayer.isAdminBypassing()) {
                return false;
            } else {
                Board.getInstance().setFactionAt(newFaction, ps);
                Set<FPlayer> informees = new HashSet<>();
                informees.add(uPlayer);
                if (newFaction.isNormal()) {
                    informees.addAll(newFaction.getFPlayers());
                }

                if (oldFaction.isNormal()) {
                    informees.addAll(oldFaction.getFPlayers());
                }

                String chunkString = ps.toString();
                String typeString = event.getEventName().toLowerCase();

                if (Conf.logLandClaims) {
                    Core.getInstance().getLogger().info(TextUtil.parse("<h>%s<i> did %s %s <i>for <h>%s<i> from <h>%s<i>.", uPlayer.getName(), typeString, chunkString, newFaction.getTag(), oldFaction.getTag()));
                }

                for (FPlayer informee : informees) {
                    informee.msg("<h>%s<i> did %s %s <i>for <h>%s<i> from <h>%s<i>.", uPlayer.describeTo(informee, true), typeString, chunkString, newFaction.describeTo(informee), oldFaction.describeTo(informee));
                }

                return true;
            }
        }
    }

    private List<List<FLocation>> getClaimGroups(Faction faction) {
        List<List<FLocation>> groups = new ArrayList<>();
        for (FLocation claim : Board.getInstance().getAllClaims(faction)) {
            groups.add(Lists.newArrayList(claim));
        }
        boolean merged = true;
        while (merged) {
            merged = false;
            firstLoop:
            for (int i = 0; i < groups.size(); i++) {
                for (int j = i + 1; j < groups.size(); j++) {
                    List<FLocation> g1 = groups.get(i);
                    List<FLocation> g2 = groups.get(j);
                    if (canMerge(g1, g2)) {
                        g1.addAll(g2);
                        groups.remove(g2);
                        merged = true;
                        break firstLoop;
                    }
                }
            }
        }
        return groups;
    }

    private boolean canMerge(List<FLocation> g1, List<FLocation> g2) {
        for (FLocation ps1 : g1) {
            for (FLocation ps2 : g2) {
                if (areNeighbours(ps1, ps2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean areNeighbours(FLocation ps1, FLocation ps2) {
        int dx = ps1.getChunk().getX() - ps2.getChunk().getX();
        int dy = ps1.getChunk().getZ() - ps2.getChunk().getZ();
        return (Math.abs(dx) == 1 && dy == 0) || (Math.abs(dy) == 1 && dx == 0);
    }
}
