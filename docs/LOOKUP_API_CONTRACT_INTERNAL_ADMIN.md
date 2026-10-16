# Internal Admin Master Lookups API Contract (Allianza Panel)

> **CRITICAL NOTE ON BASE URL PATHS**:
> The backend controller maps both `/api/v1/administration/lookups` and `/api/v1/admin/lookups` for legacy compatibility.
> **The Frontend UI must use ONLY `/api/v1/administration/lookups`** for all requests.

> **STANDARD RESPONSE STRUCTURE**:
> All successful responses follow the standard platform `ApiResponse<T>` envelope:
> ```json
> {
>   "success": true,
>   "message": "Operation description",
>   "data": { ... },
>   "timestamp": "2026-10-16T14:30:00.123456"
> }
> ```
> All error responses follow the standard `ErrorResponse` envelope:
> ```json
> {
>   "success": false,
>   "status": 400 | 401 | 403 | 404 | 409,
>   "error": "BAD_REQUEST | CONFLICT | NOT_FOUND | Forbidden | Unauthorized",
>   "message": "Human-readable detail message",
>   "path": "/api/v1/administration/lookups/...",
>   "timestamp": "2026-10-16T14:30:00.123456"
> }
> ```

> Note on `createdBy`: When present, `createdBy` is stored as an integer/numeric ID (the User Principal ID), or `null` for legacy system seed rows.

---

