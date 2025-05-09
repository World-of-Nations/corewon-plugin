package fr.world.nations.assault;

import com.google.common.collect.Lists;
import com.massivecraft.factions.Faction;
import fr.world.nations.util.FactionUtil;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssaultManager {
    private final Set<Assault> assaults;
    private final List<Assault> pending;
    private final WonAssault plugin;

    public AssaultManager(WonAssault plugin) {
        this.plugin = plugin;
        this.assaults = new HashSet<>();
        this.pending = Lists.newArrayList();
    }

    public boolean isInAssault(Player player) {
        return getAssault(player) != null;
    }

    public boolean isInAssault(Faction faction) {
        return getAssault(faction) != null;
    }

    public Assault getAssault(Player player) {
        if (player == null) return null;
        for (Assault assault : assaults) {
            if (assault.contains(player)) {
                return assault;
            }
        }
        return null;
    }

    public Assault getAssault(Faction faction) {
        if (!FactionUtil.isPlayerFaction(faction)) return null;
        for (Assault assault : assaults) {
            if (assault.contains(faction)) {
                return assault;
            }
        }
        return null;
    }

    public List<Assault> getAssaults() {
        return assaults.stream().toList();
    }

    public void startAssault(Faction attacker, Faction defendant, boolean explosions) {
        Assault assault = new Assault(plugin, attacker, defendant, explosions);
        assault.run();
        boolean added = assaults.add(assault);
        if (!added) {
            new Exception("Tried to add already existing assault").printStackTrace();
        }
    }

    public void remove(Assault assault) {
        /*if (!assault.getCTags().isEmpty()) {
            this.pending.add(assault);

        }*/
        this.assaults.remove(assault);
    }

    /*public void updateCTag(Player player) {
        for (Assault assault : pending) {
            if (assault.removeCTag(player)) {
                if (assault.getCTags().isEmpty()) {
                    pending.remove(assault);
                }
                return;
            }
        }
    }*/
}
