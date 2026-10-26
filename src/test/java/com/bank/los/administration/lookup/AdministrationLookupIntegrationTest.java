package com.bank.los.administration.lookup;

import com.bank.los.administration.lookup.dto.*;
import com.bank.los.administration.lookup.service.AdministrationLookupService;
import com.bank.los.security.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AdministrationLookupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdministrationLookupService administrationLookupService;

    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@losplatform.com")
                .role("INTERNAL_ADMIN")
                .build();
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("1. Server generates type codes >= 10010 and sequential sub-type codes starting at 1")
    void testServerGeneratedCodes() throws Exception {
        // Create new type with name only (no code sent)
        CreateLookupTypeRequest typeReq = CreateLookupTypeRequest.builder()
                .name("Server Generated Test Type " + System.currentTimeMillis())
                .status("ACTIVE")
                .isFixed(false)
                .build();

        MvcResult typeResult = mockMvc.perform(post("/api/v1/administration/lookups/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(typeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value(typeReq.getName()))
                .andExpect(jsonPath("$.data.description").value(typeReq.getName()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andReturn();

        JsonNode typeJson = objectMapper.readTree(typeResult.getResponse().getContentAsString());
        String generatedTypeCode = typeJson.path("data").path("code").asText();
        long numericTypeCode = Long.parseLong(generatedTypeCode);
        assertTrue(numericTypeCode >= 10010, "Generated code must start at 10010 or higher (actual: " + numericTypeCode + ")");

        // Add sub-type 1 (no subTypeCode sent)
        CreateLookupSubTypeRequest subTypeReq1 = CreateLookupSubTypeRequest.builder()
                .name("Option One")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/v1/administration/lookups/types/" + generatedTypeCode + "/sub-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subTypeReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("1"))
                .andExpect(jsonPath("$.data.subTypeCode").value("1"))
                .andExpect(jsonPath("$.data.typeCode").value(generatedTypeCode))
                .andExpect(jsonPath("$.data.name").value("Option One"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Add sub-type 2 (incremental)
        CreateLookupSubTypeRequest subTypeReq2 = CreateLookupSubTypeRequest.builder()
                .name("Option Two")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/v1/administration/lookups/types/" + generatedTypeCode + "/sub-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subTypeReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("2"))
                .andExpect(jsonPath("$.data.subTypeCode").value("2"))
                .andExpect(jsonPath("$.data.typeCode").value(generatedTypeCode))
                .andExpect(jsonPath("$.data.name").value("Option Two"));
    }

    @Test
    @DisplayName("2. 20 parallel creates for master lookup types: all succeed and all codes are unique")
    void test20ParallelTypeCreates() throws Exception {
        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        List<LookupTypeResponse> createdTypes = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    CreateLookupTypeRequest req = CreateLookupTypeRequest.builder()
                            .name("Parallel Lookup Type " + index + "_" + UUID.randomUUID())
                            .status("ACTIVE")
                            .isFixed(false)
                            .build();
                    LookupTypeResponse res = administrationLookupService.createLookupType(req, adminPrincipal);
                    createdTypes.add(res);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All 20 type creates should finish within timeout");
        executor.shutdown();

        assertTrue(errors.isEmpty(), "No thread should fail: " + errors);
        assertEquals(20, createdTypes.size());

        Set<String> uniqueCodes = new HashSet<>();
        for (LookupTypeResponse r : createdTypes) {
            assertTrue(uniqueCodes.add(r.getCode()), "Duplicate code generated: " + r.getCode());
            long val = Long.parseLong(r.getCode());
            assertTrue(val >= 10010, "Code should be >= 10010");
        }
    }

    @Test
    @DisplayName("3. 20 parallel sub-type creates under the same parent type: all succeed and all codes are unique (1..20)")
    void test20ParallelSubTypeCreatesUnderSameParent() throws Exception {
        // Create a unique parent type
        CreateLookupTypeRequest parentReq = CreateLookupTypeRequest.builder()
                .name("Parent For Concurrent SubTypes " + UUID.randomUUID())
                .status("ACTIVE")
                .isFixed(false)
                .build();
        LookupTypeResponse parent = administrationLookupService.createLookupType(parentReq, adminPrincipal);
        String typeCode = parent.getCode();

        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        List<LookupSubTypeResponse> createdSubTypes = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    CreateLookupSubTypeRequest req = CreateLookupSubTypeRequest.builder()
                            .name("Concurrent Option " + index)
                            .status("ACTIVE")
                            .build();
                    LookupSubTypeResponse res = administrationLookupService.addSubType(typeCode, req, adminPrincipal);
                    createdSubTypes.add(res);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All 20 sub-type creates should finish within timeout");
        executor.shutdown();

        assertTrue(errors.isEmpty(), "No sub-type create should fail: " + errors);
        assertEquals(20, createdSubTypes.size());

        Set<String> codes = new HashSet<>();
        for (LookupSubTypeResponse st : createdSubTypes) {
            assertTrue(codes.add(st.getCode()), "Duplicate sub-type code found: " + st.getCode());
        }
        assertEquals(20, codes.size());
        for (int i = 1; i <= 20; i++) {
            assertTrue(codes.contains(String.valueOf(i)), "Expected sub-type code " + i + " to exist");
        }
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("4. Duplicate name checks return 409 Conflict with clear message (case-insensitive)")
    void testDuplicateNameReturns409() throws Exception {
        String uniqueName = "Unique Type Name " + UUID.randomUUID();

        // Create first type
        CreateLookupTypeRequest req1 = CreateLookupTypeRequest.builder()
                .name(uniqueName)
                .build();

        mockMvc.perform(post("/api/v1/administration/lookups/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk());

        // Attempt duplicate name with different casing
        CreateLookupTypeRequest reqDuplicate = CreateLookupTypeRequest.builder()
                .name(uniqueName.toLowerCase())
                .build();

        mockMvc.perform(post("/api/v1/administration/lookups/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDuplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Lookup type already exists with name")));

        // Duplicate Sub-Type test
        LookupTypeResponse parent = administrationLookupService.createLookupType(
                CreateLookupTypeRequest.builder().name("Parent For Dup Option " + UUID.randomUUID()).build(),
                adminPrincipal);

        CreateLookupSubTypeRequest sub1 = CreateLookupSubTypeRequest.builder().name("Silver Option").build();
        mockMvc.perform(post("/api/v1/administration/lookups/types/" + parent.getCode() + "/sub-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sub1)))
                .andExpect(status().isOk());

        CreateLookupSubTypeRequest subDup = CreateLookupSubTypeRequest.builder().name("silver option").build();
        mockMvc.perform(post("/api/v1/administration/lookups/types/" + parent.getCode() + "/sub-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subDup)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Lookup sub-type already exists with name")));
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("5. Search and pagination for types and sub-types returns paginated structure")
    void testSearchAndPagination() throws Exception {
        // Search types with query and pagination
        mockMvc.perform(get("/api/v1/administration/lookups/types")
                        .param("search", "Designation")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber());

        // Search sub-types under 10002
        mockMvc.perform(get("/api/v1/administration/lookups/types/10002/sub-types")
                        .param("search", "Manager")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("6. Old no-params list behavior unchanged: returns plain list")
    void testOldNoParamsListBehavior() throws Exception {
        // Calling /types with NO params must return a plain array
        mockMvc.perform(get("/api/v1/administration/lookups/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").exists())
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].subTypes").doesNotExist()); // subTypes omitted from list

        // Calling /types/10002/sub-types with NO params must return a plain array
        mockMvc.perform(get("/api/v1/administration/lookups/types/10002/sub-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").exists())
                .andExpect(jsonPath("$.data[0].name").exists());
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("7. Activate and deactivate PATCH endpoints modify status")
    void testActivateAndDeactivate() throws Exception {
        // Create custom type
        LookupTypeResponse type = administrationLookupService.createLookupType(
                CreateLookupTypeRequest.builder().name("Toggleable Type " + UUID.randomUUID()).status("ACTIVE").build(),
                adminPrincipal);

        // Deactivate type
        mockMvc.perform(patch("/api/v1/administration/lookups/types/" + type.getCode() + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.isActive").value(false));

        // Activate type
        mockMvc.perform(patch("/api/v1/administration/lookups/types/" + type.getCode() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isActive").value(true));

        // Add sub-type
        LookupSubTypeResponse subType = administrationLookupService.addSubType(
                type.getCode(),
                CreateLookupSubTypeRequest.builder().name("Toggleable Option").status("ACTIVE").build(),
                adminPrincipal);

        // Deactivate sub-type
        mockMvc.perform(patch("/api/v1/administration/lookups/sub-types/" + subType.getId() + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.data.isActive").value(false));

        // Activate sub-type
        mockMvc.perform(patch("/api/v1/administration/lookups/sub-types/" + subType.getId() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("8. Fixed types and options cannot be deactivated or deleted (return 400)")
    void testFixedTypeProtection() throws Exception {
        // 10001 is fixed (Bank User Status)
        mockMvc.perform(patch("/api/v1/administration/lookups/types/10001/deactivate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot deactivate system-fixed")));

        mockMvc.perform(delete("/api/v1/administration/lookups/types/10001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot delete system fixed")));

        // Options under 10001 (fixed) cannot be deactivated or deleted
        List<LookupSubTypeResponse> subTypes = administrationLookupService.getSubTypesByLookupTypeCode("10001");
        assertFalse(subTypes.isEmpty());
        Long fixedOptId = subTypes.get(0).getId();

        mockMvc.perform(patch("/api/v1/administration/lookups/sub-types/" + fixedOptId + "/deactivate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot deactivate option under system-fixed")));

        mockMvc.perform(delete("/api/v1/administration/lookups/sub-types/" + fixedOptId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot delete option under system fixed")));
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("9. Frontend can specify custom code when creating lookup type and sub-types")
    void testFrontendProvidedCodes() throws Exception {
        String customTypeCode = "CUSTOM_TYPE_" + System.currentTimeMillis();

        CreateLookupTypeRequest customTypeReq = CreateLookupTypeRequest.builder()
                .code(customTypeCode)
                .name("Frontend Provided Code Type")
                .status("ACTIVE")
                .isFixed(false)
                .build();

        mockMvc.perform(post("/api/v1/administration/lookups/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customTypeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value(customTypeCode));

        // Sub-type with frontend-provided code
        CreateLookupSubTypeRequest customSubTypeReq = CreateLookupSubTypeRequest.builder()
                .code("CODE_ABC")
                .name("Option ABC")
                .status("ACTIVE")
                .build();

        mockMvc.perform(post("/api/v1/administration/lookups/types/" + customTypeCode + "/sub-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customSubTypeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CODE_ABC"))
                .andExpect(jsonPath("$.data.subTypeCode").value("CODE_ABC"));

        // Duplicate code under same parent should return 409 Conflict
        mockMvc.perform(post("/api/v1/administration/lookups/types/" + customTypeCode + "/sub-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customSubTypeReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