## 1. List All Master Lookup Types (Legacy Plain List Mode)

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/types`
- **Headers**: `Authorization: Bearer <token>`
- **Query Params**: None (when no search, status, page, or size params are provided, returns plain array under `data`)
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Operation completed successfully",
  "data" : [ {
    "id" : 1,
    "code" : "10001",
    "name" : "Bank User Status",
    "description" : "Bank User Status",
    "status" : "ACTIVE",
    "isFixed" : true,
    "isActive" : true,
    "subTypeCount" : 5,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 2,
    "code" : "10002",
    "name" : "Designation",
    "description" : "Designation",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 9,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 3,
    "code" : "10003",
    "name" : "Role",
    "description" : "Role",
    "status" : "ACTIVE",
    "isFixed" : true,
    "isActive" : true,
    "subTypeCount" : 6,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 4,
    "code" : "10004",
    "name" : "Loan Type",
    "description" : "Loan Type",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 10,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 5,
    "code" : "10005",
    "name" : "Customer Type",
    "description" : "Customer Type",
    "status" : "ACTIVE",
    "isFixed" : true,
    "isActive" : true,
    "subTypeCount" : 2,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 6,
    "code" : "10006",
    "name" : "Individual Customer Sub Type",
    "description" : "Individual Customer Sub Type",
    "status" : "ACTIVE",
    "isFixed" : true,
    "isActive" : true,
    "subTypeCount" : 2,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 7,
    "code" : "10007",
    "name" : "Corporate Customer Sub Type",
    "description" : "Corporate Customer Sub Type",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 8,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 8,
    "code" : "10008",
    "name" : "Lookup Permissions",
    "description" : "Lookup Permissions",
    "status" : "ACTIVE",
    "isFixed" : true,
    "isActive" : true,
    "subTypeCount" : 7,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 9,
    "code" : "10009",
    "name" : "Access Permissions",
    "description" : "Access Permissions",
    "status" : "ACTIVE",
    "isFixed" : true,
    "isActive" : true,
    "subTypeCount" : 21,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 10,
    "code" : "10010",
    "name" : "Toggleable Type 1761e50c-9985-4d4b-b64f-14e9d77d5d27",
    "description" : "Toggleable Type 1761e50c-9985-4d4b-b64f-14e9d77d5d27",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 1,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 11,
    "code" : "10011",
    "name" : "Server Generated Test Type 1792140407297",
    "description" : "Server Generated Test Type 1792140407297",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 2,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 12,
    "code" : "10012",
    "name" : "Parent For Concurrent SubTypes c9379fa7-af96-454c-b370-c069b7f0e998",
    "description" : "Parent For Concurrent SubTypes c9379fa7-af96-454c-b370-c069b7f0e998",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 20,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 14,
    "code" : "10013",
    "name" : "Parallel Lookup Type 1_93dba1ff-7adf-4e9e-af28-c65abc94c156",
    "description" : "Parallel Lookup Type 1_93dba1ff-7adf-4e9e-af28-c65abc94c156",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 13,
    "code" : "10014",
    "name" : "Parallel Lookup Type 2_05df1285-3e00-4a42-813d-3b6096aeeb4c",
    "description" : "Parallel Lookup Type 2_05df1285-3e00-4a42-813d-3b6096aeeb4c",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 15,
    "code" : "10015",
    "name" : "Parallel Lookup Type 3_7fc87934-2cf6-48ef-84db-af5121a559e3",
    "description" : "Parallel Lookup Type 3_7fc87934-2cf6-48ef-84db-af5121a559e3",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 16,
    "code" : "10016",
    "name" : "Parallel Lookup Type 4_6afa8ce7-b730-4132-ba8f-69a78bca0c61",
    "description" : "Parallel Lookup Type 4_6afa8ce7-b730-4132-ba8f-69a78bca0c61",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 17,
    "code" : "10017",
    "name" : "Parallel Lookup Type 6_d7ebef1f-8b18-4948-afcb-d7b7a30e6b54",
    "description" : "Parallel Lookup Type 6_d7ebef1f-8b18-4948-afcb-d7b7a30e6b54",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 18,
    "code" : "10018",
    "name" : "Parallel Lookup Type 5_0c06b77f-f109-4971-addf-e31010405387",
    "description" : "Parallel Lookup Type 5_0c06b77f-f109-4971-addf-e31010405387",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 19,
    "code" : "10019",
    "name" : "Parallel Lookup Type 7_4b5c65fa-8f83-4e87-a6f7-34c122b921fd",
    "description" : "Parallel Lookup Type 7_4b5c65fa-8f83-4e87-a6f7-34c122b921fd",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 20,
    "code" : "10020",
    "name" : "Parallel Lookup Type 8_743e7805-d1f9-4b0a-a4fc-55c19f185e08",
    "description" : "Parallel Lookup Type 8_743e7805-d1f9-4b0a-a4fc-55c19f185e08",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 21,
    "code" : "10021",
    "name" : "Parallel Lookup Type 9_60a93558-3bbf-448e-b639-9fbb40aa80d4",
    "description" : "Parallel Lookup Type 9_60a93558-3bbf-448e-b639-9fbb40aa80d4",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 22,
    "code" : "10022",
    "name" : "Parallel Lookup Type 10_e2336cad-e236-416d-ae3c-d8315b9fe049",
    "description" : "Parallel Lookup Type 10_e2336cad-e236-416d-ae3c-d8315b9fe049",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 23,
    "code" : "10023",
    "name" : "Parallel Lookup Type 11_e271fded-0d70-487a-be14-33f9994ba221",
    "description" : "Parallel Lookup Type 11_e271fded-0d70-487a-be14-33f9994ba221",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 24,
    "code" : "10024",
    "name" : "Parallel Lookup Type 12_7b95c057-d03b-405b-9a43-5f5de89cb059",
    "description" : "Parallel Lookup Type 12_7b95c057-d03b-405b-9a43-5f5de89cb059",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 25,
    "code" : "10025",
    "name" : "Parallel Lookup Type 13_a3a2c5b1-f481-4d19-96e6-6f0a01952d79",
    "description" : "Parallel Lookup Type 13_a3a2c5b1-f481-4d19-96e6-6f0a01952d79",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 26,
    "code" : "10026",
    "name" : "Parallel Lookup Type 14_e70688c1-7053-46c0-a985-759382de74eb",
    "description" : "Parallel Lookup Type 14_e70688c1-7053-46c0-a985-759382de74eb",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 27,
    "code" : "10027",
    "name" : "Parallel Lookup Type 15_6dd0ae3e-21f9-4632-ba6e-b277966f624c",
    "description" : "Parallel Lookup Type 15_6dd0ae3e-21f9-4632-ba6e-b277966f624c",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 30,
    "code" : "10028",
    "name" : "Parallel Lookup Type 17_0d9b6cc1-950b-4da8-a3b5-10bc727c2f58",
    "description" : "Parallel Lookup Type 17_0d9b6cc1-950b-4da8-a3b5-10bc727c2f58",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 28,
    "code" : "10029",
    "name" : "Parallel Lookup Type 18_43cd9db0-91ae-498d-b604-6e03f2ea3864",
    "description" : "Parallel Lookup Type 18_43cd9db0-91ae-498d-b604-6e03f2ea3864",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 29,
    "code" : "10030",
    "name" : "Parallel Lookup Type 16_a57285ae-0613-41f4-be98-0971c8f78068",
    "description" : "Parallel Lookup Type 16_a57285ae-0613-41f4-be98-0971c8f78068",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 31,
    "code" : "10031",
    "name" : "Parallel Lookup Type 19_02cce3a3-a44a-4ff5-ade5-aa36fcfa6602",
    "description" : "Parallel Lookup Type 19_02cce3a3-a44a-4ff5-ade5-aa36fcfa6602",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 32,
    "code" : "10032",
    "name" : "Parallel Lookup Type 20_d4195486-a5e4-4bd8-8a2f-a3d972f3c0a1",
    "description" : "Parallel Lookup Type 20_d4195486-a5e4-4bd8-8a2f-a3d972f3c0a1",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:47",
    "updatedAt" : "2026-10-16T14:16:47"
  }, {
    "id" : 33,
    "code" : "10033",
    "name" : "Unique Type Name 01229cbb-eeb0-4b21-9813-3c7cd4ff9365",
    "description" : "Unique Type Name 01229cbb-eeb0-4b21-9813-3c7cd4ff9365",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:48",
    "updatedAt" : "2026-10-16T14:16:48"
  }, {
    "id" : 34,
    "code" : "10034",
    "name" : "Parent For Dup Option 30130f5b-9ae0-468d-b14a-b98eb6ff6889",
    "description" : "Parent For Dup Option 30130f5b-9ae0-468d-b14a-b98eb6ff6889",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 1,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-16T14:16:48",
    "updatedAt" : "2026-10-16T14:16:48"
  } ],
  "timestamp" : "2026-10-16T14:16:48.9931619"
}
```

