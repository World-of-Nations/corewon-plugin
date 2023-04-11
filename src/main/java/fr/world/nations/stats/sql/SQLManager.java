package fr.world.nations.stats.sql;

import fr.world.nations.Core;
import fr.world.nations.stats.WonStats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLManager {

    private final Connection c;

    public SQLManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de charger le driver JDBC, désactivation du plugin...");
            Core.getInstance().getServer().getPluginManager().disablePlugin(Core.getInstance());
        }
        try {
            this.c = DriverManager.getConnection("jdbc:sqlite:"
                    +
                    WonStats.getInstance().getConfigFolder().getAbsolutePath() + "/wonstats.db");
            this.c.createStatement();
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonStats | Impossible de se connecter à la base de donnée, désactivation du plugin...");
            Core.getInstance().getServer().getPluginManager().disablePlugin(Core.getInstance());
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return c;
    }

    public void closeConnection() throws SQLException {
        c.close();
    }

}
