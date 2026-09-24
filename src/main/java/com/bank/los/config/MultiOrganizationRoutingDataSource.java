package com.bank.los.config;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

/**
 * Dynamic Multi-Organization Routing DataSource (Alias for MultiBankRoutingDataSource).
 */
@Slf4j
public class MultiOrganizationRoutingDataSource extends MultiBankRoutingDataSource {

    public MultiOrganizationRoutingDataSource(BankDataSourceProvider bankDataSourceProvider, DataSource masterDataSource) {
        super(bankDataSourceProvider, masterDataSource);
    }
}

