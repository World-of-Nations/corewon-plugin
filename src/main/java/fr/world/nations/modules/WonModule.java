package fr.world.nations.modules;

import com.massivecraft.factions.cmd.FCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WonModule {

    private static WonModule instance;
    private final String name;
    private final Plugin loader;
    private final File configFolder;

    public WonModule(Plugin loader, String name, boolean created_dir) {
        this.name = name;
        this.loader = loader;

        instance = this;

        this.configFolder = new File(loader.getDataFolder(), name);

        if (!configFolder.exists() && created_dir) {
            configFolder.mkdir();
        }

        File defaultConfigFile = getFile("config.yml");
        if (!defaultConfigFile.exists()) {
            try {
                defaultConfigFile.createNewFile();
                FileConfiguration config = getDefaultConfig();
                Map<String, Object> defaultConfigValues = getDefaultConfigValues();
                for (String key : defaultConfigValues.keySet()) {
                    config.set(key, defaultConfigValues.get(key));
                }
                config.save(defaultConfigFile);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public WonModule(Plugin loader, String name) {
        this(loader, name, true);
    }

    public static WonModule getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public File getFile(String fileName) {
        return new File(configFolder, fileName);
    }

    public FileConfiguration getConfig(String name) {
        return getConfig(getFile(name + ".yml"));
    }

    public FileConfiguration getConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getDefaultConfig() {
        return getConfig("config");
    }

    public Plugin getLoader() {
        return loader;
    }

    public abstract void load();

    public abstract void unload();

    public List<Listener> registerListeners() {
        return new ArrayList<>();
    }

    public Map<String, CommandExecutor> registerCommands() {
        return new HashMap<>();
    }

    public List<FCommand> registerFCommands() {
        return new ArrayList<>();
    }

    protected Map<String, Object> getDefaultConfigValues() {
        return Map.of();
    }
}
