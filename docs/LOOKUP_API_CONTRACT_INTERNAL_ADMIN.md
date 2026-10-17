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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 2,
    "code" : "10002",
    "name" : "Designation",
    "description" : "Designation",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 12,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 10,
    "code" : "10010",
    "name" : "Toggleable Type 3b636241-2910-4db0-9790-3042e9a004e4",
    "description" : "Toggleable Type 3b636241-2910-4db0-9790-3042e9a004e4",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 1,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 11,
    "code" : "10011",
    "name" : "Server Generated Test Type 1792231891507",
    "description" : "Server Generated Test Type 1792231891507",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 2,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 12,
    "code" : "10012",
    "name" : "Parent For Concurrent SubTypes a96a067f-1565-4518-952e-f625ad92aadf",
    "description" : "Parent For Concurrent SubTypes a96a067f-1565-4518-952e-f625ad92aadf",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 20,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 13,
    "code" : "10013",
    "name" : "Parallel Lookup Type 1_585fab7b-532e-495f-a8bd-c10c1a68f11c",
    "description" : "Parallel Lookup Type 1_585fab7b-532e-495f-a8bd-c10c1a68f11c",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 14,
    "code" : "10014",
    "name" : "Parallel Lookup Type 2_d55c9588-732a-438b-b933-f9eb9092cdfe",
    "description" : "Parallel Lookup Type 2_d55c9588-732a-438b-b933-f9eb9092cdfe",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 15,
    "code" : "10015",
    "name" : "Parallel Lookup Type 3_afbf9866-c801-4508-915a-961cbe79fc49",
    "description" : "Parallel Lookup Type 3_afbf9866-c801-4508-915a-961cbe79fc49",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 16,
    "code" : "10016",
    "name" : "Parallel Lookup Type 4_2bd42474-5967-4d30-ade3-1c084ba60b95",
    "description" : "Parallel Lookup Type 4_2bd42474-5967-4d30-ade3-1c084ba60b95",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 17,
    "code" : "10017",
    "name" : "Parallel Lookup Type 5_66c19759-e2f1-4050-8b1b-f08968173eda",
    "description" : "Parallel Lookup Type 5_66c19759-e2f1-4050-8b1b-f08968173eda",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 18,
    "code" : "10018",
    "name" : "Parallel Lookup Type 6_407343dc-7d6e-4e53-9a7a-f11d2f0b327b",
    "description" : "Parallel Lookup Type 6_407343dc-7d6e-4e53-9a7a-f11d2f0b327b",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:31",
    "updatedAt" : "2026-10-17T15:41:31"
  }, {
    "id" : 19,
    "code" : "10019",
    "name" : "Parallel Lookup Type 7_b1305640-2e5c-4ed4-b1d6-913d50cbc849",
    "description" : "Parallel Lookup Type 7_b1305640-2e5c-4ed4-b1d6-913d50cbc849",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 20,
    "code" : "10020",
    "name" : "Parallel Lookup Type 8_19db4031-2e58-44e6-a09a-a74a099c6649",
    "description" : "Parallel Lookup Type 8_19db4031-2e58-44e6-a09a-a74a099c6649",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 21,
    "code" : "10021",
    "name" : "Parallel Lookup Type 10_644de031-d58b-492c-bd57-9eabdb082b97",
    "description" : "Parallel Lookup Type 10_644de031-d58b-492c-bd57-9eabdb082b97",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 22,
    "code" : "10022",
    "name" : "Parallel Lookup Type 9_bc743a4c-fde4-4857-9033-569ef611f1db",
    "description" : "Parallel Lookup Type 9_bc743a4c-fde4-4857-9033-569ef611f1db",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 23,
    "code" : "10023",
    "name" : "Parallel Lookup Type 11_6a2f4b06-dce7-4941-9460-a95f408e3f22",
    "description" : "Parallel Lookup Type 11_6a2f4b06-dce7-4941-9460-a95f408e3f22",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 24,
    "code" : "10024",
    "name" : "Parallel Lookup Type 12_7440db71-1a6d-413d-8993-a896f3a0cf2d",
    "description" : "Parallel Lookup Type 12_7440db71-1a6d-413d-8993-a896f3a0cf2d",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 25,
    "code" : "10025",
    "name" : "Parallel Lookup Type 14_75a6ec8f-6a98-4e24-a71e-17a7cf7b0990",
    "description" : "Parallel Lookup Type 14_75a6ec8f-6a98-4e24-a71e-17a7cf7b0990",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 26,
    "code" : "10026",
    "name" : "Parallel Lookup Type 15_879aed46-c9a4-4142-bac6-b0fc16133f87",
    "description" : "Parallel Lookup Type 15_879aed46-c9a4-4142-bac6-b0fc16133f87",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 27,
    "code" : "10027",
    "name" : "Parallel Lookup Type 13_dcb94093-d5ec-4046-999f-d1dc6009d8bb",
    "description" : "Parallel Lookup Type 13_dcb94093-d5ec-4046-999f-d1dc6009d8bb",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 28,
    "code" : "10028",
    "name" : "Parallel Lookup Type 16_22045310-49a1-41d3-9587-d40c73d76b7f",
    "description" : "Parallel Lookup Type 16_22045310-49a1-41d3-9587-d40c73d76b7f",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 29,
    "code" : "10029",
    "name" : "Parallel Lookup Type 17_cd5da14c-5213-4750-86b6-b138b4723b34",
    "description" : "Parallel Lookup Type 17_cd5da14c-5213-4750-86b6-b138b4723b34",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 30,
    "code" : "10030",
    "name" : "Parallel Lookup Type 18_45618bcd-82e1-4068-ba3b-3f27d2fc5409",
    "description" : "Parallel Lookup Type 18_45618bcd-82e1-4068-ba3b-3f27d2fc5409",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 32,
    "code" : "10031",
    "name" : "Parallel Lookup Type 19_79ab66cb-3029-4539-b89f-b506023af10f",
    "description" : "Parallel Lookup Type 19_79ab66cb-3029-4539-b89f-b506023af10f",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 31,
    "code" : "10032",
    "name" : "Parallel Lookup Type 20_485f162b-48dd-4441-bd43-5d040ebfb9a8",
    "description" : "Parallel Lookup Type 20_485f162b-48dd-4441-bd43-5d040ebfb9a8",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 33,
    "code" : "10033",
    "name" : "Unique Type Name 4e70ed9d-1210-46bd-9dcd-212a9d95b7f9",
    "description" : "Unique Type Name 4e70ed9d-1210-46bd-9dcd-212a9d95b7f9",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  }, {
    "id" : 34,
    "code" : "10034",
    "name" : "Parent For Dup Option 850e236f-92ef-42a4-8af8-13d332ae7086",
    "description" : "Parent For Dup Option 850e236f-92ef-42a4-8af8-13d332ae7086",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 1,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-17T15:41:32",
    "updatedAt" : "2026-10-17T15:41:32"
  } ],
  "timestamp" : "2026-10-17T15:41:33.6147723"
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    } ],
    "page" : 0,
    "size" : 2,
    "totalElements" : 3,
    "totalPages" : 2
  },
  "timestamp" : "2026-10-17T15:41:33.6476727"
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19",
    "subTypes" : [ {
      "id" : 24,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 25,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 26,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 27,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 28,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 29,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 30,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 31,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 32,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 33,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    } ]
  },
  "timestamp" : "2026-10-17T15:41:33.6606774"
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
  }, {
    "code" : "10",
    "description" : "Relationship Manager"
  }, {
    "code" : "11",
    "description" : "Compliance Officer"
  }, {
    "code" : "12",
    "description" : "Branch Manager"
  } ],
  "timestamp" : "2026-10-17T15:41:33.6730622"
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
  "isActive" : true,
  "description" : "Real Seeded Collateral Asset Type",
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-17T15:41:33.690272"
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
  "isActive" : true,
  "description" : "Real Seeded Collateral Asset Type",
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
  "timestamp" : "2026-10-17T15:41:33.7012675"
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-17T15:41:33.7463688"
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-17T15:41:33.7923427"
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-17T15:41:33.8078482"
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
  "timestamp" : "2026-10-17T15:41:33.8128461"
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
    "id" : 24,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 25,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 26,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 27,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 28,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 29,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 30,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 31,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 32,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  }, {
    "id" : 33,
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
    "createdAt" : "2026-10-17T15:41:19",
    "updatedAt" : "2026-10-17T15:41:19"
  } ],
  "timestamp" : "2026-10-17T15:41:33.8260638"
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
      "id" : 24,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    }, {
      "id" : 25,
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
      "createdAt" : "2026-10-17T15:41:19",
      "updatedAt" : "2026-10-17T15:41:19"
    } ],
    "page" : 0,
    "size" : 2,
    "totalElements" : 10,
    "totalPages" : 5
  },
  "timestamp" : "2026-10-17T15:41:33.838062"
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
    "id" : 98,
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33"
  },
  "timestamp" : "2026-10-17T15:41:33.8560461"
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
  "timestamp" : "2026-10-17T15:41:33.8777411"
}
```

---

## 15. Update Sub-Type

- **Method**: `PUT`
- **Path**: `/api/v1/administration/lookups/sub-types/98`
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
    "id" : 98,
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33"
  },
  "timestamp" : "2026-10-17T15:41:33.8960457"
}
```

