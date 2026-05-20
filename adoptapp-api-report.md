# AdoptApp API Analysis Report

**Generated:** 2026-05-20
**Project:** AdoptApp Microservices Architecture
**Framework:** Spring Boot 3.4.5, Spring Cloud 2024.0.0, Java 21
**Database:** PostgreSQL with Flyway migrations

---

## 1. MICROSERVICES OVERVIEW

| # | Service | Port | Context Path | DB | Status |
|---|---------|------|-------------|-----|--------|
| 1 | user-service | 8081 | /user-app | users_db | Found |
| 2 | pet-service | 8082 | /pet-app | pet_db | Found |
| 3 | adoption-service | 8083 | /adoption-app | adoption_db | Found |
| 4 | notification-service | 8084 | /notification-app | notif_db | Found |
| 5 | health-service | 8085 | /health-app | health_db | Found |
| 6 | followup-service | 8086 | /followup-app | followup_db | Found |
| 7 | donation-service | 8090 | /donation-app | donation_db | Found |
| 8 | staff-service | 8091 | /staff-app | staff_db | Found |
| 9 | supply-service | 8092 | /supply-app | supply_db | Found |
| 10 | shelter-service | 8095 | /shelter-app | shelter_db | Found |

**Total Endpoints Found:** 64

---

## 2. ENDPOINTS BY SERVICE

### 2.1 User Service (8 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/users?status` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/users/by-id/{id}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/users/by-email/{email}` | Basic | authenticated |
| GET | `/users/by-email/{email}/auth` | Basic | ADMIN |
| GET | `/users/by-id/{id}/history` | Basic | ADMIN |
| POST | `/users` | Basic | ADOPTER, SHELTER_ADMIN, VOLUNTEER, VET, ADMIN |
| PUT | `/users/by-id/{id}` | Basic | @userSecurity.canEdit SpEL |
| DELETE | `/users/by-id/{id}` | Basic | ADMIN |

### 2.2 Shelter Service (6 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/shelters?status` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/shelters/by-id/{id}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/shelters/by-id/{id}/history` | Basic | ADMIN |
| POST | `/shelters?userId` | Basic | SHELTER_ADMIN, ADMIN |
| PUT | `/shelters/by-id/{id}?userId` | Basic | SHELTER_ADMIN, ADMIN |
| DELETE | `/shelters/by-id/{id}?userId` | Basic | SHELTER_ADMIN, ADMIN |

### 2.3 Pet Service (7 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/pets?status` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/pets/by-id/{id}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/pets/by-id/{id}/history` | Basic | ADMIN |
| GET | `/pets/by-id/{id}/health` | Basic | authenticated |
| POST | `/pets` | Basic | ADOPTER, SHELTER_ADMIN, VOLUNTEER, ADMIN |
| PUT | `/pets/by-id/{id}` | Basic | SHELTER_ADMIN, VOLUNTEER, ADMIN |
| DELETE | `/pets/by-id/{id}` | Basic | ADMIN |

### 2.4 Adoption Service (6 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/adoptions?status` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/adoptions/by-id/{id}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/adoptions/by-id/{id}/history` | Basic | authenticated |
| POST | `/adoptions` | Basic | SHELTER_ADMIN, ADMIN |
| PUT | `/adoptions/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |
| DELETE | `/adoptions/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |

### 2.5 Health Service (6 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/health?vaccinationStatus&sterilizationStatus` | Basic | VET, SHELTER_ADMIN, ADMIN |
| GET | `/health/by-id/{id}` | Basic | VET, SHELTER_ADMIN, ADMIN |
| GET | `/health/by-id/{id}/history` | Basic | authenticated |
| POST | `/health` | Basic | VET, SHELTER_ADMIN, ADMIN |
| PUT | `/health/by-id/{id}` | Basic | VET, SHELTER_ADMIN, ADMIN |
| DELETE | `/health/by-id/{id}` | Basic | VET, SHELTER_ADMIN, ADMIN |

