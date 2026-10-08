package com.bank.los.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic DataSource Provider for Multi-Bank databases.
 */
@Slf4j
@Component
public class BankDataSourceProvider {

    @Value("${bank.datasource.driver-class-name:${organization.datasource.driver-class-name:${tenant.datasource.driver-class-name:org.postgresql.Driver}}}")
    private String driverClassName;

    @Value("${bank.datasource.username:${organization.datasource.username:${tenant.datasource.username:postgres}}}")
    private String defaultUsername;

    @Value("${bank.datasource.password:${organization.datasource.password:${tenant.datasource.password:postgres}}}")
    private String defaultPassword;

    @Value("${bank.datasource.default-host:${organization.datasource.default-host:${tenant.datasource.default-host:localhost}}}")
    private String defaultHost;

    @Value("${bank.datasource.default-port:${organization.datasource.default-port:${tenant.datasource.default-port:5432}}}")
    private int defaultPort;

    @Value("${bank.datasource.default-url-prefix:${organization.datasource.default-url-prefix:${tenant.datasource.default-url-prefix:}}}")
    private String urlPrefix;

    @Value("${bank.datasource.default-url-suffix:${organization.datasource.default-url-suffix:${tenant.datasource.default-url-suffix:}}}")
    private String urlSuffix;

    @Value("${master.datasource.url:jdbc:postgresql://localhost:5432/los_master_db}")
    private String masterUrl;

    private final Map<String, DataSource> bankDataSources = new ConcurrentHashMap<>();

    public DataSource getOrCreateBankDataSource(String dbName, String dbHost, Integer dbPort) {
        return bankDataSources.computeIfAbsent(dbName, key -> createDataSource(dbName, dbHost, dbPort));
    }

    public DataSource getBankDataSource(String dbName) {
        return bankDataSources.get(dbName);
    }

    public void registerDataSource(String dbName, DataSource dataSource) {
        bankDataSources.put(dbName, dataSource);
    }

    public Map<String, DataSource> getAllBankDataSources() {
        return bankDataSources;
    }

    // Backwards-compatible aliases
    public DataSource getOrCreateOrganizationDataSource(String dbName, String dbHost, Integer dbPort) {
        return getOrCreateBankDataSource(dbName, dbHost, dbPort);
    }

    public DataSource getOrganizationDataSource(String dbName) {
        return getBankDataSource(dbName);
    }

    public Map<String, DataSource> getAllOrganizationDataSources() {
        return getAllBankDataSources();
    }

    public DataSource getOrCreateTenantDataSource(String dbName, String dbHost, Integer dbPort) {
        return getOrCreateBankDataSource(dbName, dbHost, dbPort);
    }

    public DataSource getTenantDataSource(String dbName) {
        return getBankDataSource(dbName);
    }

    public Map<String, DataSource> getAllTenantDataSources() {
        return getAllBankDataSources();
    }

    private record ResolvedConnection(String host, int port, String username, String password, String queryParams) {}