---

## 2. Search & Paginate Master Lookup Types

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/types?search=Customer&status=ACTIVE&page=0&size=2`
- **Headers**: `Authorization: Bearer <token>`
- **Query Params**:
  - `search` (optional): Case-insensitive match on code or description/name.
  - `status` (optional): `ACTIVE` or `INACTIVE`.
  - `page` (optional, default 0): 0-based page index.
  - `size` (optional, default 10, max 100): Page size.
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Operation completed successfully",
  "data" : {
    "items" : [ {
      "id" : 5,
      "code" : "10005",
      "name" : "Customer Type",
      "description" : "Customer Type",
      "status" : "ACTIVE",
      "isFixed" : true,
      "isActive" : true,
      "subTypeCount" : 2,
      "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 6,
      "code" : "10006",
      "name" : "Individual Customer Sub Type",
      "description" : "Individual Customer Sub Type",
      "status" : "ACTIVE",
      "isFixed" : true,
      "isActive" : true,
      "subTypeCount" : 2,
      "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    } ],
    "page" : 0,
    "size" : 2,
    "totalElements" : 3,
    "totalPages" : 2
  },
  "timestamp" : "2026-10-16T14:16:49.0305332"
}
```

---

## 3. Get Master Lookup Type by Code (Detail View with Sub-Types)

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/types/10004`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Operation completed successfully",
  "data" : {
    "id" : 4,
    "code" : "10004",
    "name" : "Loan Type",
    "description" : "Loan Type",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 10,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36",
    "subTypes" : [ {
      "id" : 21,
      "code" : "1",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "1",
      "name" : "Personal Loan",
      "description" : "Personal Loan",
      "subTypeDescription" : "Personal Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 1,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 22,
      "code" : "2",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "2",
      "name" : "Home Loan",
      "description" : "Home Loan",
      "subTypeDescription" : "Home Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 2,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 23,
      "code" : "3",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "3",
      "name" : "Vehicle Loan",
      "description" : "Vehicle Loan",
      "subTypeDescription" : "Vehicle Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 3,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 24,
      "code" : "4",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "4",
      "name" : "Gold Loan",
      "description" : "Gold Loan",
      "subTypeDescription" : "Gold Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 4,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 25,
      "code" : "5",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "5",
      "name" : "Business Loan",
      "description" : "Business Loan",
      "subTypeDescription" : "Business Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 5,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 26,
      "code" : "6",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "6",
      "name" : "Loan Against Property (LAP)",
      "description" : "Loan Against Property (LAP)",
      "subTypeDescription" : "Loan Against Property (LAP)",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 6,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 27,
      "code" : "7",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "7",
      "name" : "Working Capital / Cash Credit",
      "description" : "Working Capital / Cash Credit",
      "subTypeDescription" : "Working Capital / Cash Credit",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 7,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 28,
      "code" : "8",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "8",
      "name" : "Professional Loan",
      "description" : "Professional Loan",
      "subTypeDescription" : "Professional Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 8,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 29,
      "code" : "9",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "9",
      "name" : "Corporate / Commercial Loan",
      "description" : "Corporate / Commercial Loan",
      "subTypeDescription" : "Corporate / Commercial Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 9,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 30,
      "code" : "10",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "10",
      "name" : "Educational Loan",
      "description" : "Educational Loan",
      "subTypeDescription" : "Educational Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 10,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    } ]
  },
  "timestamp" : "2026-10-16T14:16:49.0402452"
}
```