### 2.6 FollowUp Service (6 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/followups?status` | Basic | SHELTER_ADMIN, ADMIN, VOLUNTEER |
| GET | `/followups/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN, VOLUNTEER |
| GET | `/followups/by-id/{id}/history` | Basic | ADMIN |
| POST | `/followups` | Basic | SHELTER_ADMIN, ADMIN |
| PUT | `/followups/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |
| DELETE | `/followups/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |

### 2.7 Staff Service (6 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/staff?status` | Basic | SHELTER_ADMIN, ADMIN |
| GET | `/staff/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |
| GET | `/staff/by-id/{id}/history` | Basic | ADMIN |
| POST | `/staff` | Basic | SHELTER_ADMIN, ADMIN |
| PUT | `/staff/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |
| DELETE | `/staff/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |

### 2.8 Donation Service (6 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/donations?status` | Basic | ADMIN, SHELTER_ADMIN |
| GET | `/donations/by-id/{id}` | Basic | ADMIN, SHELTER_ADMIN |
| GET | `/donations/by-id/{id}/history` | Basic | ADMIN |
| POST | `/donations` | Basic | ADMIN |
| PUT | `/donations/by-id/{id}` | Basic | ADMIN |
| DELETE | `/donations/by-id/{id}` | Basic | ADMIN |

### 2.9 Supply Service (7 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/supplies?status` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/supplies/by-id/{id}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/supplies/shelter/{shelterId}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/supplies/by-id/{id}/history` | Basic | ADMIN |
| POST | `/supplies` | Basic | ADMIN, SHELTER_ADMIN |
| PUT | `/supplies/by-id/{id}` | Basic | ADMIN, SHELTER_ADMIN |
| DELETE | `/supplies/by-id/{id}` | Basic | ADMIN, SHELTER_ADMIN |

### 2.10 Notification Service (5 endpoints)

| Method | Path | Auth | Roles |
|--------|------|------|-------|
| GET | `/notifications?status` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| GET | `/notifications/by-id/{id}` | Basic | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| POST | `/notifications` | Basic | SHELTER_ADMIN, ADMIN |
| PUT | `/notifications/by-id/{id}` | Basic | SHELTER_ADMIN, ADMIN |
| DELETE | `/notifications/by-id/{id}` | Basic | ADMIN |

---

## 3. ENUMS REFERENCE

| Enum | Values |
|------|--------|
| User.Role | ADOPTER, SHELTER_ADMIN, VOLUNTEER, VET, ADMIN |
| UserStatus | ACTIVE, INACTIVE, SUSPENDED |
| PetStatus | AVAILABLE, NOT_AVAILABLE |
| VaccinationStatus | VACCINATED, NOT_VACCINATED |
| SterilizationStatus | STERILIZED, NOT_STERILIZED |
| AdoptionStatus | PENDING, APPROVED, REJECTED |
| NotificationStatus | SENT, PENDING, FAILED |
| FollowUpStatus | PENDING, COMPLETED, CANCELLED |
| DonationStatus | PENDING, COMPLETED, CANCELLED |
| ShelterStatus | ACTIVE, INACTIVE, DELETED |
| StaffStatus | ACTIVE, INACTIVE, SUSPENDED |
| StaffPosition | VETERINARIAN, CARETAKER, VOLUNTEER, ADMINISTRATOR, MANAGER, CLEANER, SECURITY, OTHER |
| SupplyStatus | AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED |
| SupplyCategory | FOOD, MEDICINE, TOYS, CLEANING, EQUIPMENT, OTHER |

---

## 4. INTER-SERVICE DEPENDENCIES (Feign Clients)

```
user-service --> notification-service (POST /notifications)

pet-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}, GET /users/by-email/{email}/auth)
pet-service --> shelter-service (GET /shelters/by-id/{id})
pet-service --> notification-service (POST /notifications)
pet-service --> health-service (POST /health, GET /health/by-id/{id}, PUT /health/by-id/{id}, DELETE /health/by-id/{id})

adoption-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
adoption-service --> pet-service (GET /pets/by-id/{id})
adoption-service --> shelter-service (GET /shelters/by-id/{id})
adoption-service --> followup-service (POST /followups)
adoption-service --> notification-service (POST /notifications x2)

health-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
health-service --> pet-service (GET /pets/by-id/{id})
health-service --> notification-service (POST /notifications)

followup-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
followup-service --> pet-service (GET /pets/by-id/{id})
followup-service --> notification-service (POST /notifications)

donation-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
donation-service --> shelter-service (GET /shelters/by-id/{id})
donation-service --> notification-service (POST /notifications)

staff-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
staff-service --> shelter-service (GET /shelters/by-id/{id})
staff-service --> notification-service (POST /notifications)

supply-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
supply-service --> shelter-service (GET /shelters/by-id/{id})
supply-service --> notification-service (POST /notifications)

shelter-service --> user-service (GET /users/by-id/{id}, GET /users/by-email/{email}/auth)
shelter-service --> notification-service (POST /notifications)

notification-service --> user-service (GET /users/by-email/{email}/auth)
```

