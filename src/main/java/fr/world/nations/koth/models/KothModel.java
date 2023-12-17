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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
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
    //@JsonIgnore
    //private int kothCurrentCapPercent;
    private CuboidRegion kothCuboid;
    private Location teleportPoint;
    //@JsonIgnore
    //private String currentFactionIdCap, currentPlayerCap;
    private String rewardType;
    private int rewardAmount;
    private int rewardTime;
    @JsonIgnore
    private State kothState;
    @JsonIgnore
    private int kothTaskId, statusTaskId;
    @JsonIgnore
    private Integer scorezoneTaskId;
    @JsonIgnore
    private Integer rewardTaskId;
    @JsonIgnore
    private boolean started;
    //    @JsonIgnore
//    private boolean steal;
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

//    @JsonIgnore
//    public Player getCapper() {
//        return Bukkit.getPlayerExact(this.currentPlayerCap);
//    }

    public void newCheck() {
        List<Faction> bestFactions = getContainedPlayers().stream()
                //Simple message if player does not belong to any faction
                .peek(p -> {
                    if (!FPlayers.getInstance().getByPlayer(p).hasFaction()) {
                        sendFactionError(p);
                    }
                })
                .map(player -> FPlayers.getInstance().getByPlayer(player).getFaction())
                .distinct()
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
        int playerDifference = bestFactions.size() > 1 ? 1 : getContainedPlayers().size();
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
            List<Player> players = new ArrayList<>(getContainedPlayers());
            players.addAll(getCapturingFaction().getOnlinePlayers());
            for (Player player : players) {
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
                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-full-control")) {
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
        if (rewardType == null || rewardType.isEmpty()) return;
        if (!RewardType.exists(rewardType)) {
            Core.getInstance().getLogger().warning("KOTH reward type \"" + rewardType + "\" does not exist ! Possible reward types : "
                    + String.join(", ", Arrays.stream(RewardType.values()).map(type -> type.name().toLowerCase()).toList()));
            return;
        }
        Faction faction = getCapturingFaction();
        double rewardAmount = (int) Math.floor(getRewardAmount() * PowerManager.getInstance().getFactionFactor(faction, true));
        if (rewardType.equalsIgnoreCase("power")) {
            if (faction.getPowerBoost() < 300) {
                if ((faction.getPowerBoost() + rewardAmount) > 300) {
                    rewardAmount = 300 - faction.getPowerBoost();
                }
//                faction.setPowerBoost(faction.getPowerBoost() + rewardAmount);
                PowerManager.getInstance().addPower(new PowerAddedModel(faction.getId(), System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1), rewardAmount));
                double power = faction.getPower();
                for (FPlayer uPlayer : faction.getFPlayers()) {
                    if (!uPlayer.isOnline()) continue;
                    if (!contains(uPlayer.getPlayer())) continue;
                    for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.koth-reward-money")) {
                        uPlayer.getPlayer().sendMessage(msg
                                .replace("%power_amount%", String.valueOf(rewardAmount))
                                .replace("%total_money%", String.valueOf(power)));
                    }
                }
            }
        } else if (rewardType.equalsIgnoreCase("money")) {
            String cmd = "f ecogivef " + ((int) (rewardAmount)) + " " + faction.getTag();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
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

//    public void check() {
//        switch (this.kothState) {
//            case EMPTY -> {
//                for (Player player : getContainedPlayers()) {
//                    FPlayer uPlayer = FPlayers.getInstance().getByPlayer(player);
//                    if (uPlayer.hasFaction()) {
//                        if (!player.isDead()) {
//                            this.currentFactionIdCap = uPlayer.getFactionId();
//                            this.currentPlayerCap = player.getName();
//                            this.kothCurrentCapPercent = 0;
//                            broadcastStartCap();
//                            this.kothState = State.CAPPED;
//                        }
//                    } else {
//                        sendFactionError(player);
//                    }
//                }
//            }
//            case CAPPED -> {
//                for (Player player : getContainedPlayers()) {
//                    FPlayer uPlayer = FPlayers.getInstance().getByPlayer(player);
//                    if (uPlayer.hasFaction()) {
//                        if (this.currentFactionIdCap.equals("empty") && this.currentPlayerCap.equals("empty")) {
//                            if (!player.isDead()) {
//                                this.currentFactionIdCap = uPlayer.getFactionId();
//                                this.currentPlayerCap = player.getName();
//                                broadcastStartCap();
//                            }
//                        }
//                    } else {
//                        sendFactionError(player);
//                    }
//                }
//                if (this.getCapper() == null || !this.getCapper().isOnline() || !this.contains(getCapper())) {
//                    Faction capperFaction = Factions.getInstance().getFactionById(currentFactionIdCap);
//                    if (capperFaction == null) {
//                        clear();
//                        return;
//                    }
//                    Player capper = capperFaction.getOnlinePlayers().stream()
//                            .filter(this::contains)
//                            .findFirst().orElse(null);
//                    if (capper != null) {
//                        this.currentPlayerCap = capper.getName();
//                    } else {
//                        Set<Faction> stealerFactions = new HashSet<>();
//                        long opponentCount = getContainedPlayers().stream()
//                                .peek(p -> {
//                                    FPlayer fPlayer = FPlayers.getInstance().getByPlayer(p);
//                                    if (!fPlayer.hasFaction()) return;
//                                    stealerFactions.add(fPlayer.getFaction());
//                                }).count();
//                        if (shouldSendLoseControl((int) opponentCount)) { //Assuming there wont be 2b players in the warzone ;)
//                            for (Player onlinePlayer : capperFaction.getOnlinePlayers()) {
//                                sendLoseControl(onlinePlayer, String.join(", ", stealerFactions.stream().map(Faction::getTag).toList()));
//                            }
//                        }
//                        kothCurrentCapPercent -= opponentCount;
//                        if (kothCurrentCapPercent <= 0) {
//                            clear();
//                        } else {
//                            for (Player player : getContainedPlayers()) {
//                                for (String s : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status-decrease")) {
//                                    player.sendMessage(s.replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()).replace("%area_name%", kothName).replace("%control%", String.valueOf(kothCurrentCapPercent)));
//                                }
//                            }
//                        }
//                    }
//                } else incrementCapPercent();
//            }
//        }
//    }

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

    private boolean isFromCapperFaction(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (!fPlayer.hasFaction()) return false;
        return fPlayer.getFactionId().equals(this.capturingFactionId);
    }

//    public void incrementCapPercent() {
//        int initPercent = kothCurrentCapPercent;
//        if (kothCurrentCapPercent < 100) {
//            boolean ennemy = getContainedPlayers().stream().anyMatch(p -> !isFromCapperFaction(p));
//            for (Player player : getContainedPlayers()) {
//                for (String s : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status")) {
//                    player.sendMessage(s.replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()).replace("%area_name%", kothName).replace("%control%", String.valueOf(kothCurrentCapPercent)));
//                }
//            }
//            if (!ennemy) {
//                long mutliplier = getContainedPlayers().stream().filter(this::isFromCapperFaction).count();
//                //?? should never happen
//                if (kothCurrentCapPercent < 0) {
//                    System.out.println("172---------------------------");
//                    kothCurrentCapPercent = 0;
//                }
//                this.kothCurrentCapPercent += mutliplier;
//                if (this.kothCurrentCapPercent > 100)
//                    this.kothCurrentCapPercent = 100;
//            }
//        }
//        if (kothCurrentCapPercent == 50 || (initPercent < 50 && kothCurrentCapPercent > 50)) broadcastStatus();
//        if (kothCurrentCapPercent == 100 && rewardTaskId == 0) {
//            broadcastEndCap();
//            BukkitTask task = new BukkitRunnable() {
//
//                int warningCount = 0;
//
//                @Override
//                public void run() {
//                    try {
//                        if (rewardType == null || rewardType.isEmpty()) return;
//                        if (!RewardType.exists(rewardType)) {
//                            if (++warningCount % 10 == 0) {
//                                Core.getInstance().getLogger().warning("KOTH reward type \"" + rewardType + "\" does not exist ! Possible reward types : "
//                                        + String.join(", ", Arrays.stream(RewardType.values()).map(type -> type.name().toLowerCase()).toList()));
//                            }
//                            return;
//                        }
//                        Faction faction = Factions.getInstance().getFactionById(currentFactionIdCap);
//                        double rewardAmount = (int) Math.floor(getRewardAmount() * PowerManager.getInstance().getFactionFactor(faction, true));
//                        if (rewardType.equalsIgnoreCase("power")) {
//                            if (faction.getPowerBoost() < 300) {
//                                if ((faction.getPowerBoost() + rewardAmount) > 300) {
//                                    rewardAmount = 300 - faction.getPowerBoost();
//                                }
//                                faction.setPowerBoost(faction.getPowerBoost() + rewardAmount);
//                                PowerManager.getInstance().addPower(new PowerAddedModel(faction.getId(), System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1), rewardAmount));
//                                for (FPlayer uPlayer : faction.getFPlayers()) {
//                                    if (!uPlayer.isOnline()) continue;
//                                    if (!contains(uPlayer.getPlayer())) continue;
//
//                                }
//                            }
//                        } else if (rewardType.equalsIgnoreCase("money")) {
//                            String cmd = "f ecogivef " + ((int) (rewardAmount)) + " " + faction.getTag();
//                            //System.out.println(cmd);
//                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
//                            for (FPlayer mPlayer : faction.getFPlayers()) {
//                                if (!mPlayer.isOnline()) continue;
//                                if (!contains(mPlayer.getPlayer())) continue;
//                                double money = faction.getFactionBalance();
//                                for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.koth-reward-money")) {
//                                    mPlayer.getPlayer().sendMessage(msg
//                                            .replace("%money_amount%", String.valueOf(rewardAmount))
//                                            .replace("%total_money%", String.valueOf(money)));
//                                }
//                            }
//                        }
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }.runTaskTimer(Core.getInstance(), 20L * rewardTime, 20L * rewardTime);
//
//            BukkitTask scorezoneTask = new BukkitRunnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        FactionData data = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager().getFactionData(Factions.getInstance().getFactionById(currentFactionIdCap).getTag());
//                        if (data != null) data.addScoreZone(0.01D);
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }.runTaskTimerAsynchronously(Core.getInstance(), 20 * 5, 20 * 5);
//
//            rewardTaskId = task.getTaskId();
//            scorezoneTaskId = scorezoneTask.getTaskId();
//        }
//    }

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

