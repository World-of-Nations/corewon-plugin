package fr.world.nations.util;

import com.massivecraft.factions.Faction;

import java.util.HashMap;

public class CoolDownManager {
    private final HashMap<String, Long> map = new HashMap<>();

    public void addCoolDownSec(Faction faction, String objectId, double cdSec) {
        addCoolDownSec(faction.getId() + "-" + objectId, cdSec);
    }

    public void addCoolDownHour(Faction faction, String objectId, double cdHour) {
        addCoolDownHour(faction.getId() + "-" + objectId, cdHour);
    }

    public void addCoolDownSec(String arg, double cdSec) {
        //System.out.println(arg + " " + cdSec);
        map.put(arg, System.currentTimeMillis() + (long) (cdSec * 1000));
    }

    public void addCoolDownHour(String arg, double cdHour) {
        //System.out.println(arg + " " + TimeUnit.MILLISECONDS.toSeconds((long) (cdHour * 60 * 60 * 1000)));
        map.put(arg, System.currentTimeMillis() + (long) (cdHour * 60 * 60 * 1000));
    }

    public boolean removeCoolDown(Faction faction, String objectId) {
        return removeCoolDown(faction.getId() + "-" + objectId);
    }

    public boolean removeCoolDown(String arg) {
        return map.remove(arg) != null;
    }

    public boolean isUsable(Faction faction, String objectId) {
        return isUsable(faction.getId() + "-" + objectId);
    }

    private boolean isUsable(String arg) {
        return getRemaining(arg) == 0;
    }

    public long getRemaining(Faction faction, String objectId) {
        return getRemaining(faction.getId() + "-" + objectId);
    }

    public long getRemaining(String arg) {
        if (map.get(arg) == null) return 0;
        if (map.get(arg) - System.currentTimeMillis() < 0) {
            map.remove(arg);
            return 0;
        }
        return map.get(arg) - System.currentTimeMillis();
    }
}
