package fr.world.nations.assault.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseAccess {
    private final DatabaseCredentials credentials;
    private HikariDataSource hikariDataSource;

    public DatabaseAccess(DatabaseCredentials credentials) {
        this.credentials = credentials;
    }

    private void setupHikariCP() {
        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setJdbcUrl(credentials.toURL());
        hikariConfig.setUsername(credentials.getUser());
        hikariConfig.setPassword(credentials.getPass());
        hikariConfig.setMaxLifetime(6000000L);
        hikariConfig.setIdleTimeout(300000L);
        hikariConfig.setLeakDetectionThreshold(300000L);
        hikariConfig.setConnectionTimeout(10000L);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void initPool() {
        setupHikariCP();
    }

    public void closePool() {
        this.hikariDataSource.close();
    }

    public Connection getConnection() throws SQLException {
        if (this.hikariDataSource == null) {
            System.out.println("Not connected");
            setupHikariCP();
        }
        return this.hikariDataSource.getConnection();
    }


}