    private ResolvedConnection resolveConnection(String dbHost, Integer dbPort) {
        PersistenceConfig.ConnectionDetails masterDetails =
                PersistenceConfig.parseConnectionDetails(masterUrl, defaultUsername, defaultPassword);

        String masterHost = null;
        int masterPort = 5432;
        String queryParams = "";

        try {
            String cleanMasterUrl = masterDetails.jdbcUrl();
            if (cleanMasterUrl != null && cleanMasterUrl.startsWith("jdbc:postgresql://")) {
                String uriPart = cleanMasterUrl.substring("jdbc:postgresql://".length());
                int qIdx = uriPart.indexOf('?');
                if (qIdx > 0) {
                    queryParams = uriPart.substring(qIdx);
                    uriPart = uriPart.substring(0, qIdx);
                }
                int slashIdx = uriPart.indexOf('/');
                String hostPort = slashIdx > 0 ? uriPart.substring(0, slashIdx) : uriPart;
                if (hostPort.contains(":")) {
                    String[] parts = hostPort.split(":", 2);
                    masterHost = parts[0];
                    try { masterPort = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                } else if (!hostPort.isBlank()) {
                    masterHost = hostPort;
                }
            }
        } catch (Exception ignored) {}

        boolean hasExplicitHost = dbHost != null && !dbHost.isBlank() &&
                !"localhost".equalsIgnoreCase(dbHost.trim()) && !"127.0.0.1".equals(dbHost.trim());

        String host;
        int port;

        if (hasExplicitHost) {
            host = dbHost.trim();
            port = (dbPort != null && dbPort > 0) ? dbPort : defaultPort;
        } else if (defaultHost != null && !defaultHost.isBlank() &&
                !"localhost".equalsIgnoreCase(defaultHost.trim()) && !"127.0.0.1".equals(defaultHost.trim())) {
            host = defaultHost.trim();
            port = (defaultPort > 0) ? defaultPort : 5432;
        } else if (masterHost != null && !masterHost.isBlank() &&
                !"localhost".equalsIgnoreCase(masterHost) && !"127.0.0.1".equals(masterHost)) {
            host = masterHost;
            port = masterPort;
        } else {
            host = (dbHost != null && !dbHost.isBlank()) ? dbHost.trim() :
                    (defaultHost != null && !defaultHost.isBlank() ? defaultHost.trim() : "localhost");
            port = (dbPort != null && dbPort > 0) ? dbPort : (defaultPort > 0 ? defaultPort : 5432);
        }

        String username = defaultUsername;
        String password = defaultPassword;
        if ("postgres".equalsIgnoreCase(username) && masterDetails.username() != null && !"postgres".equalsIgnoreCase(masterDetails.username())) {
            username = masterDetails.username();
            password = masterDetails.password();
        }

        return new ResolvedConnection(host, port, username, password, queryParams);
    }

    private DataSource createDataSource(String dbName, String dbHost, Integer dbPort) {
        log.info("Creating dynamic HikariDataSource for bank DB: {}", dbName);

        String jdbcUrl;
        String effectiveUsername = defaultUsername;
        String effectivePassword = defaultPassword;

        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            jdbcUrl = urlPrefix + dbName + (urlSuffix != null ? urlSuffix : "");
        } else {
            ResolvedConnection conn = resolveConnection(dbHost, dbPort);
            effectiveUsername = conn.username();
            effectivePassword = conn.password();
            ensurePostgreSqlDatabaseExists(masterUrl, dbName, effectiveUsername, effectivePassword);
            jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s%s",
                    conn.host(), conn.port(), dbName, conn.queryParams());
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);

        PersistenceConfig.ConnectionDetails details = PersistenceConfig.parseConnectionDetails(jdbcUrl, effectiveUsername, effectivePassword);
        ds.setJdbcUrl(details.jdbcUrl());
        ds.setUsername(details.username());
        ds.setPassword(details.password());
        ds.setPoolName("HikariPool-" + dbName);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setIdleTimeout(30000);
        ds.setConnectionTimeout(20000);

        try (Connection conn = ds.getConnection()) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            ClassPathResource bankSchema = new ClassPathResource("db/bank-schema.sql");
            if (bankSchema.exists()) {
                populator.addScript(bankSchema);
            }
            ClassPathResource tenantSchema = new ClassPathResource("db/tenant-schema.sql");
            if (tenantSchema.exists()) {
                populator.addScript(tenantSchema);
            }
            ClassPathResource orgSchema = new ClassPathResource("db/organization-schema.sql");
            if (orgSchema.exists()) {
                populator.addScript(orgSchema);
            }
            populator.setContinueOnError(true);
            populator.setIgnoreFailedDrops(true);
            populator.populate(conn);
        } catch (Exception e) {
            log.debug("Bank DB schema populate notice for {}: {}", dbName, e.getMessage());
        }

        return ds;
    }

    private void ensurePostgreSqlDatabaseExists(String masterDbUrl, String targetDbName, String username, String password) {
        if (targetDbName == null || targetDbName.isEmpty() || (urlPrefix != null && !urlPrefix.isEmpty())) {
            return;
        }
        try {
            PersistenceConfig.ConnectionDetails masterDetails = PersistenceConfig.parseConnectionDetails(masterDbUrl, username, password);
            try (Connection conn = DriverManager.getConnection(
                    masterDetails.jdbcUrl(), masterDetails.username(), masterDetails.password())) {
                conn.setAutoCommit(true);
                try (Statement checkStmt = conn.createStatement();
                     ResultSet rs = checkStmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + targetDbName + "'")) {
                    if (!rs.next()) {
                        log.info("Auto-provisioning bank database in PostgreSQL: {}", targetDbName);
                        try (Statement createStmt = conn.createStatement()) {
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