//    public void clear() {
//        getContainedPlayers().forEach(p -> p.sendMessage("Area " + kothName + " has been cleared !"));
//        this.kothCurrentCapPercent = 0;
//        this.currentPlayerCap = "empty";
//        this.currentFactionIdCap = "empty";
//        this.kothState = State.EMPTY;
//        Bukkit.getScheduler().cancelTask(rewardTaskId);
//        Bukkit.getScheduler().cancelTask(scorezoneTaskId);
//        rewardTaskId = 0;
//        scorezoneTaskId = 0;
//    }

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

//    private void broadcastStartCap() {
//        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-start-control")) {
//            Bukkit.broadcastMessage(msg.replace("%area_name%", kothName).replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()));
//        }
//    }
//
//    private void broadcastEndCap() {
//        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-end-control")) {
//            Bukkit.broadcastMessage(msg.replace("%area_name%", kothName).replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()));
//        }
//    }
//
//    private void sendLoseControl(Player player, String f) {
//        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-lose-control")) {
//            player.sendMessage(msg.replace("%area_name%", kothName).replace("%faction%", f).replace("%control%", String.valueOf(kothCurrentCapPercent)));
//        }
//    }

//    private boolean shouldSendLoseControl(int delta) {
//        int newX = kothCurrentCapPercent - delta;
//        if (kothCurrentCapPercent / 10 > newX / 10) {
//            if (kothCurrentCapPercent % 10 == 0) {
//                return newX % 10 == 0;
//            }
//            return true;
//        }
//        return newX % 10 == 0;
//    }

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

    public enum State {
        CAPPED, EMPTY
    }

    private enum RewardType {
        POWER, MONEY;

        public static boolean exists(String type) {
            return Arrays.stream(values()).anyMatch(v -> v.name().equalsIgnoreCase(type));
        }
    }
}
