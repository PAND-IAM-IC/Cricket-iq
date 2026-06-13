# Cricket Intelligence API — Build Blueprint (v1)

> The roadmap tells you *what* and *why*. This doc gives you the concrete artifacts to
> start typing **now**: dependencies, infra, schema, config, and an ordered first-day
> checklist. Versions are current as of Spring Boot 3.x / Java 21.

---

## 0. Prerequisites (install once)

- **JDK 21** (Temurin/Adoptium) — verify: `java -version`  ✅ done
- **Docker + Docker Compose** — verify: `docker --version`
- **Maven** (or the `mvnw` wrapper Spring Initializr generates)
- IDE: VS Code and/or IntelliJ IDEA

---

## 1. Generate the project

Go to **https://start.spring.io** and select:

- Project: **Maven** · Language: **Java** · Spring Boot: **3.x (latest stable)**
- Group: `com.cricketiq` · Artifact: `cricket-iq` · Packaging: **Jar** · Java: **21**
- Dependencies: **Spring Web, Spring Data JPA, Spring Security, Validation,
  Spring Data Redis, Spring Boot Actuator, PostgreSQL Driver, Flyway Migration,
  Lombok, Testcontainers**

---

## 2. `pom.xml` — dependencies to add manually (Initializr misses these)

```xml
<!-- JWT (jjwt 0.12.x) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- OpenAPI / Swagger UI for Spring Boot 3 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>

<!-- Flyway: Postgres support module -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>

<!-- Test: Testcontainers Postgres + Spring Security test -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 3. `docker-compose.yml` — Postgres + Redis (repo root)

```yaml
services:
  postgres:
    image: postgres:16
    container_name: cricketiq-postgres
    environment:
      POSTGRES_DB: cricketiq
      POSTGRES_USER: cricketiq
      POSTGRES_PASSWORD: cricketiq
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7
    container_name: cricketiq-redis
    ports:
      - "6379:6379"

volumes:
  pgdata:
```

---

## 4. `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cricketiq
    username: cricketiq
    password: cricketiq
  jpa:
    hibernate:
      ddl-auto: validate        # Flyway owns the schema, NOT Hibernate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  flyway:
    enabled: true
  data:
    redis:
      host: localhost
      port: 6379

app:
  jwt:
    secret: change-me-to-a-long-random-base64-secret-at-least-32-bytes
    expiration-ms: 3600000

springdoc:
  swagger-ui:
    path: /swagger-ui.html

logging:
  level:
    org.hibernate.SQL: debug
