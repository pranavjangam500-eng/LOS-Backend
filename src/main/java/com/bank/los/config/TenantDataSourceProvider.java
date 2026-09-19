package com.bank.los.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TenantDataSourceProvider {

    @Value("${tenant.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${tenant.datasource.username:postgres}")
    private String defaultUsername;

    @Value("${tenant.datasource.password:postgres}")
    private String defaultPassword;

    @Value("${tenant.datasource.default-host:localhost}")
    private String defaultHost;

    @Value("${tenant.datasource.default-port:5432}")
    private int defaultPort;

    @Value("${tenant.datasource.default-url-prefix:}")
    private String urlPrefix;

    @Value("${tenant.datasource.default-url-suffix:}")
    private String urlSuffix;

    @Value("${master.datasource.url:jdbc:postgresql://localhost:5432/los_master_db}")
    private String masterUrl;

    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    public DataSource getOrCreateTenantDataSource(String dbName, String dbHost, Integer dbPort) {
        return tenantDataSources.computeIfAbsent(dbName, key -> createDataSource(dbName, dbHost, dbPort));
    }

    public DataSource getTenantDataSource(String dbName) {
        return tenantDataSources.get(dbName);
    }

    public void registerDataSource(String dbName, DataSource dataSource) {
        tenantDataSources.put(dbName, dataSource);
    }

    public Map<String, DataSource> getAllTenantDataSources() {
        return tenantDataSources;
    }

    private DataSource createDataSource(String dbName, String dbHost, Integer dbPort) {
        log.info("Creating dynamic HikariDataSource for tenant DB: {}", dbName);

        String jdbcUrl;
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            jdbcUrl = urlPrefix + dbName + (urlSuffix != null ? urlSuffix : "");
        } else {
            ensurePostgreSqlDatabaseExists(masterUrl, dbName, defaultUsername, defaultPassword);
            String host = (dbHost != null && !dbHost.isEmpty()) ? dbHost : defaultHost;
            int port = (dbPort != null && dbPort > 0) ? dbPort : defaultPort;
            jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);

        PersistenceConfig.ConnectionDetails details = PersistenceConfig.parseConnectionDetails(jdbcUrl, defaultUsername, defaultPassword);
        ds.setJdbcUrl(details.jdbcUrl());
        ds.setUsername(details.username());
        ds.setPassword(details.password());
        ds.setPoolName("HikariPool-" + dbName);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setIdleTimeout(30000);
        ds.setConnectionTimeout(20000);

        try (java.sql.Connection conn = ds.getConnection()) {
            org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator =
                    new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
            populator.addScript(new org.springframework.core.io.ClassPathResource("db/tenant-schema.sql"));
            populator.setContinueOnError(true);
            populator.setIgnoreFailedDrops(true);
            populator.populate(conn);
        } catch (Exception e) {
            log.debug("Tenant DB schema populate notice for {}: {}", dbName, e.getMessage());
        }

        return ds;
    }

    private void ensurePostgreSqlDatabaseExists(String masterDbUrl, String targetDbName, String username, String password) {
        if (targetDbName == null || targetDbName.isEmpty() || (urlPrefix != null && !urlPrefix.isEmpty())) {
            return;
        }
        try {
            PersistenceConfig.ConnectionDetails masterDetails = PersistenceConfig.parseConnectionDetails(masterDbUrl, username, password);
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    masterDetails.jdbcUrl(), masterDetails.username(), masterDetails.password())) {
                conn.setAutoCommit(true);
                try (java.sql.Statement checkStmt = conn.createStatement();
                     java.sql.ResultSet rs = checkStmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + targetDbName + "'")) {
                    if (!rs.next()) {
                        log.info("Auto-provisioning tenant database in PostgreSQL: {}", targetDbName);
                        try (java.sql.Statement createStmt = conn.createStatement()) {
                            createStmt.executeUpdate("CREATE DATABASE \"" + targetDbName + "\"");
                            log.info("Successfully provisioned database: {}", targetDbName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Auto database creation note for {}: {}", targetDbName, e.getMessage());
        }
    }
}
