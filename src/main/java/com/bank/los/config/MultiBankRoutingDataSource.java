package com.bank.los.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * Dynamic Multi-Bank Routing DataSource.
 */
@Slf4j
public class MultiBankRoutingDataSource extends AbstractRoutingDataSource {

    private final BankDataSourceProvider bankDataSourceProvider;
    private final DataSource masterDataSource;

    public MultiBankRoutingDataSource(BankDataSourceProvider bankDataSourceProvider, DataSource masterDataSource) {
        this.bankDataSourceProvider = bankDataSourceProvider;
        this.masterDataSource = masterDataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String bankKey = BankContext.getCurrentBank();
        log.trace("Routing database connection for bank key: {}", bankKey);
        return bankKey;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String bankKey = BankContext.getCurrentBank();
        if (bankKey == null || bankKey.equalsIgnoreCase(BankContext.MASTER_BANK_ID)) {
            return masterDataSource;
        }

        DataSource bankDs = bankDataSourceProvider.getBankDataSource(bankKey);
        if (bankDs != null) {
            return bankDs;
        }

        // If not yet cached, attempt to look up/provision via provider
        bankDs = bankDataSourceProvider.getOrCreateBankDataSource(bankKey, null, null);
        if (bankDs != null) {
            return bankDs;
        }

        log.warn("No bank datasource found for '{}', falling back to master datasource", bankKey);
        return masterDataSource;
    }
}
