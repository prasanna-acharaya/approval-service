# DSA Approval Flow - Final Integration Test Results (25-12-25)

## 1. Staging API (Additive-Only)
**Endpoint**: `POST /api/dsa/approval/stage`
**Description**: Adds new manager-product assignments for a DSA. Existing assignments are preserved and NOT modified.

### Request (Initial Stage)
```bash
curl -X POST http://localhost:8081/api/dsa/approval/stage \
-H "Content-Type: application/json" \
-d '{
    "dsaId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005",
    "items": [
        { "productType": "HOME_LOAN", "userId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003" }
    ]
}'
```

### Response
```text
Products staged successfully
```

---

## 2. Authorize API (Finalizing Approval)
**Endpoint**: `POST /api/dsa/approval/authorize`

### Request
```bash
curl -X POST http://localhost:8081/api/dsa/approval/authorize \
-H "Content-Type: application/json" \
-d '{
    "dsaId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005",
    "productType": "HOME_LOAN",
    "userId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003"
}'
```

### Response
```json
{
    "status": "APPROVED",
    "targetId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005",
    "createdAt": "2025-12-25T21:07:14.926"
}
```

---

## 3. Staging API (Adding Verification Record)
**Description**: Adding `VEHICLE_LOAN`. `HOME_LOAN` should remain approved.

### Request
```bash
curl -X POST http://localhost:8081/api/dsa/approval/stage \
-H "Content-Type: application/json" \
-d '{
    "dsaId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005",
    "items": [
        { "productType": "VEHICLE_LOAN", "userId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003" }
    ]
}'
```

---

## 4. Final Verify API (Post-Authorization & Incremental Stage)
**Endpoint**: `POST /api/dsa/approval/verify`
**Description**: Returns all records for a DSA. Note that `HOME_LOAN` is still authorized from Step 2.

### Request
```bash
curl -X POST http://localhost:8081/api/dsa/approval/verify \
-H "Content-Type: application/json" \
-d '{ "dsaId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005" }'
```

### Response
```json
[
    {
        "name": "HOME_LOAN",
        "value": 1,
        "approvedDate": "2025-12-25T21:07:14.92",
        "approverId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003"
    },
    {
        "name": "VEHICLE_LOAN",
        "value": 0,
        "approvedDate": null,
        "approverId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003"
    }
]
```

---

## 5. User Pending Approvals API
**Endpoint**: `GET /api/dsa/approval/pending/{userId}`
**Description**: Fetches all staged products assigned to a specific manager that are still pending authorization (`approvedAt` is null).

### Request
```bash
curl -X GET http://localhost:8081/api/dsa/approval/pending/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003
```

### Response
```json
[
    {
        "id": "694d4c64ace8aa618890f47c",
        "dsaId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380005",
        "userId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003",
        "productType": "VEHICLE_LOAN",
        "approvedAt": null,
        "runningFlowId": null
    },
    {
        "id": "694d5fb69a42740e6bc04a43",
        "dsaId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380007",
        "userId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380003",
        "productType": "LAP_LOAN",
        "approvedAt": null,
        "runningFlowId": null
    }
]
```
