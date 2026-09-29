package com.bank.los.administration.lookup.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBankLookupCapabilitiesRequest {

    private Boolean canView;
    private Boolean canAdd;
    private Boolean canImportFromMaster;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canActivate;
    private Boolean canDeactivate;
}
