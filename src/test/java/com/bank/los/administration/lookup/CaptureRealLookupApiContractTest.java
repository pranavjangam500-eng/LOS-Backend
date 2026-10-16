package com.bank.los.administration.lookup;

import com.bank.los.administration.lookup.dto.CreateLookupSubTypeRequest;
import com.bank.los.administration.lookup.dto.CreateLookupTypeRequest;
import com.bank.los.administration.lookup.dto.UpdateLookupSubTypeRequest;
import com.bank.los.administration.lookup.dto.UpdateLookupTypeRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CaptureRealLookupApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String prettyJson(String raw) {
        try {
            if (raw == null || raw.isBlank()) return "*(empty body)*";
            JsonNode tree = objectMapper.readTree(raw);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
        } catch (Exception e) {
            return raw;
        }
    }

    @Test
    @WithMockUser(username = "admin@losplatform.com", roles = {"INTERNAL_ADMIN"})
    @DisplayName("Generate docs/LOOKUP_API_CONTRACT_INTERNAL_ADMIN.md with authentic raw responses")
    void generateApiContractMarkdown() throws Exception {
        StringBuilder md = new StringBuilder();
        md.append("# Internal Admin Master Lookups API Contract (Allianza Panel)\n\n");
        md.append("> **CRITICAL NOTE ON BASE URL PATHS**:\n");
        md.append("> The backend controller maps both `/api/v1/administration/lookups` and `/api/v1/admin/lookups` for legacy compatibility.\n");
        md.append("> **The Frontend UI must use ONLY `/api/v1/administration/lookups`** for all requests.\n\n");
        md.append("> **STANDARD RESPONSE STRUCTURE**:\n");
        md.append("> All successful responses follow the standard platform `ApiResponse<T>` envelope:\n");
        md.append("> ```json\n");
        md.append("> {\n");
        md.append(">   \"success\": true,\n");
        md.append(">   \"message\": \"Operation description\",\n");
        md.append(">   \"data\": { ... },\n");
        md.append(">   \"timestamp\": \"2026-10-16T14:30:00.123456\"\n");
        md.append("> }\n");
        md.append("> ```\n");
        md.append("> All error responses follow the standard `ErrorResponse` envelope:\n");
        md.append("> ```json\n");
        md.append("> {\n");
        md.append(">   \"success\": false,\n");
        md.append(">   \"status\": 400 | 401 | 403 | 404 | 409,\n");
        md.append(">   \"error\": \"BAD_REQUEST | CONFLICT | NOT_FOUND | Forbidden | Unauthorized\",\n");
        md.append(">   \"message\": \"Human-readable detail message\",\n");
        md.append(">   \"path\": \"/api/v1/administration/lookups/...\",\n");
        md.append(">   \"timestamp\": \"2026-10-16T14:30:00.123456\"\n");
        md.append("> }\n");
        md.append("> ```\n\n");
        md.append("> Note on `createdBy`: When present, `createdBy` is stored as an integer/numeric ID (the User Principal ID), or `null` for legacy system seed rows.\n\n");
        md.append("---\n\n");

        // 1. GET /types (no params -> plain list)
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/types")).andReturn();
            md.append("## 1. List All Master Lookup Types (Legacy Plain List Mode)\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Query Params**: None (when no search, status, page, or size params are provided, returns plain array under `data`)\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 2. GET /types (paginated & search)
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/types")
                    .param("search", "Customer")
                    .param("status", "ACTIVE")
                    .param("page", "0")
                    .param("size", "2")).andReturn();
            md.append("## 2. Search & Paginate Master Lookup Types\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types?search=Customer&status=ACTIVE&page=0&size=2`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Query Params**:\n");
            md.append("  - `search` (optional): Case-insensitive match on code or description/name.\n");
            md.append("  - `status` (optional): `ACTIVE` or `INACTIVE`.\n");
            md.append("  - `page` (optional, default 0): 0-based page index.\n");
            md.append("  - `size` (optional, default 10, max 100): Page size.\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 3. GET /types/{code} (Detail endpoint)
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/types/10004")).andReturn();
            md.append("## 3. Get Master Lookup Type by Code (Detail View with Sub-Types)\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/10004`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 4. GET /{code}/options (Dropdown options)
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/10002/options")).andReturn();
            md.append("## 4. Get Master Lookup Options for Dropdowns\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/10002/options`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 5. POST /types (Create type)
        String createdTypeCode = "";
        {
            CreateLookupTypeRequest req = CreateLookupTypeRequest.builder()
                    .name("Real Seeded Collateral Asset Type")
                    .status("ACTIVE")
                    .isFixed(false)
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(post("/api/v1/administration/lookups/types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 5. Create Master Lookup Type (Add Lookup Screen)\n\n");
            md.append("- **Method**: `POST`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");

            JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
            if (node.has("data") && node.get("data").has("code")) {
                createdTypeCode = node.get("data").get("code").asText();
            }
        }

        // 6. POST /types with duplicate name -> 409 Conflict
        {
            CreateLookupTypeRequest req = CreateLookupTypeRequest.builder()
                    .name("Real Seeded Collateral Asset Type")
                    .status("ACTIVE")
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(post("/api/v1/administration/lookups/types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 6. Create Master Lookup Type - Duplicate Name Error (409 Conflict)\n\n");
            md.append("- **Method**: `POST`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" CONFLICT`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 7. PUT /types/{code} (Update type)
        if (!createdTypeCode.isEmpty()) {
            UpdateLookupTypeRequest req = UpdateLookupTypeRequest.builder()
                    .name("Updated Real Seeded Collateral Asset Type")
                    .status("ACTIVE")
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(put("/api/v1/administration/lookups/types/" + createdTypeCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 7. Update Master Lookup Type\n\n");
            md.append("- **Method**: `PUT`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/").append(createdTypeCode).append("`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 8. PATCH /types/{code}/deactivate
        if (!createdTypeCode.isEmpty()) {
            MvcResult res = mockMvc.perform(patch("/api/v1/administration/lookups/types/" + createdTypeCode + "/deactivate")).andReturn();
            md.append("## 8. Deactivate Master Lookup Type\n\n");
            md.append("- **Method**: `PATCH`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/").append(createdTypeCode).append("/deactivate`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 9. PATCH /types/{code}/activate
        if (!createdTypeCode.isEmpty()) {
            MvcResult res = mockMvc.perform(patch("/api/v1/administration/lookups/types/" + createdTypeCode + "/activate")).andReturn();
            md.append("## 9. Activate Master Lookup Type\n\n");
            md.append("- **Method**: `PATCH`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/").append(createdTypeCode).append("/activate`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 10. PATCH /types/10001/deactivate -> 400 Bad Request (Fixed type protection)
        {
            MvcResult res = mockMvc.perform(patch("/api/v1/administration/lookups/types/10001/deactivate")).andReturn();
            md.append("## 10. Deactivate Fixed Type - Error (400 Bad Request)\n\n");
            md.append("- **Method**: `PATCH`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/10001/deactivate`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" BAD_REQUEST`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 11. GET /types/{code}/sub-types (no params -> plain list)
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/types/10004/sub-types")).andReturn();
            md.append("## 11. List Sub-Types for a Type (Plain List Mode)\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/10004/sub-types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 12. GET /types/{code}/sub-types (paginated)
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/types/10004/sub-types")
                    .param("page", "0")
                    .param("size", "2")).andReturn();
            md.append("## 12. Search & Paginate Sub-Types for a Type\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/10004/sub-types?page=0&size=2`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 13. POST /types/{code}/sub-types (Add sub-type)
        Long createdSubTypeId = null;
        if (!createdTypeCode.isEmpty()) {
            CreateLookupSubTypeRequest req = CreateLookupSubTypeRequest.builder()
                    .name("Residential Real Estate")
                    .status("ACTIVE")
                    .displayOrder(1)
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(post("/api/v1/administration/lookups/types/" + createdTypeCode + "/sub-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 13. Add Sub-Type / Option to Master Lookup Type\n\n");
            md.append("- **Method**: `POST`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/").append(createdTypeCode).append("/sub-types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");

            JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString(StandardCharsets.UTF_8));
            if (node.has("data") && node.get("data").has("id")) {
                createdSubTypeId = node.get("data").get("id").asLong();
            }
        }

        // 14. POST duplicate sub-type name -> 409 Conflict
        if (!createdTypeCode.isEmpty()) {
            CreateLookupSubTypeRequest req = CreateLookupSubTypeRequest.builder()
                    .name("Residential Real Estate")
                    .status("ACTIVE")
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(post("/api/v1/administration/lookups/types/" + createdTypeCode + "/sub-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 14. Add Sub-Type - Duplicate Name Error (409 Conflict)\n\n");
            md.append("- **Method**: `POST`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/").append(createdTypeCode).append("/sub-types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" CONFLICT`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 15. PUT /sub-types/{id} (Update sub-type)
        if (createdSubTypeId != null) {
            UpdateLookupSubTypeRequest req = UpdateLookupSubTypeRequest.builder()
                    .name("Updated Residential Real Estate")
                    .status("ACTIVE")
                    .displayOrder(2)
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(put("/api/v1/administration/lookups/sub-types/" + createdSubTypeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 15. Update Sub-Type\n\n");
            md.append("- **Method**: `PUT`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/sub-types/").append(createdSubTypeId).append("`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 16. PATCH /sub-types/{id}/deactivate
        if (createdSubTypeId != null) {
            MvcResult res = mockMvc.perform(patch("/api/v1/administration/lookups/sub-types/" + createdSubTypeId + "/deactivate")).andReturn();
            md.append("## 16. Deactivate Sub-Type\n\n");
            md.append("- **Method**: `PATCH`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/sub-types/").append(createdSubTypeId).append("/deactivate`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 17. PATCH /sub-types/{id}/activate
        if (createdSubTypeId != null) {
            MvcResult res = mockMvc.perform(patch("/api/v1/administration/lookups/sub-types/" + createdSubTypeId + "/activate")).andReturn();
            md.append("## 17. Activate Sub-Type\n\n");
            md.append("- **Method**: `PATCH`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/sub-types/").append(createdSubTypeId).append("/activate`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 18. DELETE /sub-types/{id}
        if (createdSubTypeId != null) {
            MvcResult res = mockMvc.perform(delete("/api/v1/administration/lookups/sub-types/" + createdSubTypeId)).andReturn();
            md.append("## 18. Delete Sub-Type\n\n");
            md.append("- **Method**: `DELETE`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/sub-types/").append(createdSubTypeId).append("`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 19. DELETE /types/{code}
        if (!createdTypeCode.isEmpty()) {
            MvcResult res = mockMvc.perform(delete("/api/v1/administration/lookups/types/" + createdTypeCode)).andReturn();
            md.append("## 19. Delete Master Lookup Type\n\n");
            md.append("- **Method**: `DELETE`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/").append(createdTypeCode).append("`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" OK`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 20. GET /types/99999 -> 404 Not Found
        {
            MvcResult res = mockMvc.perform(get("/api/v1/administration/lookups/types/99999")).andReturn();
            md.append("## 20. Not Found Error (404 Not Found)\n\n");
            md.append("- **Method**: `GET`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types/99999`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" NOT_FOUND`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // 21. POST /types with blank name -> 400 Validation Error
        {
            CreateLookupTypeRequest req = CreateLookupTypeRequest.builder()
                    .name("")
                    .build();
            String reqJson = objectMapper.writeValueAsString(req);
            MvcResult res = mockMvc.perform(post("/api/v1/administration/lookups/types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reqJson)).andReturn();
            md.append("## 21. Validation Error (400 Bad Request)\n\n");
            md.append("- **Method**: `POST`\n");
            md.append("- **Path**: `/api/v1/administration/lookups/types`\n");
            md.append("- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`\n");
            md.append("- **Request Body**:\n```json\n").append(prettyJson(reqJson)).append("\n```\n");
            md.append("- **Status Code**: `").append(res.getResponse().getStatus()).append(" BAD_REQUEST`\n");
            md.append("- **Real Raw Response Body**:\n```json\n")
                    .append(prettyJson(res.getResponse().getContentAsString(StandardCharsets.UTF_8)))
                    .append("\n```\n\n---\n\n");
        }

        // Write to docs/LOOKUP_API_CONTRACT_INTERNAL_ADMIN.md
        File docFile = new File("docs/LOOKUP_API_CONTRACT_INTERNAL_ADMIN.md");
        try (FileWriter writer = new FileWriter(docFile, StandardCharsets.UTF_8)) {
            writer.write(md.toString());
        }
        System.out.println("LOOKUP_API_CONTRACT_INTERNAL_ADMIN.md generated successfully at: " + docFile.getAbsolutePath());
    }
}
