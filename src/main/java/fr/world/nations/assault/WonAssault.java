package fr.world.nations.assault;

import com.google.common.collect.Lists;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.cmd.FCommand;
import fr.world.nations.Core;
import fr.world.nations.assault.explosion.ExplosionListener;
import fr.world.nations.assault.explosion.ExplosionManager;
import fr.world.nations.modules.WonModule;
import fr.world.nations.util.CoolDownManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class WonAssault extends WonModule {

    private static WonAssault instance;
    public FileConfiguration databaseConfig;
    private CoolDownManager coolDownManager;
    private ExplosionManager explosionManager;
    private AssaultManager assaultManager;

    public WonAssault(Plugin loader) {
        super(loader, "Assault");
    }

    public static WonAssault getInstance() {
        return instance;
    }

    @Override
    public void load() {
        // Plugin startup logic
        instance = this;

        if (!getConfigFolder().exists()) {
            getConfigFolder().mkdir();
        }

        File configFile = new File(getConfigFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                loadConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        coolDownManager = new CoolDownManager();
        explosionManager = new ExplosionManager(this);
        explosionManager.load();
        assaultManager = new AssaultManager(this);

        databaseConfig = null;
        File databaseFile = new File(Core.getInstance().getDataFolder(), "database.yml");
        if (isInvalid(databaseFile)) {
            Core.getInstance().getLogger().warning("Config database non trouvée ! A-t-elle été migrée ? Contactez moi : Asarix#1234");
            File localDatabaseFile = new File(getConfigFolder(), "database.yml");
            if (isInvalid(localDatabaseFile)) {
                try {
                    databaseFile.createNewFile();
                    databaseConfig = YamlConfiguration.loadConfiguration(localDatabaseFile);
                    if (databaseConfig.get("Database.pass") == null) {
                        databaseConfig.set("Database.host", "");
                        databaseConfig.set("Database.user", "");
                        databaseConfig.set("Database.pass", "");
                        databaseConfig.set("Database.dbName", "");
                        databaseConfig.set("Database.port", 0);
                    }
                    try {
                        databaseConfig.save(databaseFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            databaseConfig = YamlConfiguration.loadConfiguration(localDatabaseFile);
        } else {
            databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);
        }
    }

    @Override
    public void unload() {
        explosionManager.save();
    }

    @Override
    public List<FCommand> registerFCommands() {
        return Lists.newArrayList(new AssaultCommand(this));
    }

    @Override
    public List<Listener> registerListeners() {
        return Lists.newArrayList(new AssaultListener(this), new ExplosionListener(this));
    }

    public AssaultManager getAssaultManager() {
        return assaultManager;
    }

    public ExplosionManager getExplosionManager() {
        return explosionManager;
    }

    public void addAttackCoolDown(Faction attacker, Faction defendant) {
        double cdHours = getConfig().getDouble("assault.attack-cooldown-hour");
        coolDownManager.addCoolDownHour(attacker, "assault-attack-" + defendant.getId(), cdHours);
    }

    public boolean removeAttackCoolDown(Faction attacker, Faction defendant) {
        return coolDownManager.removeCoolDown(attacker, "assault-attack-" + defendant.getId());
    }

    public boolean canAttack(Faction attacker, Faction defendant) {
        return coolDownManager.isUsable(attacker, "assault-attack-" + defendant.getId());
    }

    public long getAttackRemaining(Faction attacker, Faction defendant) {
        return coolDownManager.getRemaining(attacker, "assault-attack-" + defendant.getId());
    }

    public void addClaimCoolDown(Faction faction) {
        double cdHours = getConfig().getDouble("assault.claim-disabled-cooldown-hours");
        coolDownManager.addCoolDownHour(faction, "assault-claim", cdHours);
    }

    public boolean removeClaimCoolDown(Faction faction) {
        return coolDownManager.removeCoolDown(faction, "assault-claim");
    }

    public boolean canClaim(Faction faction) {
        return coolDownManager.isUsable(faction, "assault-claim");
    }

    public long getClaimRemaining(Faction faction) {
        return coolDownManager.getRemaining(faction, "assault-claim");
    }

    private boolean isInvalid(File databaseFile) {
        if (!databaseFile.exists()) return true;
        FileConfiguration databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);
        return databaseConfig.getString("Database.host").equals("") || databaseConfig.getString("Database.pass").equals("") || databaseConfig.getString("Database.user").equals("") || databaseConfig.getInt("Database.port") == 0 || databaseConfig.getString("Database.dbName").equals("");
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        config.set("assault.duration-min", 30);
        config.set("assault.target-chunk-start-delay-mins", 10);
        config.set("assault.target-chunk-unclaim-delay-sec", 180);
        config.set("assault.target-chunk-success-points", 5);
        config.set("assault.target-chuck-fail-points", 5);
        config.set("assault.bank-transfer-percentage", 5);
        config.set("assault.logout-penality-time-minutes", 3);
        config.set("assault.faction-age-required-days", 7);
        config.set("assault.attack-cooldown-hour", 2);
        config.set("assault.claim-disabled-cooldown-hours", 1);
        config.set("explosions.enemy-required-time-days", 7);
        config.set("assault.score-broadcast-delay-secs", 300);
        config.set("assault.quit-stop-min", 2);
        try {
            config.save(new File(getConfigFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
