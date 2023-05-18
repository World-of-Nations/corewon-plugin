package fr.world.nations.koth;

import fr.world.nations.koth.commands.WarzoneCommand;
import fr.world.nations.koth.fastinv.FastInvManager;
import fr.world.nations.koth.listeners.PlayerQuitListeners;
import fr.world.nations.koth.managers.KothManager;
import fr.world.nations.koth.managers.PowerManager;
import fr.world.nations.koth.tasks.UpdatePowerTask;
import fr.world.nations.modules.WonModule;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Getter
public class WonKoth extends WonModule {

    private static WonKoth instance;
    private KothManager kothManager;

    public WonKoth(Plugin loader) {
        super(loader, "Warzones");
    }

    public static WonKoth getInstance() {
        return instance;
    }

    @Override
    public void load() {
        instance = this;
        kothManager = new KothManager(this);
        kothManager.loadKoths();

        FastInvManager.register(this.getLoader());

        File configFile = new File(getConfigFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                FileConfiguration configuration = getDefaultConfig();
                configuration.set("messages.players.no-faction",
                        List.of("Sorry, you must have a faction.", "§8§m----------------------"));
                configuration.set("messages.players.faction-start-control",
                        List.of("Faction %faction% start to take control of the area %area_name%",
                                "§8§m-------------------------------------------"));
                configuration.set("messages.players.faction-end-control",
                        List.of("Faction %faction% has finished taking control of the area %area_name%",
                                "§8§m-------------------------------------------"));
                configuration.set("messages.players.faction-lose-control",
                        List.of("Faction %faction% steal an area you control (%area_name%) (%control% %) ",
                                "§8§m-------------------------------------------"));
                configuration.set("messages.players.area-status",
                        List.of("Faction %faction% has control of the area %area_name% (%control% %)",
                                "§8§m-------------------------------------------"));
                configuration.set("messages.players.area-status-decrease",
                        List.of("Decrease in possession of %faction% in progress %control% %",
                                "§8§m-------------------------------------------"));
                configuration.set("messages.admins.koth-already-exists",
                        List.of("Area %area_name% already exists."));
                configuration.set("messages.admins.koth-created",
                        List.of("area %area_name% has created."));
                configuration.set("messages.admins.no-koth",
                        List.of("Area %area_name% doesn't exists."));
                configuration.set("messages.admins.koth-delete",
                        List.of("Area %area_name% is deleted."));
                configuration.set("messages.admins.koth-spawn-set",
                        List.of("Spawn area of %area_name% has been set."));
                configuration.set("messages.admins.koth-reward-set",
                        List.of("The reward has been set for area %area_name%"));

                configuration.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        PowerManager.getInstance().loadPower();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this.getLoader(), new UpdatePowerTask(), (20 * 60) * 20, (20 * 60) * 20);
    }

    @Override
    public void unload() {
        PowerManager.getInstance().savePower();
    }

    @Override
    public Map<String, CommandExecutor> registerCommands() {
        return Map.of("warzone", new WarzoneCommand(this));
    }

    @Override
    public List<Listener> registerListeners() {
        return List.of(new PlayerQuitListeners());
    }
}
