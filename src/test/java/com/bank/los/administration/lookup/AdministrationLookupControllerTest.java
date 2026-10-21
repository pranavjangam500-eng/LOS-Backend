package com.bank.los.administration.lookup;

import com.bank.los.administration.lookup.dto.LookupOptionResponse;
import com.bank.los.administration.lookup.service.AdministrationLookupService;
import com.bank.los.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AdministrationLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationLookupService administrationLookupService;

    @Test
    @WithMockUser(roles = "INTERNAL_ADMIN")
    @DisplayName("GET /api/v1/administration/lookups/10003/options should return 200 and option list")
    void testGetMasterLookupOptionsSuccess() throws Exception {
        when(administrationLookupService.getActiveOptionsByLookupCode(eq("10003"), any())).thenReturn(List.of(
                new LookupOptionResponse("1", "System Admin"),
                new LookupOptionResponse("2", "Super Admin"),
                new LookupOptionResponse("3", "Admin"),
                new LookupOptionResponse("4", "Checker"),
                new LookupOptionResponse("5", "Maker"),
                new LookupOptionResponse("6", "Viewer")
        ));

        mockMvc.perform(get("/api/v1/administration/lookups/10003/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("1"))
                .andExpect(jsonPath("$.data[0].description").value("System Admin"))
                .andExpect(jsonPath("$.data[2].code").value("3"))
                .andExpect(jsonPath("$.data[2].description").value("Admin"));
    }

    @Test
    @WithMockUser(roles = "INTERNAL_ADMIN")
    @DisplayName("GET /api/v1/administration/lookups/99999/options should return 404 for unknown lookup type")
    void testGetMasterLookupOptionsUnknownNotFound() throws Exception {
        when(administrationLookupService.getActiveOptionsByLookupCode(eq("99999"), any()))
                .thenThrow(new ResourceNotFoundException("Master lookup type not found with code: 99999"));

        mockMvc.perform(get("/api/v1/administration/lookups/99999/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CHECKER")
    @DisplayName("GET /api/v1/administration/lookups/10003/options as bank CHECKER role should return 200 OK")
    void testGetMasterLookupOptionsBankUserAllowed() throws Exception {
        when(administrationLookupService.getActiveOptionsByLookupCode(eq("10003"), any())).thenReturn(List.of(
                new LookupOptionResponse("4", "Checker"),
                new LookupOptionResponse("5", "Maker")
        ));

        mockMvc.perform(get("/api/v1/administration/lookups/10003/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").value("4"));
    }

    @Test
    @DisplayName("GET /api/v1/administration/lookups/10003/options unauthenticated should return 401")
    void testGetMasterLookupOptionsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/administration/lookups/10003/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/administration/lookups/types/10001 as bank admin when managed by platform should return 403 Forbidden")
    void testDeletePlatformLookupAsBankAdminForbidden() throws Exception {
        doThrow(new AccessDeniedException("This lookup is managed by the platform and cannot be modified."))
                .when(administrationLookupService).deleteLookupType(eq("10001"), any());

        mockMvc.perform(delete("/api/v1/administration/lookups/types/10001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