---

## 4. Get Master Lookup Options for Dropdowns

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/10002/options`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Operation completed successfully",
  "data" : [ {
    "code" : "1",
    "description" : "Clerk"
  }, {
    "code" : "2",
    "description" : "Officer"
  }, {
    "code" : "3",
    "description" : "Sr. Officer"
  }, {
    "code" : "4",
    "description" : "Asst. Manager"
  }, {
    "code" : "5",
    "description" : "Manager"
  }, {
    "code" : "6",
    "description" : "Dy. Manager"
  }, {
    "code" : "7",
    "description" : "Gen. Manager"
  }, {
    "code" : "8",
    "description" : "CEO"
  }, {
    "code" : "9",
    "description" : "Chairman"
  } ],
  "timestamp" : "2026-10-16T14:16:49.0648793"
}
```

---

## 5. Create Master Lookup Type (Add Lookup Screen)

- **Method**: `POST`
- **Path**: `/api/v1/administration/lookups/types`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "isFixed" : false,
  "description" : "Real Seeded Collateral Asset Type",
  "isActive" : true,
  "name" : "Real Seeded Collateral Asset Type"
}
```
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup type created successfully in Master DB",
  "data" : {
    "id" : 35,
    "code" : "10035",
    "name" : "Real Seeded Collateral Asset Type",
    "description" : "Real Seeded Collateral Asset Type",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-16T14:16:49.085902"
}
```

---

## 6. Create Master Lookup Type - Duplicate Name Error (409 Conflict)

