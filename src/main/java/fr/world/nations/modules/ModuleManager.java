package fr.world.nations.modules;

import com.massivecraft.factions.cmd.FCmdRoot;
import fr.world.nations.Core;

import java.util.ArrayList;
import java.util.Objects;

public class ModuleManager {

    private final Core loader;

    private final ArrayList<WonModule> modules = new ArrayList<>();

    public ModuleManager(Core loader) {
        this.loader = loader;
    }

    public void addModule(WonModule module) {
        modules.add(module);
    }

    public void loadModules() {
        modules.forEach(WonModule::load);
    }

    public void unloadModules() {
        modules.forEach(WonModule::unload);
    }

    public <T extends WonModule> T getModule(Class<T> clazz) {
        return modules.stream().filter(clazz::isInstance).map(clazz::cast).findFirst().orElse(null);
    }

    public void registerListeners() {
        modules.forEach(module -> {
            module.registerListeners().forEach(listener -> loader.getServer().getPluginManager().registerEvents(listener, loader));
        });
    }

    public void registerCommands() {
        modules.forEach(module -> {
            module.registerCommands().forEach((name, executor) -> Objects.requireNonNull(loader.getCommand(name)).setExecutor(executor));
        });
    }

    public void registerFCommands() {
        FCmdRoot cmdRoot = loader.getFactionsPlugin().cmdBase;
        modules.forEach(module -> {
            module.registerFCommands().forEach(cmdRoot::addSubCommand);
        });
        cmdRoot.rebuild();
    }


}
