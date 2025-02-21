package fr.world.nations;

import com.massivecraft.factions.FactionsPlugin;
import fr.world.nations.assault.WonAssault;
import fr.world.nations.country.WonContry;
import fr.world.nations.koth.WonKoth;
import fr.world.nations.modules.ModuleManager;
import fr.world.nations.pvp.WonPvp;
import fr.world.nations.stats.WonStats;
import fr.world.nations.stats.data.StatsManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {

    //test
    private static Core instance;
    public FileConfiguration config = getConfig();
    private FactionsPlugin factionsPlugin;
    private ModuleManager moduleManager;


    public static Core getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        FactionsPlugin.getInstance().getLogger().setFilter(new ConsoleFilter());
        instance = this;
        moduleManager = new ModuleManager(this);

        if (!getServer().getPluginManager().isPluginEnabled("Factions")) {
            getServer().getLogger().severe("WonCore | Factions n'est pas installé, désactivation du plugin...");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        factionsPlugin = FactionsPlugin.getInstance();

        loadModules();
        getServer().getPluginManager().registerEvents(new FactionCommandsInterferer(), this);

        getServer().getLogger().info("WonCore | Plugin activé !");
    }

    @Override
    public void onDisable() {
        StatsManager.saveAllData();
        unloadModules();
        getServer().getLogger().info("WonCore | Plugin désactivé !");
    }

    public void loadModules() {
        moduleManager.addModule(new WonContry(this, "country"));
        moduleManager.addModule(new WonStats(this, "stats"));
        moduleManager.addModule(new WonKoth(this));
        moduleManager.addModule(new WonAssault(this));
        moduleManager.addModule(new WonPvp(this));

        moduleManager.loadModules();
        moduleManager.registerListeners();
        moduleManager.registerCommands();
        moduleManager.registerFCommands();
    }

    public void unloadModules() {
        moduleManager.unloadModules();
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public FactionsPlugin getFactionsPlugin() {
        return factionsPlugin;
    }
}
