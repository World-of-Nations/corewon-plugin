package fr.world.nations.koth.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import fr.world.nations.Core;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.koth.managers.PowerManager;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.FactionData;
import fr.world.nations.util.FactionUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class KothModel {

    @JsonIgnore
    private final HashMap<Faction, Integer> factionRewards = new HashMap<>();
    @JsonIgnore
    private final HashMap<Faction, Integer> factionRewardsTasks = new HashMap<>();
    @EqualsAndHashCode.Include
    private String kothName;
    private CuboidRegion kothCuboid;
    private Location teleportPoint;
    private String rewardType;
    private int rewardAmount;
    private int rewardTime;
    @JsonIgnore
    private int kothTaskId, statusTaskId;
    @JsonIgnore
    private Integer scorezoneTaskId;
    @JsonIgnore
    private Integer rewardTaskId;
    @JsonIgnore
    private boolean started;
    @JsonIgnore
    private String capturingFactionId;
    @JsonIgnore
    private int capPercentage;

    public KothModel() {
    }

    public void start() {
        clear();
        kothTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), this::newCheck, 40L, 40L).getTaskId();
        statusTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), this::broadcastStatus, (20 * 60) * 5, (20 * 60) * 5).getTaskId();
        started = true;
    }

    @JsonIgnore
    public Faction getCapturingFaction() {
        Factions factions = Factions.getInstance();
        if (capturingFactionId == null)
            return factions.getWilderness();
        Faction faction = factions.getFactionById(capturingFactionId);
        if (faction == null)
            return factions.getWilderness();
        return faction;
    }

    public void newCheck() {
        // Used only for comparison : if a wilderness player is in the zone,
        // then it is considered that more that 1 faction is there, even
        // if wilderness is not considered as a faction in clashes
        List<Faction> containedFactions = getContainedPlayers().stream()
                //Simple message if player does not belong to any faction
                .peek(p -> {
                    if (!FPlayers.getInstance().getByPlayer(p).hasFaction()) {
                        sendFactionError(p);
                    }
                })
                .map(player -> FPlayers.getInstance().getByPlayer(player).getFaction())
                .distinct()
                .toList();
        // Filtering and sorting contained factions
        List<Faction> bestFactions = containedFactions.stream()
                .sorted(Comparator.comparingLong(
                        faction -> getContainedPlayers((Faction) faction).size()).reversed()
                )
                .filter(Faction::isNormal)
                .toList();
        if (bestFactions.isEmpty()) return;
        Faction currentFaction = getCapturingFaction();
        Faction bestFaction = bestFactions.get(0);
        //If the faction is alone in the zone then the capping
        //increases depending on how many player the zone contains
        if (bestFactions.size() > 1) {
            int firstFactionPlayerAmount = getContainedPlayers(bestFactions.get(0)).size();
            int secFactionPlayerAmount = getContainedPlayers(bestFactions.get(1)).size();
            if (firstFactionPlayerAmount == secFactionPlayerAmount) {
                for (Player player : getContainedPlayers()) {
                    for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.factions-clash")) {
                        player.sendMessage(msg
                                .replace("%area_name%", kothName)
                                .replace("%faction1%", bestFaction.getTag())
                                .replace("%faction2%", currentFaction.getTag())
                                .replace("%current_controller%", currentFaction.getTag())
                                .replace("%control%", String.valueOf(this.capPercentage))
                        );
                    }
                }
                return;
            }
        }
        int playerDifference = containedFactions.size() > 1 ? 1 : getContainedPlayers().size();
        boolean shouldIncreasePercentage = bestFaction == currentFaction;
        int newCapPercentage;
        if (shouldIncreasePercentage) {
            newCapPercentage = this.capPercentage + playerDifference;
        } else {
            newCapPercentage = this.capPercentage - playerDifference;
        }
        if (!shouldIncreasePercentage) {
            //Both should be null at the same time but who knows :)
            if (this.rewardTaskId != null || this.scorezoneTaskId != null) {
                cancelRewardScoreZoneTasks();
            }
            if (newCapPercentage < 0) {
                this.capturingFactionId = bestFaction.getId();
                this.capPercentage = -newCapPercentage;
                sendCaptureUpdate(bestFaction);
                return;
            }
            this.capPercentage = newCapPercentage;
            for (Player player : getCapturingFaction().getOnlinePlayers()) {
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-lose-control")) {
                    player.sendMessage(msg
                            .replace("%area_name%", kothName)
                            .replace("%faction%", bestFaction.getTag())
                            .replace("%control%", String.valueOf(this.capPercentage))
                    );
                }
            }
            for (Player player : getContainedPlayers(bestFaction)) {
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status-decrease")) {
                    player.sendMessage(msg
                            .replace("%area_name%", kothName)
                            .replace("%faction%", getCapturingFaction().getTag())
                            .replace("%control%", String.valueOf(this.capPercentage))
                    );
                }
            }

            List<Player> otherPlayers = getContainedPlayers().stream()
                    .filter(player -> FactionUtil.getFaction(player) != bestFaction
                            && FactionUtil.getFaction(player) != getCapturingFaction())
                    .toList();
            for (Player player : otherPlayers) {
                //Sends message saying that bestFaction is stealing this zone
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-steal-zone")) {
                    player.sendMessage(msg
                            .replace("%area_name%", kothName)
                            .replace("%faction%", bestFaction.getTag())
                            .replace("%victim%", currentFaction.getTag())
                            .replace("%control%", String.valueOf(this.capPercentage))
                    );
                }
            }
            return;
        }
        int futureCapPercentage = Math.min(newCapPercentage, 100);
        if (this.capPercentage < 50 && futureCapPercentage >= 50) {
            //Pour afficher un beau nombre pour tout le monde :) même si le vrai pourcentage est 51 ou plus
            capPercentage = 50;
            broadcastStatus();
        }

        if (capPercentage == 100) {
            if (rewardTaskId == null || scorezoneTaskId == null) {
                cancelRewardScoreZoneTasks();
                this.rewardTaskId = Bukkit.getScheduler().runTaskTimer(
                        Core.getInstance(), this::sendRewards, 20L * rewardTime, 20L * rewardTime
                ).getTaskId();
                this.scorezoneTaskId = Bukkit.getScheduler().runTaskTimer(
                        Core.getInstance(), this::addScore, 20L * rewardTime, 20L * rewardTime
                ).getTaskId();
            }
            if (capPercentage < 100 && futureCapPercentage == 100) {
                //Sends message saying that bestFaction is now fully controlling the zone
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-end-control")) {
                    Bukkit.broadcastMessage(msg
                            .replace("%area_name%", kothName)
                            .replace("%faction%", getCapturingFaction().getTag())
                    );
                }
            }
            return;
        }
        capPercentage = futureCapPercentage;
        sendCaptureUpdate(bestFaction);
    }

    public void sendCaptureUpdate(Faction capturer) {
        for (Player player : getContainedPlayers()) {
            for (String s : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status")) {
                player.sendMessage(s
                        .replace("%faction%", capturer.getTag())
                        .replace("%area_name%", kothName)
                        .replace("%control%", String.valueOf(this.capPercentage))
                );
            }
        }
    }

    public void sendRewards() {
        Bukkit.getServer().getLogger().info("Sending KOTH rewards...");
        if (rewardType == null || rewardType.isEmpty()) {
            Bukkit.getServer().getLogger().warning("No reward type.");
            return;
        }
        if (!RewardType.exists(rewardType)) {
            Core.getInstance().getLogger().warning("KOTH reward type \"" + rewardType + "\" does not exist ! Possible reward types : "
                    + String.join(", ", Arrays.stream(RewardType.values()).map(type -> type.name().toLowerCase()).toList()));
            return;
        }

        Faction faction = getCapturingFaction();
        Bukkit.getServer().getLogger().info("Giving " + rewardAmount + rewardType + " to " + faction.getTag());
        double rewardAmount = (int) Math.floor(getRewardAmount() * PowerManager.getInstance().getFactionFactor(faction, true));
        if (rewardType.equalsIgnoreCase("power")) {
            if (faction.getPowerBoost() < 300) {
                if ((faction.getPowerBoost() + rewardAmount) > 300) {
                    rewardAmount = 300 - faction.getPowerBoost();
                }
                faction.setPowerBoost(faction.getPowerBoost() + rewardAmount);
                PowerManager.getInstance().addPower(new PowerAddedModel(faction.getId(), System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1), rewardAmount));
                double power = faction.getPower();
                for (FPlayer uPlayer : faction.getFPlayers()) {
                    if (!uPlayer.isOnline()) continue;
                    if (!contains(uPlayer.getPlayer())) continue;
                    for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.koth-reward-power")) {
                        uPlayer.getPlayer().sendMessage(msg
                                .replace("%power_amount%", String.valueOf(rewardAmount))
                                .replace("%total_power%", String.valueOf(power)));
                    }
                }
                Bukkit.getServer().getLogger().info("Given!");
            }
            else Bukkit.getServer().getLogger().info("Not Given: Powerboost > 300");
        } else if (rewardType.equalsIgnoreCase("money")) {
            /*String cmd = "f ecogivef " + ((int) (rewardAmount)) + " " + faction.getTag();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);*/

            int reward = (int)rewardAmount;
            faction.setFactionBalance(faction.getFactionBalance() + reward);

            double money = faction.getFactionBalance();
            for (FPlayer mPlayer : faction.getFPlayers()) {
                if (!mPlayer.isOnline()) continue;
                if (!contains(mPlayer.getPlayer())) continue;
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.koth-reward-money")) {
                    mPlayer.getPlayer().sendMessage(msg
                            .replace("%money_amount%", String.valueOf(rewardAmount))
                            .replace("%total_money%", String.valueOf(money)));
                }
            }
            Bukkit.getServer().getLogger().info("Given!");
        }
    }

    public void addScore() {
        String capturingFactionTag = getCapturingFaction().getTag();
        FactionData data = Core.getInstance()
                .getModuleManager()
                .getModule(WonStats.class)
                .getStatsManager()
                .getFactionData(capturingFactionTag);
        if (data != null) data.addScoreZone(0.01D);
    }

    private List<Player> getContainedPlayers() {
        return Bukkit.getOnlinePlayers().stream().filter(this::contains).map(v -> (Player) v).toList();
    }

    private List<Player> getContainedPlayers(Faction faction) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(this::contains)
                .map(v -> (Player) v)
                .filter(player -> FPlayers.getInstance().getByPlayer(player).getFaction() == faction)
                .toList();
    }

    private boolean contains(Player player) {
        return contains(player.getLocation());
    }

    private boolean contains(Location loc) {
        return kothCuboid.contains(Vector3.toBlockPoint(loc.getX(), loc.getY(), loc.getZ()));
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(kothTaskId);
        Bukkit.getScheduler().cancelTask(statusTaskId);
        clear();
        started = false;
    }

    public void clear() {
        getContainedPlayers().forEach(p -> p.sendMessage("Area " + kothName + " has been cleared !"));
        this.capPercentage = 0;
        this.capturingFactionId = null;
        cancelRewardScoreZoneTasks();
    }

    private void broadcastStatus() {
        String capturingFactionName = getCapturingFaction().getTag();
        if ((capturingFactionId != null && !capturingFactionId.isEmpty()) && capPercentage != 0) {
            for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status")) {
                Bukkit.broadcastMessage(msg
                        .replace("%area_name%", kothName)
                        .replace("%faction%", capturingFactionName)
                        .replace("%control%", String.valueOf(capPercentage)));
            }
        }
    }

    private void cancelRewardScoreZoneTasks() {
        if (rewardTaskId != null)
            Bukkit.getScheduler().cancelTask(rewardTaskId);
        if (scorezoneTaskId != null)
            Bukkit.getScheduler().cancelTask(scorezoneTaskId);
        rewardTaskId = null;
        scorezoneTaskId = null;
    }

    private void sendFactionError(Player player) {
        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.no-faction")) {
            player.sendMessage(msg);
        }
    }

    private enum RewardType {
        POWER, MONEY;

        public static boolean exists(String type) {
            return Arrays.stream(values()).anyMatch(v -> v.name().equalsIgnoreCase(type));
        }
    }
}