```

---

## 5. First migration — `src/main/resources/db/migration/V1__init.sql`

```sql
CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE team (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE player (
    id            BIGSERIAL PRIMARY KEY,
    registry_id   VARCHAR(64) UNIQUE,
    name          VARCHAR(160) NOT NULL,
    batting_style VARCHAR(40),
    bowling_style VARCHAR(40),
    primary_role  VARCHAR(40)
);

CREATE TABLE cricket_match (
    id             BIGSERIAL PRIMARY KEY,
    match_date     DATE,
    season         VARCHAR(20),
    format         VARCHAR(20),
    venue          VARCHAR(160),
    city           VARCHAR(120),
    team1_id       BIGINT REFERENCES team(id),
    team2_id       BIGINT REFERENCES team(id),
    winner_team_id BIGINT REFERENCES team(id)
);

CREATE TABLE innings (
    id              BIGSERIAL PRIMARY KEY,
    match_id        BIGINT NOT NULL REFERENCES cricket_match(id),
    batting_team_id BIGINT NOT NULL REFERENCES team(id),
    innings_number  INT    NOT NULL
);

CREATE TABLE delivery (
    id             BIGSERIAL PRIMARY KEY,
    innings_id     BIGINT NOT NULL REFERENCES innings(id),
    over_number    INT    NOT NULL,
    ball_number    INT    NOT NULL,
    striker_id     BIGINT NOT NULL REFERENCES player(id),
    non_striker_id BIGINT REFERENCES player(id),
    bowler_id      BIGINT NOT NULL REFERENCES player(id),
    runs_off_bat   INT    NOT NULL DEFAULT 0,
    extras         INT    NOT NULL DEFAULT 0,
    extra_type     VARCHAR(20),
    wicket         BOOLEAN NOT NULL DEFAULT FALSE,
    dismissal_kind VARCHAR(40),
    player_out_id  BIGINT REFERENCES player(id)
);

CREATE INDEX idx_delivery_innings ON delivery(innings_id);
CREATE INDEX idx_delivery_bowler  ON delivery(bowler_id);
CREATE INDEX idx_delivery_striker ON delivery(striker_id);
CREATE INDEX idx_delivery_phase   ON delivery(over_number);
```

> Note: table renamed `match` → `cricket_match` because `match` is a reserved SQL word.
> Decide now whether overs are 0-based (Cricsheet style) or 1-based, and record it.

---

## 6. Package layout (package-by-feature)

```
com.cricketiq
├── CricketIqApplication.java
├── config/        SecurityConfig · RedisConfig · OpenApiConfig
├── security/      User · UserRepository · JwtService · JwtAuthFilter · AuthController · dto
├── common/        GlobalExceptionHandler · ApiError · PageResponse
├── ingestion/     CricsheetParser · IngestionService   (sync first, async in v2)
├── team/          Team · TeamRepository
├── player/        Player · PlayerRepository · PlayerService · PlayerController · PlayerDto
├── match/         CricketMatch · Innings · repositories
├── delivery/      Delivery · DeliveryRepository
└── analytics/     MatchupService · PhaseAnalysisService · AnalyticsController · dto
```

---

## 7. Entity sketches

- **Player**: `Long id`, `String registryId`, `String name`, `String battingStyle`,
  `String bowlingStyle`, `String primaryRole`.
- **Delivery**: `Long id`; `@ManyToOne(fetch = LAZY) Innings innings`;
  `int overNumber, ballNumber`; `@ManyToOne(LAZY) Player striker, nonStriker, bowler`;
  `int runsOffBat, extras`; `String extraType`; `boolean wicket`;
  `String dismissalKind`; `@ManyToOne(LAZY) Player playerOut`.

> Default every `@ManyToOne` to `FetchType.LAZY` — EAGER is the #1 cause of the
> N+1 problem you want to fix on purpose, not suffer by accident.

---

## 8. Cricsheet ingestion

1. Download a JSON bundle from https://cricsheet.org/downloads/ (start with one tournament).
2. Each match file: `info` block (teams, venue, players, registry) + `innings` → `overs` → `deliveries`.
3. `CricsheetParser` maps a file to entities; `IngestionService` upserts teams/players
   (dedupe by registry id), then matches → innings → deliveries.
4. Keep v1 **synchronous**. Parallelizing is a deliberate v2 upgrade.
5. Verify with `SELECT count(*)` sanity checks against a known scorecard.

---

## 9. First-day checklist (in order)

- [ ] Generate project from Initializr (§1), add manual deps (§2).
- [ ] Add `docker-compose.yml` (§3); `docker compose up -d`; confirm Postgres + Redis up.
- [ ] Add `application.yml` (§4) and `V1__init.sql` (§5).
- [ ] Boot the app → Flyway creates all tables. Check with a DB client.
- [ ] Create packages (§6) + entities (§7); app starts clean with `ddl-auto: validate`.
- [ ] Commit. **First green checkpoint.**

---

## 10. MVP — Definition of Done (FROZEN) 🔒

The project is **done (v1)** when ALL are true and deployed:

- [ ] **Auth**: register + login with JWT; protected endpoints reject missing/invalid tokens.
- [ ] **Data**: one real Cricsheet tournament ingested into PostgreSQL.
- [ ] **One flagship analytics endpoint** working on real data (batsman matchup / phase analysis).
- [ ] **Caching**: that endpoint is Redis-cached, with a measurable latency difference.
- [ ] **API quality**: DTOs (no entities exposed), pagination, consistent errors via `@ControllerAdvice`.
- [ ] **Docs**: OpenAPI/Swagger UI at `/swagger-ui.html`.
- [ ] **Tests**: unit tests on analytics logic + ≥1 Testcontainers integration test.
- [ ] **Runs anywhere**: `docker compose up` starts app + Postgres + Redis.
- [ ] **Deployed**: a live URL.
- [ ] **README**: pitch, architecture diagram, design-decision section, screenshots, one-command setup.

Anything beyond this list → parking lot (message queue, observability, MongoDB, AWS, AI).
**Build the slice. Don't go wide.**
