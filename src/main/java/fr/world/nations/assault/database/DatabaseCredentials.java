package fr.world.nations.assault.database;

public class DatabaseCredentials {

    private final String host;

    private final String user;

    private final String pass;

    private final String dbName;

    private final int port;

    public DatabaseCredentials(String host, String user, String pass, String dbName, int port) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.dbName = dbName;
        this.port = port;
    }

    public String toURL() {
        return "jdbc:mysql://" +
                host +
                ":" +
                port +
                "/" +
                dbName;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getPass() {
        return pass;
    }

    public int getPort() {
        return port;
    }

    public String getDbName() {
        return dbName;
    }

}