---

## 16. Deactivate Sub-Type

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/sub-types/98/deactivate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type deactivated successfully",
  "data" : {
    "id" : 98,
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33"
  },
  "timestamp" : "2026-10-17T15:41:33.9100404"
}
```

---

## 17. Activate Sub-Type

- **Method**: `PATCH`
- **Path**: `/api/v1/administration/lookups/sub-types/98/activate`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type activated successfully",
  "data" : {
    "id" : 98,
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
    "createdAt" : "2026-10-17T15:41:33",
    "updatedAt" : "2026-10-17T15:41:33"
  },
  "timestamp" : "2026-10-17T15:41:33.975999"
}
```

---

## 18. Delete Sub-Type

- **Method**: `DELETE`
- **Path**: `/api/v1/administration/lookups/sub-types/98`
- **Headers**: `Authorization: Bearer <token>`
- **Status Code**: `200 OK`
- **Real Raw Response Body**:
```json
{
  "success" : true,
  "message" : "Lookup sub-type deleted successfully from Master DB",
  "timestamp" : "2026-10-17T15:41:34.005982"
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
  "timestamp" : "2026-10-17T15:41:34.028445"
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
  "timestamp" : "2026-10-17T15:41:34.0354113"
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
  "isActive" : true,
  "description" : "",
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
  "timestamp" : "2026-10-17T15:41:34.0440464"
}
```

---

