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
        List<Faction> containedFactions = getContainedPlayers().stream()
                .peek(p -> {
                    if (!FPlayers.getInstance().getByPlayer(p).hasFaction()) {
                        sendFactionError(p);
                    }
                })
                .map(player -> FPlayers.getInstance().getByPlayer(player).getFaction())
                .distinct()
                .toList();

        List<Faction> bestFactions = containedFactions.stream()
                .sorted(Comparator.comparingLong(faction -> getContainedPlayers((Faction) faction).size()).reversed())
                .filter(Faction::isNormal)
                .toList();

        if (bestFactions.isEmpty()) return;

        Faction currentFaction = getCapturingFaction();
        Faction bestFaction = bestFactions.get(0);

        if (bestFactions.size() > 1) {
            int first = getContainedPlayers(bestFactions.get(0)).size();
            int second = getContainedPlayers(bestFactions.get(1)).size();
            if (first == second) {
                getContainedPlayers().forEach(player -> {
                    for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.factions-clash")) {
                        player.sendMessage(msg
                                .replace("%area_name%", kothName)
                                .replace("%faction1%", bestFaction.getTag())
                                .replace("%faction2%", currentFaction.getTag())
                                .replace("%current_controller%", currentFaction.getTag())
                                .replace("%control%", String.valueOf(capPercentage))
                        );
                    }
                });
                return;
            }
        }

        int playerDifference = containedFactions.size() > 1 ? 1 : getContainedPlayers().size();
        boolean increasing = bestFaction == currentFaction;
        int newCap = increasing ? capPercentage + playerDifference : capPercentage - playerDifference;

        if (!increasing) {
            if (rewardTaskId != null || scorezoneTaskId != null) {
                cancelRewardScoreZoneTasks();
            }

            if (newCap < 0) {
                capturingFactionId = bestFaction.getId();
                capPercentage = -newCap;
                sendCaptureUpdate(bestFaction);
                return;
            }

            capPercentage = newCap;
            getCapturingFaction().getOnlinePlayers().forEach(player -> {
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-lose-control")) {
                    player.sendMessage(msg
                            .replace("%area_name%", kothName)
                            .replace("%faction%", bestFaction.getTag())
                            .replace("%control%", String.valueOf(capPercentage))
                    );
                }
            });

            getContainedPlayers(bestFaction).forEach(player -> {
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status-decrease")) {
                    player.sendMessage(msg
                            .replace("%area_name%", kothName)
                            .replace("%faction%", getCapturingFaction().getTag())
                            .replace("%control%", String.valueOf(capPercentage))
                    );
                }
            });

            return;
        }

        int futureCap = Math.min(newCap, 100);
        capPercentage = futureCap; // ✅ IMPORTANT : on met à jour AVANT le if

        if (capPercentage == 100) {
            if (rewardTaskId == null || scorezoneTaskId == null) {
                cancelRewardScoreZoneTasks();

                rewardTaskId = Bukkit.getScheduler().runTaskTimer(
                        Core.getInstance(), this::sendRewards, 20L * rewardTime, 20L * rewardTime
                ).getTaskId();

                scorezoneTaskId = Bukkit.getScheduler().runTaskTimer(
                        Core.getInstance(), this::addScore, 20L * rewardTime, 20L * rewardTime
                ).getTaskId();

                Bukkit.getLogger().info("[KOTH] ➕ Récompenses activées pour " + kothName);
            }

            for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-end-control")) {
                Bukkit.broadcastMessage(msg
                        .replace("%area_name%", kothName)
                        .replace("%faction%", getCapturingFaction().getTag()));
            }
            return;
        }

        if (capPercentage == 50) {
            broadcastStatus();
        }

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
            } else Bukkit.getServer().getLogger().info("Not Given: Powerboost > 300");
        } else if (rewardType.equalsIgnoreCase("money")) {
            /*String cmd = "f ecogivef " + ((int) (rewardAmount)) + " " + faction.getTag();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);*/

            int reward = (int) rewardAmount;
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
        String tag = getCapturingFaction().getTag();
        FactionData data = Core.getInstance()
                .getModuleManager()
                .getModule(WonStats.class)
                .getStatsManager()
                .getFactionData(tag);

        if (data != null) {
            data.addScoreZone(0.01D);
            Bukkit.getLogger().info("[KOTH] +0.01 point pour " + tag + " | total: " + data.getScoreZone());
        } else {
            Bukkit.getLogger().warning("[KOTH] FactionData introuvable pour " + tag);
        }
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