- **Method**: `POST`
- **Path**: `/api/v1/administration/lookups/types`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "isFixed" : false,
  "description" : "Real Seeded Collateral Asset Type",
  "isActive" : true,
  "name" : "Real Seeded Collateral Asset Type"
}
```
- **Status Code**: `409 CONFLICT`
- **Real Raw Response Body**:
```json
{
  "success" : false,
  "status" : 409,
  "error" : "CONFLICT",
  "message" : "Lookup type already exists with name: Real Seeded Collateral Asset Type",
  "path" : "/api/v1/administration/lookups/types",
  "timestamp" : "2026-10-16T14:16:49.0938975"
}
```

---

## 7. Update Master Lookup Type

- **Method**: `PUT`
- **Path**: `/api/v1/administration/lookups/types/10035`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "description" : "Updated Real Seeded Collateral Asset Type",
  "name" : "Updated Real Seeded Collateral Asset Type"
}
```
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup type updated successfully in Master DB",
  "data" : {
    "id" : 35,
    "code" : "10035",
    "name" : "Updated Real Seeded Collateral Asset Type",
    "description" : "Updated Real Seeded Collateral Asset Type",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-16T14:16:49.1203105"
}
```

---

## 8. Deactivate Master Lookup Type

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/types/10035/deactivate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup type deactivated successfully",
  "data" : {
    "id" : 35,
    "code" : "10035",
    "name" : "Updated Real Seeded Collateral Asset Type",
    "description" : "Updated Real Seeded Collateral Asset Type",
    "status" : "INACTIVE",
    "isFixed" : false,
    "isActive" : false,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-16T14:16:49.1322992"
}
```

---

