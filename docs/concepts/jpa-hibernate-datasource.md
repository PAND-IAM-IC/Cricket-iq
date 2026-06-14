## Topic: JPA vs Hibernate, the DataSource / JDBC URL, and HikariCP

**What problem does it solve?**

Getting Java objects in and out of a relational database, and managing the connections
to that database efficiently.

**How does it work internally?**

**JPA vs Hibernate vs Spring Data JPA** (socket analogy):
- **JPA** = the *specification* — interfaces and annotations only (`@Entity`, `@Id`,
  `@OneToMany`, `EntityManager`). It's just rules; it contains no working code.
- **Hibernate** = the *implementation* of that spec — the real engine that generates SQL,
  talks to the DB, maps rows ↔ objects. (Default in Spring Boot; alternatives exist, e.g.
  EclipseLink.)
- **Spring Data JPA** = Spring's convenience layer on top — write an empty interface
  `PlayerRepository extends JpaRepository<Player, Long>` and Spring auto-implements
  `save`, `findById`, `findAll`, etc.
- Layering: my code → Spring Data JPA → JPA (spec) → Hibernate (does the work) →
  JDBC driver → database.

**DataSource & JDBC URL:**
- A **DataSource** is the object that holds DB connection info and hands out connections.
- The address is a **JDBC URL**: `jdbc:postgresql://localhost:5432/cricketiq`
  - `jdbc:` standard Java DB API · `postgresql:` which DB (the driver) ·
    `localhost:5432` host:port · `/cricketiq` database name.
- Plus username/password. Normally set in `application.yaml`, but the
  `spring-boot-docker-compose` integration reads the running container and wires these
  automatically in local dev. In prod we'll pass them via environment variables.

**HikariCP (connection pooling):**
- Opening a new DB connection per request is slow. A **connection pool** keeps a set of
  open connections ready to reuse. HikariCP is Spring Boot's default pool (the
  `HikariPool-1 - Start completed` log line).

**Interview questions I can now answer:**
1. JPA vs Hibernate? → spec vs implementation; I code to JPA, Hibernate runs underneath.
2. What is Spring Data JPA? → repository abstraction on top of JPA/Hibernate.
3. What is a JDBC URL? → the address string telling the driver where/which DB to connect to.
4. Why connection pooling / what is HikariCP? → reuse connections instead of opening one
   per request; Hikari is Boot's default pool.

**Mistakes I made:**

- On first boot, `compose.yaml` used `postgres:latest`, which pulled 18.4 and triggered a
  Flyway "untested version" warning.

**How I fixed them:**

- Pinned `postgres:17` (a Flyway-supported version) in `compose.yaml`.