**Total Feign Clients:** 29

---

## 5. CORRECT EXECUTION ORDER FOR TESTING

Based on inter-service dependencies, the correct order for testing is:

1. **user-service** (8081) - No external dependencies for CRUD
2. **shelter-service** (8095) - Depends on user-service for userId
3. **pet-service** (8082) - Depends on user-service, shelter-service, health-service
4. **adoption-service** (8083) - Depends on user-service, pet-service, shelter-service, followup-service
5. **health-service** (8085) - Depends on user-service, pet-service
6. **followup-service** (8086) - Depends on user-service, pet-service
7. **staff-service** (8091) - Depends on user-service, shelter-service
8. **donation-service** (8090) - Depends on user-service, shelter-service
9. **supply-service** (8092) - Depends on user-service, shelter-service
10. **notification-service** (8084) - Depends on user-service

---

## 6. SECURITY ANALYSIS

### Global Security Pattern
- **Authentication:** HTTP Basic (stateless)
- **Password Encoding:** BCrypt
- **CSRF:** Disabled
- **Session Management:** STATELESS
- **Method Security:** @EnableMethodSecurity with @PreAuthorize

### Critical Security Observations

| Issue | Severity | Details |
|-------|----------|---------|
| PUT /users uses SpEL | Medium | `@userSecurity.canEdit(#id, authentication)` - custom security expression, needs verification |
| POST /users permitAll | Low | Registration endpoint allows unregistered access (by design) |
| No rate limiting | Medium | No rate limiting detected on any endpoint |
| No CORS config visible | Low | CORS configuration not found in analyzed files |
| All services use Basic Auth | Info | Consider OAuth2/JWT for production |

---

## 7. VALIDATION ANALYSIS

### UserRequest
- `username`: @NotBlank, @Size(3-50)
- `name`: @NotBlank, @Size(max=50)
- `surname`: @NotBlank, @Size(max=50)
- `email`: @NotBlank, @Email
- `password`: @NotBlank, @Size(6-100)
- `phone`: @NotBlank
- `country`: @NotBlank
- `city`: @NotBlank
- `street`: @NotBlank
- `homeNumber`: @NotBlank
- `postalCode`: @NotBlank
- `type`: @NotBlank
- `status`: @NotNull

### PetRequest
- `name`: @NotBlank, @Size(2-50)
- `species`: @NotBlank, @Size(max=50)
- `race`: @NotBlank, @Size(max=50)
- `age`: @NotNull, @Min(0)
- `size`: @NotBlank, @Size(max=50)
- `color`: @NotBlank, @Size(max=50)
- `personality`: @NotBlank, @Size(max=50)
- `fosterId`: @NotNull
- `vaccinated`: @NotNull
- `sterilized`: @NotNull
- `diseases`: @NotNull, @Size(max=255)
- `status`: @NotBlank, @Size(max=50)
- `shelterId`: optional

### AdoptionRequest
- `petId`: @NotNull
- `userId`: @NotNull
- `status`: @NotNull

### NotificationRequest
- `recipient`: @NotBlank
- `message`: @NotBlank
- `typeName`: @NotBlank
- `status`: @NotNull
- `userId`: optional

### HealthRequest
- `userId`: @NotNull
- `petId`: @NotNull
- `vaccinationStatus`: @NotNull
- `sterilizationStatus`: @NotNull
- `diseases`: @NotBlank

### FollowUpRequest
- `adopterName`: @NotBlank
- `petName`: @NotBlank
- `visitDate`: @NotNull
- `status`: @NotNull
- `userId`, `petId`, `adoptionId`, `comments`: optional

### DonationRequest
- `donorName`: @NotBlank
- `amount`: @NotNull, @DecimalMin(0.01)
- `description`: @NotBlank
- `userId`: @NotNull
- `shelterId`: @NotNull
- `status`: optional

### StaffRequest
- `userId`: @NotNull
- `shelterId`: @NotNull
- `position`: @NotNull
- `email`: @Email
- `phone`, `hireDate`, `status`: optional

### SupplyRequest
- `name`: @NotBlank
- `quantity`: @NotNull, @Min(0)
- `unit`: @NotBlank
- `category`: @NotBlank
- `shelterId`: @NotNull
- `userId`: @NotNull
- `minimumStock`: @Min(0)
- `status`: @NotNull
- `description`, `supplierName`: optional

### ShelterRequest
- `name`: @NotBlank
- `email`: @NotBlank, @Email
- `phone`, `description`, `status`: optional

