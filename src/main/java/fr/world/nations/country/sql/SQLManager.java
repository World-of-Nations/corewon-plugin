package fr.world.nations.country.sql;

import fr.world.nations.Core;
import fr.world.nations.country.WonContry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLManager {

    private final Connection c;

    public SQLManager(WonContry wonContry) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de charger le driver JDBC, désactivation du plugin...");
            Core.getInstance().getServer().getPluginManager().disablePlugin(Core.getInstance());
        }
        try {
            this.c = DriverManager.getConnection("jdbc:sqlite:"
                    +
                    wonContry.getConfigFolder().getAbsolutePath() + "/woncontry.db");
            this.c.createStatement();
        } catch (SQLException e) {
            Core.getInstance().getLogger().severe("WonCountry | Impossible de se connecter à la base de donnée, désactivation du plugin...");
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
