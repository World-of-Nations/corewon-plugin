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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    @JsonIgnore
    private int kothCurrentCapPercent;
    private CuboidRegion kothCuboid;
    private Location teleportPoint;
    @JsonIgnore
    private String currentFactionIdCap, currentPlayerCap;
    private String rewardType;
    private int rewardAmount;
    private int rewardTime;
    @JsonIgnore
    private State kothState;
    @JsonIgnore
    private int kothTaskId, statusTaskId, rewardTaskId, scorezoneTaskId;
    @JsonIgnore
    private boolean started;
    @JsonIgnore
    private boolean steal;

    public KothModel() {
    }

    public void start() {
        clear();
        kothTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), this::check, 40L, 40L).getTaskId();
        statusTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getInstance(), this::broadcastStatus, (20 * 60) * 5, (20 * 60) * 5).getTaskId();
        started = true;
    }

    @JsonIgnore
    public Player getCapper() {
        return Bukkit.getPlayerExact(this.currentPlayerCap);
    }


    public void check() {
        switch (this.kothState) {
            case EMPTY -> {
                for (Player player : getContainedPlayers()) {
                    FPlayer uPlayer = FPlayers.getInstance().getByPlayer(player);
                    if (uPlayer.hasFaction()) {
                        if (!player.isDead()) {
                            this.currentFactionIdCap = uPlayer.getFactionId();
                            this.currentPlayerCap = player.getName();
                            this.kothCurrentCapPercent = 0;
                            broadcastStartCap();
                            this.kothState = State.CAPPED;
                        }
                    } else {
                        sendFactionError(player);
                    }
                }
            }
            case CAPPED -> {
                for (Player player : getContainedPlayers()) {
                    FPlayer uPlayer = FPlayers.getInstance().getByPlayer(player);
                    if (uPlayer.hasFaction()) {
                        if (this.currentFactionIdCap.equals("empty") && this.currentPlayerCap.equals("empty")) {
                            if (!player.isDead()) {
                                this.currentFactionIdCap = uPlayer.getFactionId();
                                this.currentPlayerCap = player.getName();
                                broadcastStartCap();
                            }
                        }
                    } else {
                        sendFactionError(player);
                    }
                }
                if (this.getCapper() == null || !this.getCapper().isOnline() || !this.contains(getCapper())) {
                    Faction capperFaction = Factions.getInstance().getFactionById(currentFactionIdCap);
                    if (capperFaction == null) {
                        clear();
                        return;
                    }
                    Player capper = capperFaction.getOnlinePlayers().stream()
                            .filter(this::contains)
                            .findFirst().orElse(null);
                    if (capper != null) {
                        this.currentPlayerCap = capper.getName();
                    } else {
                        Set<Faction> stealerFactions = new HashSet<>();
                        long opponentCount = getContainedPlayers().stream()
                                .peek(p -> {
                                    FPlayer fPlayer = FPlayers.getInstance().getByPlayer(p);
                                    if (!fPlayer.hasFaction()) return;
                                    stealerFactions.add(fPlayer.getFaction());
                                }).count();
                        if (Math.floor(kothCurrentCapPercent / 10d) != Math.floor((kothCurrentCapPercent - opponentCount) / 10d)) {
                            for (Player onlinePlayer : capperFaction.getOnlinePlayers()) {
                                sendLoseControl(onlinePlayer, String.join(", ", stealerFactions.stream().map(Faction::getTag).toList()));
                            }
                        }
                        kothCurrentCapPercent -= opponentCount;
                        if (kothCurrentCapPercent <= 0) {
                            clear();
                        } else {
                            for (Player player : getContainedPlayers()) {
                                for (String s : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status-decrease")) {
                                    player.sendMessage(s.replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()).replace("%area_name%", kothName).replace("%control%", String.valueOf(kothCurrentCapPercent)));
                                }
                            }
                        }
                    }
                } else incrementCapPercent();
            }
        }
    }

    private List<Player> getContainedPlayers() {
        return Bukkit.getOnlinePlayers().stream().filter(this::contains).map(v -> (Player) v).toList();
    }

    private boolean isFromCapperFaction(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if (!fPlayer.hasFaction()) return false;
        return fPlayer.getFactionId().equals(currentFactionIdCap);
    }

    public void incrementCapPercent() {
        if (kothCurrentCapPercent < 100) {
            boolean ennemy = getContainedPlayers().stream().anyMatch(p -> !isFromCapperFaction(p));
            for (Player player : getContainedPlayers()) {
                for (String s : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status")) {
                    player.sendMessage(s.replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()).replace("%area_name%", kothName).replace("%control%", String.valueOf(kothCurrentCapPercent)));
                }
            }
            if (!ennemy) {
                long mutliplier = getContainedPlayers().stream().filter(this::isFromCapperFaction).count();
                //?? should never happen
                if (kothCurrentCapPercent < 0) {
                    System.out.println("172---------------------------");
                    kothCurrentCapPercent = 0;
                }
                this.kothCurrentCapPercent += mutliplier;
            }
        }
        if (kothCurrentCapPercent == 50) broadcastStatus();
        if (kothCurrentCapPercent == 100 && rewardTaskId == 0) {
            broadcastEndCap();
            BukkitTask task = new BukkitRunnable() {

                int warningCount = 0;

                @Override
                public void run() {
                    if (rewardType == null || rewardType.isEmpty()) return;
                    if (!RewardType.exists(rewardType)) {
                        if (++warningCount % 10 == 0) {
                            Core.getInstance().getLogger().warning("KOTH reward type \"" + rewardType + "\" does not exist ! Possible reward types : "
                                    + String.join(", ", Arrays.stream(RewardType.values()).map(type -> type.name().toLowerCase()).toList()));
                        }
                        return;
                    }
                    Faction faction = Factions.getInstance().getFactionById(currentFactionIdCap);
                    double rewardAmount = (int) Math.floor(getRewardAmount() * PowerManager.getInstance().getFactionFactor(faction, true));
                    if (rewardType.equalsIgnoreCase("power")) {
                        if (faction.getPowerBoost() < 300) {
                            if ((faction.getPowerBoost() + rewardAmount) > 300) {
                                rewardAmount = 300 - faction.getPowerBoost();
                            }
                            faction.setPowerBoost(faction.getPowerBoost() + rewardAmount);
                            PowerManager.getInstance().addPower(new PowerAddedModel(faction.getId(), System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1), rewardAmount));
                            for (FPlayer uPlayer : faction.getFPlayers()) {
                                if (!uPlayer.isOnline()) continue;
                                if (!contains(uPlayer.getPlayer())) continue;

                            }
                        }
                    } else if (rewardType.equalsIgnoreCase("money")) {
                        String cmd = "bank give " + faction.getTag() + " " + (int) (rewardAmount);
                        //System.out.println(cmd);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                        for (FPlayer mPlayer : faction.getFPlayers()) {
                            if (!mPlayer.isOnline()) continue;
                            if (!contains(mPlayer.getPlayer())) continue;
                            double money = faction.getFactionBalance();
                            for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.koth-reward-money")) {
                                mPlayer.getPlayer().sendMessage(msg
                                        .replace("%money_amount%", String.valueOf(rewardAmount))
                                        .replace("%total_money%", String.valueOf(money)));
                            }
                        }
                    }
                }
            }.runTaskTimer(Core.getInstance(), 20L * rewardTime, 20L * rewardTime);

            BukkitTask scorezoneTask = new BukkitRunnable() {

                @Override
                public void run() {
                    FactionData data = Core.getInstance().getModuleManager().getModule(WonStats.class).getStatsManager().getFactionData(Factions.getInstance().getFactionById(currentFactionIdCap).getTag());
                    if (data != null) data.addScoreZone(0.01D);
                }
            }.runTaskTimerAsynchronously(Core.getInstance(), 20 * 5, 20 * 5);

            rewardTaskId = task.getTaskId();
            scorezoneTaskId = scorezoneTask.getTaskId();
        }
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
        this.kothCurrentCapPercent = 0;
        this.currentPlayerCap = "empty";
        this.currentFactionIdCap = "empty";
        this.kothState = State.EMPTY;
        Bukkit.getScheduler().cancelTask(rewardTaskId);
        Bukkit.getScheduler().cancelTask(scorezoneTaskId);
        rewardTaskId = 0;
        scorezoneTaskId = 0;
    }

    private void broadcastStatus() {
        if ((currentFactionIdCap != null && !currentFactionIdCap.isEmpty()) && kothCurrentCapPercent != 0) {
            for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.area-status")) {
                Bukkit.broadcastMessage(msg.replace("%area_name%", kothName).replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()).replace("%control%", String.valueOf(kothCurrentCapPercent)));
            }
        }
    }

    private void broadcastStartCap() {
        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-start-control")) {
            Bukkit.broadcastMessage(msg.replace("%area_name%", kothName).replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()));
        }
    }

    private void broadcastEndCap() {
        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-end-control")) {
            Bukkit.broadcastMessage(msg.replace("%area_name%", kothName).replace("%faction%", Factions.getInstance().getFactionById(currentFactionIdCap).getTag()));
        }
    }

    private void sendLoseControl(Player player, String f) {
        for (String msg : WonKoth.getInstance().getDefaultConfig().getStringList("messages.players.faction-lose-control")) {
            player.sendMessage(msg.replace("%area_name%", kothName).replace("%faction%", f).replace("%control%", String.valueOf(kothCurrentCapPercent)));
        }
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
