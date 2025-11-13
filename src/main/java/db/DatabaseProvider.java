package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseProvider {
    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource ds;

    static {
        config.setJdbcUrl("jdbc_url");
        config.setUsername("database_username");
        config.setPassword("database_password");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    private DatabaseProvider() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