## 9. Activate Master Lookup Type

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/types/10035/activate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup type activated successfully",
  "data" : {
    "id" : 35,
    "code" : "10035",
    "name" : "Updated Real Seeded Collateral Asset Type",
    "description" : "Updated Real Seeded Collateral Asset Type",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-16T14:16:49.1515555"
}
```

---

## 10. Deactivate Fixed Type - Error (400 Bad Request)

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/types/10001/deactivate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `400 BAD_REQUEST`
- **Real Raw Response Body**:
```json
{
  "success" : false,
  "status" : 400,
  "error" : "CANNOT_DEACTIVATE_FIXED",
  "message" : "Cannot deactivate system-fixed lookup type: 10001",
  "path" : "/api/v1/administration/lookups/types/10001/deactivate",
  "timestamp" : "2026-10-16T14:16:49.1655199"
}
```

---

## 11. List Sub-Types for a Type (Plain List Mode)

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/types/10004/sub-types`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Operation completed successfully",
  "data" : [ {
    "id" : 21,
    "code" : "1",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "1",
    "name" : "Personal Loan",
    "description" : "Personal Loan",
    "subTypeDescription" : "Personal Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 1,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 22,
    "code" : "2",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "2",
    "name" : "Home Loan",
    "description" : "Home Loan",
    "subTypeDescription" : "Home Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 2,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 23,
    "code" : "3",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "3",
    "name" : "Vehicle Loan",
    "description" : "Vehicle Loan",
    "subTypeDescription" : "Vehicle Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 3,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 24,
    "code" : "4",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "4",
    "name" : "Gold Loan",
    "description" : "Gold Loan",
    "subTypeDescription" : "Gold Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 4,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 25,
    "code" : "5",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "5",
    "name" : "Business Loan",
    "description" : "Business Loan",
    "subTypeDescription" : "Business Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 5,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 26,
    "code" : "6",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "6",
    "name" : "Loan Against Property (LAP)",
    "description" : "Loan Against Property (LAP)",
    "subTypeDescription" : "Loan Against Property (LAP)",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 6,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 27,
    "code" : "7",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "7",
    "name" : "Working Capital / Cash Credit",
    "description" : "Working Capital / Cash Credit",
    "subTypeDescription" : "Working Capital / Cash Credit",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 7,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 28,
    "code" : "8",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "8",
    "name" : "Professional Loan",
    "description" : "Professional Loan",
    "subTypeDescription" : "Professional Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 8,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 29,
    "code" : "9",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "9",
    "name" : "Corporate / Commercial Loan",
    "description" : "Corporate / Commercial Loan",
    "subTypeDescription" : "Corporate / Commercial Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 9,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  }, {
    "id" : 30,
    "code" : "10",
    "typeCode" : "10004",
    "lookupTypeCode" : "10004",
    "typeDescription" : "Loan Type",
    "subTypeCode" : "10",
    "name" : "Educational Loan",
    "description" : "Educational Loan",
    "subTypeDescription" : "Educational Loan",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 10,
    "createdAt" : "2026-10-16T14:16:36",
    "updatedAt" : "2026-10-16T14:16:36"
  } ],
  "timestamp" : "2026-10-16T14:16:49.1851102"
}
```

---

## 12. Search & Paginate Sub-Types for a Type

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/types/10004/sub-types?page=0&size=2`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Operation completed successfully",
  "data" : {
    "items" : [ {
      "id" : 21,
      "code" : "1",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "1",
      "name" : "Personal Loan",
      "description" : "Personal Loan",
      "subTypeDescription" : "Personal Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 1,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    }, {
      "id" : 22,
      "code" : "2",
      "typeCode" : "10004",
      "lookupTypeCode" : "10004",
      "typeDescription" : "Loan Type",
      "subTypeCode" : "2",
      "name" : "Home Loan",
      "description" : "Home Loan",
      "subTypeDescription" : "Home Loan",
      "status" : "ACTIVE",
      "isFixed" : false,
      "isActive" : true,
      "displayOrder" : 2,
      "createdAt" : "2026-10-16T14:16:36",
      "updatedAt" : "2026-10-16T14:16:36"
    } ],
    "page" : 0,
    "size" : 2,
    "totalElements" : 10,
    "totalPages" : 5
  },
  "timestamp" : "2026-10-16T14:16:49.2001008"
}
```

---

## 13. Add Sub-Type / Option to Master Lookup Type

- **Method**: `POST`
- **Path**: `/api/v1/administration/lookups/types/10035/sub-types`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "isFixed" : false,
  "displayOrder" : 1,
  "subTypeDescription" : "Residential Real Estate",
  "isActive" : true,
  "name" : "Residential Real Estate"
}
```
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type created successfully in Master DB",
  "data" : {
    "id" : 95,
    "code" : "1",
    "typeCode" : "10035",
    "lookupTypeCode" : "10035",
    "typeDescription" : "Updated Real Seeded Collateral Asset Type",
    "subTypeCode" : "1",
    "name" : "Residential Real Estate",
    "description" : "Residential Real Estate",
    "subTypeDescription" : "Residential Real Estate",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 1,
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49"
  },
  "timestamp" : "2026-10-16T14:16:49.2712766"
}
```

---

## 14. Add Sub-Type - Duplicate Name Error (409 Conflict)

- **Method**: `POST`
- **Path**: `/api/v1/administration/lookups/types/10035/sub-types`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "isFixed" : false,
  "subTypeDescription" : "Residential Real Estate",
  "isActive" : true,
  "name" : "Residential Real Estate"
}
```
- **Status Code**: `409 CONFLICT`
- **Real Raw Response Body**:
```json
{
  "success" : false,
  "status" : 409,
  "error" : "CONFLICT",
  "message" : "Lookup sub-type already exists with name 'Residential Real Estate' under type 10035",
  "path" : "/api/v1/administration/lookups/types/10035/sub-types",
  "timestamp" : "2026-10-16T14:16:49.2857518"
}
```

---

## 15. Update Sub-Type

