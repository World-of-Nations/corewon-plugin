package fr.world.nations.pvp;

import com.google.common.collect.Lists;
import fr.world.nations.modules.WonModule;
import fr.world.nations.pvp.weapons.WeaponListener;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class WonPvp extends WonModule {

    private PvpManager pvpManager;

    public WonPvp(Plugin loader) {
        super(loader, "pvp");
    }

    @Override
    public void load() {
        pvpManager = new PvpManager(this, getDefaultConfig().getInt("countdown", 90), getDefaultConfig().getStringList("commands_disabled"));
    }

    @Override
    public void unload() {
    }

    @Override
    public List<Listener> registerListeners() {
        return Lists.newArrayList(new PvpListener(this), new WeaponListener(this));
    }


    @Override
    protected Map<String, Object> getDefaultConfigValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("countdown", 90);
        values.put("commands_disabled", Lists.newArrayList("/tp", "/tpa", "/tpaccept", "/warp", "/home", "/spawn", "/back", "/f home"));
        values.put("pvp_message", "&cVous êtes désormais en combat !");
        values.put("pvp_bar_message", "&cVous êtes en combat !");
        values.put("command_blocked_message", "&cVous ne pouvez pas faire cette commande en combat !");
        values.put("deco_combat_message", "&cVous avez quitté le serveur pendant un combat, vous êtes donc mort !");
        return values;
    }
}
