# Concept Coverage Map

Every interview concept from the roadmap, mapped to a **concrete home** in the cricket
project. This is the checklist that guarantees the project actually teaches what it's
supposed to.

**Legend:**
- 🟢 **Natural** — arises on its own if we build properly. No forcing needed.
- 🟡 **Deliberate** — staged as a learning exercise. Keep these clearly documented (a
  comment or a dedicated `docs/concepts/` note), ideally explored on a branch, so the
  main codebase stays clean. Forcing patterns into production code is an interview
  *anti*-signal — the skill is knowing *when* to use them.

> As you implement each, tick it and write the matching `docs/concepts/<topic>.md` entry.

---

## Core Java

| Concept | Where it lives in this project | Type |
|---|---|---|
| `HashMap` / Collections internals | Aggregating analytics (group deliveries by bowler, phase) into maps | 🟢 |
| `equals()` / `hashCode()` | Entity & DTO equality; dedup players by registry id during ingestion | 🟢 |
| String immutability / pool | General; comes up when building cache keys | 🟢 |
| Streams / lambdas / `Optional` | Analytics aggregation; `repository.findById()` returns `Optional` | 🟢 |
| Checked vs unchecked + custom exceptions | `PlayerNotFoundException`, `IngestionException` → handled by `@ControllerAdvice` | 🟢 |
| Generics | `JpaRepository<Player, Long>`; a generic `ApiResponse<T>` wrapper | 🟢 |
| **Marker interface** (`Serializable`) | DTOs cached in Redis **must** implement `Serializable` — a *real* marker interface in action | 🟢 |
| Multithreading (`CompletableFuture`, `ExecutorService`) | Parallel Cricsheet ingestion | 🟡 (v2 upgrade) |
| Thread safety / `volatile` / atomics | A request-counter or ingestion progress tracker | 🟡 |

## Spring Core

| Concept | Where it lives | Type |
|---|---|---|
| Constructor injection | Every service & controller (the preferred style) | 🟢 |
| Bean lifecycle (`@PostConstruct`) | Log config / warm a cache on startup | 🟢 |
| Bean scopes (singleton default) | All beans; note why singleton in a concept doc | 🟢 |
| `@Primary` / `@Qualifier` | Two `AnalyticsStrategy` implementations → choose between them | 🟢 |
| **Circular dependency + `@Lazy`** | Deliberately create `ServiceA ↔ ServiceB`, watch startup fail, fix with redesign, then demo `@Lazy` + the three-level cache | 🟡 |
| **AOP / proxies** | Powers `@Transactional`, `@Cacheable`; demo the **self-invocation trap** | 🟢 |
| `@Transactional` (propagation, isolation, rollback) | Ingestion service; multi-step saves | 🟢 |
| `@Configuration` / `@Bean` | `SecurityConfig`, `RedisConfig`, `OpenApiConfig` | 🟢 |
| `@Value` vs `@ConfigurationProperties` | JWT settings bound via `@ConfigurationProperties` | 🟢 |
| Profiles (dev/prod) | Different DB/Redis config per environment | 🟢 |

## Spring Boot

| Concept | Where it lives | Type |
|---|---|---|
| Auto-configuration / starters | The whole project; explain in README design section | 🟢 |
| Actuator (`/health`, `/metrics`) | Monitoring endpoints | 🟢 |

## Spring Security

| Concept | Where it lives | Type |
|---|---|---|
| Authentication vs Authorization | Login (authn) + role-based endpoint access (authz) | 🟢 |
| JWT flow | `AuthController` issues token; filter validates it | 🟢 |
| `OncePerRequestFilter` | `JwtAuthFilter` in the security filter chain | 🟢 |
| `SecurityContextHolder` | Reading the current user inside a request | 🟢 |
| `BCrypt` password hashing | Storing user passwords | 🟢 |

## Persistence (JPA / Hibernate / Postgres)

| Concept | Where it lives | Type |
|---|---|---|
| Entities & repositories | `Player`, `Delivery`, etc. + Spring Data repos | 🟢 |
| JPQL / derived queries | Custom analytics queries | 🟢 |
| `Specifications` (dynamic queries) | Filtering matchups by phase/opponent | 🟡 |
| LAZY vs EAGER | All `@ManyToOne` set LAZY on purpose | 🟢 |
| **N+1 problem** + `JOIN FETCH` / `@EntityGraph` | Loading deliveries with players in analytics — see it, then fix it | 🟢 |
| Optimistic locking (`@Version`) | On `Player` or a mutable entity | 🟡 |
| Connection pooling (HikariCP) | Default in Boot; explain in a concept doc | 🟢 |

## Caching (Redis)

| Concept | Where it lives | Type |
|---|---|---|
| `@Cacheable` / `@CacheEvict` | The flagship analytics endpoint | 🟢 |
| Cache invalidation strategy | Evict player cache when new data ingested | 🟢 |

## Design Patterns

| Concept | Where it lives | Type |
|---|---|---|
| **Strategy** | Swappable `AnalyticsStrategy` (matchup vs phase vs dismissal) | 🟢 |
| **Builder** | DTO construction via Lombok `@Builder` | 🟢 |
| **Factory** | Creating the right report/strategy object | 🟡 |
| **Observer** | Spring `ApplicationEventPublisher` — fire an "IngestionCompleted" event | 🟡 |

## Testing

| Concept | Where it lives | Type |
|---|---|---|
| Unit tests (JUnit + Mockito) | Analytics service logic with mocked repos | 🟢 |
| `MockMvc` (web-layer tests) | Controller endpoints | 🟢 |
| Integration tests (Testcontainers) | Real Postgres in a container | 🟢 |
| Mock vs Spy | Demonstrated across the test suite | 🟢 |

---

## How to use this

1. When you build a feature, check which concepts it touches here and tick them.
2. For each 🟢 you hit, write the matching `docs/concepts/<topic>.md` (using the concept-log template).
3. For each 🟡, treat it as a small dedicated study exercise — build it, document *why*
   it works, and note whether you'd actually keep it in production code.
4. By v1 "done," most 🟢 boxes should be ticked. The 🟡 ones are stretch/learning extras.
