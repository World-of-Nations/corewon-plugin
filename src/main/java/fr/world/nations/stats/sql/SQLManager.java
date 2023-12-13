package fr.world.nations.stats.sql;

import fr.world.nations.Core;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLManager {

    private Connection c;

    public SQLManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de charger le driver JDBC, désactivation du plugin...");
            Core.getInstance().getServer().getPluginManager().disablePlugin(Core.getInstance());
        }
        try {
            getConnection();
            this.c.createStatement();
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de se connecter à la base de donnée, désactivation du plugin...");
            Core.getInstance().getServer().getPluginManager().disablePlugin(Core.getInstance());
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        try {
            // Check if the connection is closed or null
            if (c == null || c.isClosed()) {
                // Re-establish the connection
                File dbCredFile = new File(Core.getInstance().getDataFolder(), "database.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(dbCredFile);
                String host = config.getString("Database.host");
                String user = config.getString("Database.user");
                String password = config.getString("Database.pass");
                String dbName = config.getString("Database.dbName");
                int port = config.getInt("Database.port");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

                // Assign the new connection to 'c'
                c = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Failed to refresh the database connection, disabling the plugin...");
            Core.getInstance().getServer().getPluginManager().disablePlugin(Core.getInstance());
            throw new RuntimeException(e);
        }
        return c;
    }


    public void closeConnection() throws SQLException {
        c.close();
    }

}
