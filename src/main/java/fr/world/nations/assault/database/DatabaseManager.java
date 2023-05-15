package fr.world.nations.assault.database;

import fr.world.nations.assault.WonAssault;

public enum DatabaseManager {

    WON_DB(new DatabaseCredentials(WonAssault.getInstance().databaseConfig.getString("Database.host"),
            WonAssault.getInstance().databaseConfig.getString("Database.user"),
            WonAssault.getInstance().databaseConfig.getString("Database.pass"),
            WonAssault.getInstance().databaseConfig.getString("Database.dbName"),
            WonAssault.getInstance().databaseConfig.getInt("Database.port")));

    private final DatabaseAccess databaseAccess;

    DatabaseManager(DatabaseCredentials creditentials) {
        this.databaseAccess = new DatabaseAccess((creditentials));
    }

    public static void initAllDatabaseConnections() {
        for (DatabaseManager databaseManager : values()) {
            databaseManager.databaseAccess.initPool();
            System.out.println("Pool initialized");
        }
    }

    public static void closeAllDatabaseConnections() {
        for (DatabaseManager databaseManager : values()) {
            databaseManager.databaseAccess.closePool();
            System.out.println("Pool CLOSED");
        }
    }

    public DatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }
}
