# ⚡ FitCore — Fitness Management Application

**Spring Boot 3 + Angular 17 | JWT Auth | Spring Security | Hibernate/JPA | H2 / PostgreSQL**

---

## 🗂 Project Structure

```
fitcore/
├── backend/          # Spring Boot 3 application
│   └── src/main/java/com/fitcore/
│       ├── config/       # Security, CORS, DataInitializer
│       ├── controller/   # REST Controllers (MVC layer)
│       ├── dto/          # Request / Response DTOs
│       ├── entity/       # JPA Entities (User, Workout, etc.)
│       ├── exception/    # Global error handling
│       ├── repository/   # Spring Data JPA repositories
│       ├── security/     # JWT filter + service
│       └── service/      # Business logic layer
│
└── frontend/         # Angular 17 standalone application
    └── src/app/
        ├── core/         # Auth service, HTTP interceptor, guards, API services
        ├── features/     # auth, dashboard, workouts, diet, admin
        └── shared/       # Models/interfaces
```

---

## 🚀 Quick Start (Local)

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+ and npm
- Angular CLI: `npm install -g @angular/cli`

---

### 1. Start the Backend

```bash
cd fitcore/backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8080** with the `dev` profile (H2 in-memory DB).

**H2 Console**: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:fitcoredb`
- Username: `sa` | Password: *(empty)*

**Demo accounts** (seeded automatically on first start):
| Role  | Email                | Password  |
|-------|----------------------|-----------|
| Admin | admin@fitcore.com    | admin123  |
| User  | alex@example.com     | user123   |
| User  | maria@example.com    | user123   |

---

### 2. Start the Frontend

```bash
cd fitcore/frontend
npm install
ng serve
```

Frontend runs on **http://localhost:4200**

---

## 🔐 API Endpoints

### Auth (public)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |

### Workouts (GET is public, POST/PUT need USER+, DELETE needs ADMIN)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/workouts` | List with filters + pagination |
| GET | `/api/workouts/{id}` | Get by ID |
| POST | `/api/workouts` | Create workout |
| PUT | `/api/workouts/{id}` | Update workout |
| DELETE | `/api/workouts/{id}` | Delete (ADMIN only) |
| GET | `/api/workouts/stats` | Aggregated stats |

**Filter params**: `name`, `muscleGroup`, `difficulty`, `minDuration`, `maxDuration`, `page`, `size`, `sortBy`, `sortDir`

### Diet (USER/ADMIN)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/diet/plan?calories=2000&goal=maintenance` | Generate diet plan |
| GET | `/api/diet/nutrition?query=chicken` | Nutrition lookup |

### Admin (ADMIN only)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/users` | All users |
| PUT | `/api/admin/users/{id}/role?role=ADMIN` | Change role |
| PUT | `/api/admin/users/{id}/toggle` | Enable/disable |
| DELETE | `/api/admin/users/{id}` | Delete user |

### Profile
| Method | Path |
|--------|------|
| GET | `/api/user/me` |

---

## 🌐 Optional: Real Nutrition API

1. Register free at https://api-ninjas.com
2. Copy your API key
3. Set in `backend/src/main/resources/application.properties`:
   ```properties
   fitcore.api-ninjas.key=YOUR_REAL_KEY_HERE
   ```
4. Restart backend — nutrition lookup now uses live data

Without a key, the app uses realistic mock data (fully functional for demos).

---

## 🗄 Switch to PostgreSQL (Production)

1. Create database:
   ```sql
   CREATE DATABASE fitcoredb;
   CREATE USER fitcore_user WITH PASSWORD 'fitcore_pass';
   GRANT ALL PRIVILEGES ON DATABASE fitcoredb TO fitcore_user;
   ```

2. Run with prod profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

3. For prod, change `ddl-auto` from `validate` to `create` on first run, then switch back.

---

## 🏗 Architecture & Design Patterns

### MVC Layering
```
Request → Controller → Service → Repository → Entity → DB
                ↓
           DTO mapping
                ↓
           Response
```

- **Controllers** — handle HTTP, delegate to services. Zero business logic.
- **Services** — all business logic, transaction management (`@Transactional`)
- **Repositories** — data access via Spring Data JPA + custom JPQL queries
- **Entities** — JPA domain model with Hibernate lifecycle hooks (`@PrePersist`, `@PreUpdate`)

### Design Patterns Used
| Pattern | Where |
|---------|-------|
| **Builder** | All entities and DTOs (`@Builder`) |
| **DTO / Mapper** | `WorkoutResponse.fromEntity()`, `UserResponse.fromEntity()` |
| **Repository** | `WorkoutRepository`, `UserRepository`, `UserWorkoutLogRepository` |
| **Factory Method** | `PageResponse.from(page)` |
| **Chain of Responsibility** | Spring Security filter chain |
| **Strategy** | `PasswordEncoder` (BCrypt implementation injected) |
| **Interceptor** | Angular `authInterceptor` — adds JWT to every HTTP request |

### Security
- **BCrypt** password hashing (strength 10)
- **JWT** stateless authentication 
- **Spring Security** with method-level `@PreAuthorize`
- **Role-Based Access Control**: VISITOR (unauthenticated), USER, ADMIN

### Additional Features (assignment requirements)
- ✅ **Filtering** — by name, muscle group, difficulty, duration range
- ✅ **Sorting** — by name, date, duration, calories (asc/desc)
- ✅ **Pagination** — page/size params, page navigation in UI

---

## 📊 Entity-Relationship Summary

```
users (id, firstName, lastName, email, password[BCrypt], role, createdAt, enabled)
   |
   |── workouts (id, name, description, muscleGroup, difficulty, durationMinutes,
   |             caloriesBurned, createdAt, updatedAt, createdBy → users.id)
   |
   └── user_workout_logs (id, user_id, workout_id, logDate, notes, actualDurationMinutes)
```

---

## 🎓 Assignment Checklist

| Requirement | Status |
|-------------|--------|
| CRUD operations | ✅ Full CRUD on Workout entity |
| Role-based access control (3 roles) | ✅ VISITOR / USER / ADMIN |
| MVC architecture | ✅ Controller → Service → Repository |
| Repository implementation | ✅ Custom JPQL queries |
| Additional feature 1 — Filtering | ✅ Name, muscle group, difficulty, duration |
| Additional feature 2 — Sorting + Pagination | ✅ Multi-column sort + page navigation |
| Password hashing (bonus) | ✅ BCrypt |
| Spring Security integration (bonus) | ✅ JWT + `@PreAuthorize` |

---

## 🐛 Troubleshooting

**CORS errors** — ensure backend is running on 8081 and Angular on 4200.

**JWT expired** — log out and log back in. Token TTL is 24h.

**H2 data lost on restart** — expected; H2 is in-memory. DataInitializer re-seeds on each start.

**Angular build errors** — make sure you're using Angular CLI 17+:
```bash
ng version
npm install -g @angular/cli@17
```