---

## 8. POTENTIAL ISSUES & INCONSISTENCIES

### 8.1 Endpoint Inconsistencies

| Issue | Service | Details |
|-------|---------|---------|
| Missing PUT/DELETE for some relations | All | No PATCH endpoints found in any service |
| No search/filter endpoints | Most | Only basic status filter on GET ALL endpoints |
| Shelter userId as query param | shelter-service | POST/PUT/DELETE require userId as query param instead of in body |
| Pet health endpoint | pet-service | GET `/pets/by-id/{id}/health` calls health-service via Feign, not local data |

### 8.2 DTO Inconsistencies

| Issue | Details |
|-------|---------|
| Multiple NotificationRequest copies | Each service has its own NotificationRequest DTO copy instead of using shared-kernel |
| Multiple UserResponse copies | Each service has its own UserResponse DTO copy |
| Multiple ShelterResponse copies | Each service has its own ShelterResponse DTO copy |
| Multiple PetResponse copies | adoption-service, followup-service, health-service have their own PetResponse |

### 8.3 Security Concerns

| Issue | Severity | Details |
|-------|----------|---------|
| Pet POST allows ADOPTER role | Medium | ADOPTER can create pets, which might be unintended |
| Health DELETE allows VET | Low | VET can delete health records (may be intended) |
| No endpoint-level audit logging | Medium | History endpoints exist but no audit on write operations visible |

### 8.4 Missing Endpoints (Common REST patterns not found)

| Missing Endpoint | Expected Service |
|-----------------|------------------|
| PATCH (partial updates) | All services |
| Bulk operations | All services |
| Search with multiple filters | Most services |
| Pagination support | All GET ALL endpoints |
| Sorting support | All GET ALL endpoints |
| Export/Import | None |

---

## 9. FEIGN CLIENT CONFIGURATION

| Setting | Value |
|---------|-------|
| Connect Timeout | 2000ms |
| Read Timeout | 3000ms |
| Logger Level | BASIC |
| Circuit Breaker | Resilience4j (enabled on all except user-service) |
| Sliding Window Size | 10 |
| Failure Rate Threshold | 50% |
| Wait Duration in Open State | 30s |
| Permitted Calls in Half-Open | 3 |
| Auth Interceptor | Present on 9 services (all except user-service) |

---

## 10. EXCEPTION HANDLING

All services use shared-kernel exceptions:
- `BusinessException`
- `ForbiddenException`
- `RemoteServiceException`
- `ResourceNotFoundException`
- `UnauthorizedException`
- `ValidationException`

Global exception handlers (`@RestControllerAdvice`) present in all 10 services.

ErrorResponse format (from shared-kernel):
```json
{
    "timestamp": "...",
    "status": 400,
    "error": "Bad Request",
    "message": "...",
    "path": "...",
    "traceId": "...",
    "details": []
}
```

---

## 11. RECOMMENDATIONS

### High Priority
1. **Add pagination** to all GET ALL endpoints (Pageable support)
2. **Add sorting** to list endpoints
3. **Consolidate DTOs** in shared-kernel instead of duplicating across services
4. **Add rate limiting** to prevent abuse
5. **Consider JWT/OAuth2** instead of Basic Auth for production

### Medium Priority
6. **Add PATCH endpoints** for partial updates
7. **Add bulk operations** for batch processing
8. **Add multi-filter search** endpoints
9. **Review Pet POST role** - ADOPTER creating pets may need review
10. **Add request ID tracing** across Feign calls

### Low Priority
11. **Add CORS configuration** if frontend is separate
12. **Add API versioning** (/api/v1/...)
13. **Add OpenAPI/Swagger** documentation
14. **Add health check endpoints** (/actuator/health)
15. **Add metrics endpoints** for monitoring

---

## 12. SUMMARY

| Metric | Count |
|--------|-------|
| Microservices | 10 |
| Total REST Endpoints | 64 |
| Feign Clients | 29 |
| Entities | 22 |
| Enum Types | 14 |
| Request DTOs | 10 |
| Response DTOs | 20+ |
| Security Configs | 10 |
| Exception Handlers | 10 |
| Application Profiles per service | 5 (default, dev, prod, h2, postgres) |

**Collection Status:** COMPLETE - All 64 endpoints captured
**Import Status:** Ready for Postman, Bruno, Insomnia
**Auth:** Global Basic Auth configured

---

*Report generated from actual source code analysis. No endpoints were invented or assumed.*