- **Method**: `PUT`
- **Path**: `/api/v1/administration/lookups/sub-types/95`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "displayOrder" : 2,
  "subTypeDescription" : "Updated Residential Real Estate",
  "name" : "Updated Residential Real Estate"
}
```
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type updated successfully in Master DB",
  "data" : {
    "id" : 95,
    "code" : "1",
    "typeCode" : "10035",
    "lookupTypeCode" : "10035",
    "typeDescription" : "Updated Real Seeded Collateral Asset Type",
    "subTypeCode" : "1",
    "name" : "Updated Residential Real Estate",
    "description" : "Updated Residential Real Estate",
    "subTypeDescription" : "Updated Residential Real Estate",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 2,
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49"
  },
  "timestamp" : "2026-10-16T14:16:49.3241137"
}
```

---

## 16. Deactivate Sub-Type

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/sub-types/95/deactivate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type deactivated successfully",
  "data" : {
    "id" : 95,
    "code" : "1",
    "typeCode" : "10035",
    "lookupTypeCode" : "10035",
    "typeDescription" : "Updated Real Seeded Collateral Asset Type",
    "subTypeCode" : "1",
    "name" : "Updated Residential Real Estate",
    "description" : "Updated Residential Real Estate",
    "subTypeDescription" : "Updated Residential Real Estate",
    "status" : "INACTIVE",
    "isFixed" : false,
    "isActive" : false,
    "displayOrder" : 2,
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49"
  },
  "timestamp" : "2026-10-16T14:16:49.333109"
}
```

---

## 17. Activate Sub-Type

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/sub-types/95/activate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type activated successfully",
  "data" : {
    "id" : 95,
    "code" : "1",
    "typeCode" : "10035",
    "lookupTypeCode" : "10035",
    "typeDescription" : "Updated Real Seeded Collateral Asset Type",
    "subTypeCode" : "1",
    "name" : "Updated Residential Real Estate",
    "description" : "Updated Residential Real Estate",
    "subTypeDescription" : "Updated Residential Real Estate",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "displayOrder" : 2,
    "createdAt" : "2026-10-16T14:16:49",
    "updatedAt" : "2026-10-16T14:16:49"
  },
  "timestamp" : "2026-10-16T14:16:49.3401033"
}
```

---

## 18. Delete Sub-Type

- **Method**: `DELETE`
- **Path**: `/api/v1/administration/lookups/sub-types/95`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type deleted successfully from Master DB",
  "timestamp" : "2026-10-16T14:16:49.3842108"
}
```

---

## 19. Delete Master Lookup Type

- **Method**: `DELETE`
- **Path**: `/api/v1/administration/lookups/types/10035`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup type deleted successfully from Master DB",
  "timestamp" : "2026-10-16T14:16:49.4029134"
}
```

---

## 20. Not Found Error (404 Not Found)

- **Method**: `GET`
- **Path**: `/api/v1/administration/lookups/types/99999`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `404 NOT_FOUND`
- **Real Raw Response Body**:
```json
{
  "success" : false,
  "status" : 404,
  "error" : "Not Found",
  "message" : "Master lookup type not found with code: 99999",
  "path" : "/api/v1/administration/lookups/types/99999",
  "timestamp" : "2026-10-16T14:16:49.4079119"
}
```

---

## 21. Validation Error (400 Bad Request)

- **Method**: `POST`
- **Path**: `/api/v1/administration/lookups/types`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Body**:
```json
{
  "status" : "ACTIVE",
  "isFixed" : false,
  "description" : "",
  "isActive" : true,
  "name" : ""
}
```
- **Status Code**: `400 BAD_REQUEST`
- **Real Raw Response Body**:
```json
{
  "success" : false,
  "status" : 400,
  "error" : "VALIDATION_FAILED",
  "message" : "Request validation failed",
  "path" : "/api/v1/administration/lookups/types",
  "validationErrors" : {
    "name" : "Lookup type name is required"
  },
  "timestamp" : "2026-10-16T14:16:49.4342883"
}
```

---

