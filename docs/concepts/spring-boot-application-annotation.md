## Topic: @SpringBootApplication (auto-configuration & component scanning)

**What problem does it solve?**

The IoC container starts empty and clueless. `@SpringBootApplication` is the one
annotation on the main class that turns it into a working app — it bundles three jobs.

**How does it work internally?**

`@SpringBootApplication` is a **meta-annotation** = three combined:

- **`@Configuration`** — marks the class as a source of bean definitions (you can declare
  beans via `@Bean` methods, e.g. for library classes you can't annotate directly).
- **`@EnableAutoConfiguration`** — Spring Boot ships hundreds of auto-config classes guarded
  by conditions like `@ConditionalOnClass` (only if X is on the classpath) and
  `@ConditionalOnMissingBean` (only if you haven't defined your own). So it sees the
  Postgres driver → creates a DataSource; sees Spring Web → starts Tomcat. Defaults appear
  automatically but back off the moment you define your own bean. ("Convention over
  configuration" — the feature that made Boot popular vs old XML-heavy Spring.)
- **`@ComponentScan`** — scans **this package and all sub-packages** for `@Component`,
  `@Service`, `@Repository`, `@RestController` and registers them as beans. This is why
  all your code must live under `com.cricketiq.cricketiq` — anything outside isn't found.

**Interview questions I can now answer:**
1. What does `@SpringBootApplication` do? → meta-annotation = `@Configuration` +
   `@EnableAutoConfiguration` + `@ComponentScan`.
2. How does Boot auto-configure? → conditional auto-config classes triggered by classpath,
   overridable by your own beans.
3. Why must my classes sit under the main class's package? → component scanning starts there.
4. `@Component` vs `@Service`/`@Repository`/`@Controller`? → all beans; the others are
   specializations (`@Repository` adds DB exception translation, `@Controller`/`@RestController`
   for web, `@Service` marks the service layer).

**Mistakes I made:**

_(fill in — e.g. a bean not being picked up because it was outside the scanned package)_

**How I fixed them:**

_(fill in)_
