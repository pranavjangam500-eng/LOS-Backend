package com.bank.los.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.bank.los.master.repository", "com.bank.los.tenant.repository"},
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class PersistenceConfig {

    @Value("${master.datasource.url:jdbc:postgresql://localhost:5432/los_master_db}")
    private String masterUrl;

    @Value("${master.datasource.username:postgres}")
    private String masterUsername;

    @Value("${master.datasource.password:postgres}")
    private String masterPassword;

    @Value("${master.datasource.driver-class-name:org.postgresql.Driver}")
    private String masterDriverClassName;

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(masterDriverClassName);

        ConnectionDetails details = parseConnectionDetails(masterUrl, masterUsername, masterPassword);
        ds.setJdbcUrl(details.jdbcUrl());
        ds.setUsername(details.username());
        ds.setPassword(details.password());
        ds.setPoolName("MasterHikariPool");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setIdleTimeout(30000);
        ds.setConnectionTimeout(30000);

        // Ensure master schema exists
        try (java.sql.Connection conn = ds.getConnection()) {
            org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator =
                    new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
            populator.addScript(new org.springframework.core.io.ClassPathResource("db/master-schema.sql"));
            populator.setContinueOnError(true);
            populator.setIgnoreFailedDrops(true);
            populator.populate(conn);
        } catch (Exception e) {
            // Ignore if already created or offline during build
        }

        return ds;
    }

    @Bean(name = "routingDataSource")
    @Primary
    public DataSource routingDataSource(TenantDataSourceProvider tenantDataSourceProvider,
                                        @Qualifier("masterDataSource") DataSource masterDataSource) {
        MultiTenantRoutingDataSource routingDataSource = new MultiTenantRoutingDataSource(tenantDataSourceProvider, masterDataSource);
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(TenantContext.MASTER_TENANT_ID, masterDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("routingDataSource") DataSource routingDataSource,
            JpaProperties jpaProperties,
            org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties hibernateProperties) {
        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings());

        // Explicitly disable eager JDBC metadata access during startup
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");

        return builder
                .dataSource(routingDataSource)
                .packages("com.bank.los.master.entity", "com.bank.los.tenant.entity")
                .persistenceUnit("losPersistenceUnit")
                .properties(properties)
                .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    public static ConnectionDetails parseConnectionDetails(String rawUrl, String defaultUser, String defaultPass) {
        if (rawUrl == null || rawUrl.isEmpty()) {
            return new ConnectionDetails(rawUrl, defaultUser, defaultPass);
        }
        String url = rawUrl.trim();
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            try {
                String uriString = url.replaceFirst("^(postgresql|postgres)://", "http://");
                java.net.URI uri = new java.net.URI(uriString);
                String userInfo = uri.getUserInfo();
                String username = defaultUser;
                String password = defaultPass;
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                } else if (userInfo != null && !userInfo.isEmpty()) {
                    username = userInfo;
                }
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "los_master_db";
                String host = uri.getHost();
                String query = uri.getQuery();
                if (host != null && host.contains("render.com") && (query == null || !query.contains("sslmode"))) {
                    query = (query == null || query.isEmpty()) ? "sslmode=require" : query + "&sslmode=require";
                }
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + (query != null && !query.isEmpty() ? "?" + query : "");
                return new ConnectionDetails(jdbcUrl, username, password);
            } catch (Exception e) {
                if (!url.startsWith("jdbc:")) {
                    return new ConnectionDetails("jdbc:" + url, defaultUser, defaultPass);
                }
            }
        }
        if (url.startsWith("jdbc:postgresql://") && url.contains("render.com") && !url.contains("sslmode")) {
            url = url.contains("?") ? url + "&sslmode=require" : url + "?sslmode=require";
        }
        return new ConnectionDetails(url, defaultUser, defaultPass);
    }

    public record ConnectionDetails(String jdbcUrl, String username, String password) {}
}
