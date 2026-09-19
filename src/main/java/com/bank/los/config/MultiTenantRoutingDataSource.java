package com.bank.los.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

@Slf4j
public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {

    private final TenantDataSourceProvider tenantDataSourceProvider;
    private final DataSource masterDataSource;

    public MultiTenantRoutingDataSource(TenantDataSourceProvider tenantDataSourceProvider, DataSource masterDataSource) {
        this.tenantDataSourceProvider = tenantDataSourceProvider;
        this.masterDataSource = masterDataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantKey = TenantContext.getCurrentTenant();
        log.trace("Routing database connection for tenant key: {}", tenantKey);
        return tenantKey;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String tenantKey = TenantContext.getCurrentTenant();
        if (tenantKey == null || tenantKey.equalsIgnoreCase(TenantContext.MASTER_TENANT_ID)) {
            return masterDataSource;
        }

        DataSource tenantDs = tenantDataSourceProvider.getTenantDataSource(tenantKey);
        if (tenantDs != null) {
            return tenantDs;
        }

        // If not yet cached, attempt to look up via provider
        tenantDs = tenantDataSourceProvider.getOrCreateTenantDataSource(tenantKey, null, null);
        if (tenantDs != null) {
            return tenantDs;
        }

        log.warn("No tenant datasource found for '{}', falling back to master datasource", tenantKey);
        return masterDataSource;
    }
}
