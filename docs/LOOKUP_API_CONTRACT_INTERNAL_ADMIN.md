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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
  }, {
    "id" : 10,
    "code" : "10010",
    "name" : "Toggleable Type b79d8256-3cab-4665-b0b4-afc5e2b2e9fc",
    "description" : "Toggleable Type b79d8256-3cab-4665-b0b4-afc5e2b2e9fc",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 1,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:32",
    "updatedAt" : "2026-10-19T13:04:32"
  }, {
    "id" : 11,
    "code" : "10011",
    "name" : "Server Generated Test Type 1792395272771",
    "description" : "Server Generated Test Type 1792395272771",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 2,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-19T13:04:32",
    "updatedAt" : "2026-10-19T13:04:32"
  }, {
    "id" : 12,
    "code" : "10012",
    "name" : "Parent For Concurrent SubTypes fd27cb08-c526-4e66-9199-23e5eb98500f",
    "description" : "Parent For Concurrent SubTypes fd27cb08-c526-4e66-9199-23e5eb98500f",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 20,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:32",
    "updatedAt" : "2026-10-19T13:04:32"
  }, {
    "id" : 16,
    "code" : "10013",
    "name" : "Parallel Lookup Type 4_c7204349-8f36-4eb7-8645-a7eb279fbe26",
    "description" : "Parallel Lookup Type 4_c7204349-8f36-4eb7-8645-a7eb279fbe26",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 14,
    "code" : "10014",
    "name" : "Parallel Lookup Type 2_3c3570bc-2759-4452-a8c1-7b202968ea93",
    "description" : "Parallel Lookup Type 2_3c3570bc-2759-4452-a8c1-7b202968ea93",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 13,
    "code" : "10015",
    "name" : "Parallel Lookup Type 5_c8f2e9c9-28e5-4b9f-82a3-4d148a1f3ca6",
    "description" : "Parallel Lookup Type 5_c8f2e9c9-28e5-4b9f-82a3-4d148a1f3ca6",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 15,
    "code" : "10016",
    "name" : "Parallel Lookup Type 1_14dbfc10-e053-4bdd-9c22-61d797ecfd72",
    "description" : "Parallel Lookup Type 1_14dbfc10-e053-4bdd-9c22-61d797ecfd72",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 18,
    "code" : "10017",
    "name" : "Parallel Lookup Type 3_09462a8e-3f90-43e9-b9c1-4d5ff3a82d7e",
    "description" : "Parallel Lookup Type 3_09462a8e-3f90-43e9-b9c1-4d5ff3a82d7e",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 19,
    "code" : "10018",
    "name" : "Parallel Lookup Type 7_c55bf157-f413-4d7d-ad75-0d07922a50c2",
    "description" : "Parallel Lookup Type 7_c55bf157-f413-4d7d-ad75-0d07922a50c2",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 32,
    "code" : "10019",
    "name" : "Parallel Lookup Type 12_454889e9-027b-4e62-b1b6-e6ad5c5351b9",
    "description" : "Parallel Lookup Type 12_454889e9-027b-4e62-b1b6-e6ad5c5351b9",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 17,
    "code" : "10020",
    "name" : "Parallel Lookup Type 13_1f1bd5bb-8724-4316-901b-ea7af99ba3d4",
    "description" : "Parallel Lookup Type 13_1f1bd5bb-8724-4316-901b-ea7af99ba3d4",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 20,
    "code" : "10021",
    "name" : "Parallel Lookup Type 9_1179e07d-373f-4fe5-b338-afaed7e239db",
    "description" : "Parallel Lookup Type 9_1179e07d-373f-4fe5-b338-afaed7e239db",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 21,
    "code" : "10022",
    "name" : "Parallel Lookup Type 10_22e53c9c-8112-4a50-8c0c-511b44d5f461",
    "description" : "Parallel Lookup Type 10_22e53c9c-8112-4a50-8c0c-511b44d5f461",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 22,
    "code" : "10023",
    "name" : "Parallel Lookup Type 11_e35e3e7f-5c20-4e5a-a021-8bd339e944ae",
    "description" : "Parallel Lookup Type 11_e35e3e7f-5c20-4e5a-a021-8bd339e944ae",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 23,
    "code" : "10024",
    "name" : "Parallel Lookup Type 14_2a68ebc7-ea32-46d4-bb8b-4a51af4f0822",
    "description" : "Parallel Lookup Type 14_2a68ebc7-ea32-46d4-bb8b-4a51af4f0822",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 24,
    "code" : "10025",
    "name" : "Parallel Lookup Type 15_4441256c-02b4-4502-b4d9-68de95e9f02b",
    "description" : "Parallel Lookup Type 15_4441256c-02b4-4502-b4d9-68de95e9f02b",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 25,
    "code" : "10026",
    "name" : "Parallel Lookup Type 16_b6c4fd3b-876f-445a-860b-e853d30ab3be",
    "description" : "Parallel Lookup Type 16_b6c4fd3b-876f-445a-860b-e853d30ab3be",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 26,
    "code" : "10027",
    "name" : "Parallel Lookup Type 17_923950dc-b844-4be2-84ff-97599588042d",
    "description" : "Parallel Lookup Type 17_923950dc-b844-4be2-84ff-97599588042d",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 28,
    "code" : "10028",
    "name" : "Parallel Lookup Type 18_0bc84819-0572-4da6-97cf-99610388faba",
    "description" : "Parallel Lookup Type 18_0bc84819-0572-4da6-97cf-99610388faba",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 27,
    "code" : "10029",
    "name" : "Parallel Lookup Type 19_6d493b61-1a96-4808-86fd-657957ff5106",
    "description" : "Parallel Lookup Type 19_6d493b61-1a96-4808-86fd-657957ff5106",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 29,
    "code" : "10030",
    "name" : "Parallel Lookup Type 20_bb6c5242-05ee-4b2c-af42-6d9cf47ed816",
    "description" : "Parallel Lookup Type 20_bb6c5242-05ee-4b2c-af42-6d9cf47ed816",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 30,
    "code" : "10031",
    "name" : "Parallel Lookup Type 6_9f84855f-3c7c-4e6d-8670-a9179033f56e",
    "description" : "Parallel Lookup Type 6_9f84855f-3c7c-4e6d-8670-a9179033f56e",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 31,
    "code" : "10032",
    "name" : "Parallel Lookup Type 8_23899a8d-e9cd-40fd-a613-4713625a2add",
    "description" : "Parallel Lookup Type 8_23899a8d-e9cd-40fd-a613-4713625a2add",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 33,
    "code" : "10033",
    "name" : "Unique Type Name 64755697-ed3c-464b-8c55-b74ccdcb69c4",
    "description" : "Unique Type Name 64755697-ed3c-464b-8c55-b74ccdcb69c4",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 0,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  }, {
    "id" : 34,
    "code" : "10034",
    "name" : "Parent For Dup Option ccf471cb-7223-489f-8ffb-368f973783ef",
    "description" : "Parent For Dup Option ccf471cb-7223-489f-8ffb-368f973783ef",
    "status" : "ACTIVE",
    "isFixed" : false,
    "isActive" : true,
    "subTypeCount" : 1,
    "permissions" : [ "101", "102", "103", "104", "105", "106", "107" ],
    "createdBy" : 1,
    "createdAt" : "2026-10-19T13:04:33",
    "updatedAt" : "2026-10-19T13:04:33"
  } ],
  "timestamp" : "2026-10-19T13:04:34.21375"
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
      "createdAt" : "2026-10-19T13:04:23",
      "updatedAt" : "2026-10-19T13:04:23"
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
      "createdAt" : "2026-10-19T13:04:23",
      "updatedAt" : "2026-10-19T13:04:23"
    } ],
    "page" : 0,
    "size" : 2,
    "totalElements" : 3,
    "totalPages" : 2
  },
  "timestamp" : "2026-10-19T13:04:34.2367414"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22",
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:23",
      "updatedAt" : "2026-10-19T13:04:23"
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
      "createdAt" : "2026-10-19T13:04:23",
      "updatedAt" : "2026-10-19T13:04:23"
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
      "createdAt" : "2026-10-19T13:04:23",
      "updatedAt" : "2026-10-19T13:04:23"
    } ]
  },
  "timestamp" : "2026-10-19T13:04:34.2467805"
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
  "timestamp" : "2026-10-19T13:04:34.2605676"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-19T13:04:34.2763562"
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
  "timestamp" : "2026-10-19T13:04:34.2861271"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-19T13:04:34.3131223"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-19T13:04:34.3248471"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34",
    "subTypes" : [ ]
  },
  "timestamp" : "2026-10-19T13:04:34.3394476"
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
  "timestamp" : "2026-10-19T13:04:34.3478443"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:22",
    "updatedAt" : "2026-10-19T13:04:22"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
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
    "createdAt" : "2026-10-19T13:04:23",
    "updatedAt" : "2026-10-19T13:04:23"
  } ],
  "timestamp" : "2026-10-19T13:04:34.3567104"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
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
      "createdAt" : "2026-10-19T13:04:22",
      "updatedAt" : "2026-10-19T13:04:22"
    } ],
    "page" : 0,
    "size" : 2,
    "totalElements" : 10,
    "totalPages" : 5
  },
  "timestamp" : "2026-10-19T13:04:34.3701375"
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
  "isActive" : true,
  "subTypeDescription" : "Residential Real Estate",
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34"
  },
  "timestamp" : "2026-10-19T13:04:34.3844358"
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
  "isActive" : true,
  "subTypeDescription" : "Residential Real Estate",
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
  "timestamp" : "2026-10-19T13:04:34.3937519"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34"
  },
  "timestamp" : "2026-10-19T13:04:34.4140371"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34"
  },
  "timestamp" : "2026-10-19T13:04:34.4252971"
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
    "createdAt" : "2026-10-19T13:04:34",
    "updatedAt" : "2026-10-19T13:04:34"
  },
  "timestamp" : "2026-10-19T13:04:34.4318536"
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
  "timestamp" : "2026-10-19T13:04:34.4566226"
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
  "timestamp" : "2026-10-19T13:04:34.4788744"
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
  "timestamp" : "2026-10-19T13:04:34.486489"
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
  "timestamp" : "2026-10-19T13:04:34.500255"
}
```

---

