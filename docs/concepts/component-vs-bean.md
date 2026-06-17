## Topic: @Component vs @Bean

Both register a bean in the Spring container. The difference is *where* you put them and
*who owns the class*.

**@Component** (and `@Service` / `@Repository` / `@RestController`, which are specialized
`@Component`s)
- Goes **on a class you wrote**.
- Component scanning auto-detects it and creates the bean.
- Use when: it's your own class and you can annotate it directly (e.g. `CricsheetParser`,
  `IngestionService`).

**@Bean**
- Goes **on a method inside a `@Configuration` class**; the method builds and returns the
  object, and Spring registers the return value as a bean.
```java
@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper cricsheetObjectMapper() {
        return new ObjectMapper().setPropertyNamingStrategy(...);
    }
}
```
- Use when: you can't annotate the class — it's a **third-party/library class** (e.g.
  `ObjectMapper`, `RedisTemplate`, `DataSource`) — or you need **custom construction/config**.

**Rule of thumb**
- Your own class → `@Component`.
- Someone else's class, or needs manual setup → `@Bean` in a `@Configuration` class.

**Interview answer:** `@Component` is auto-detected on classes I own via component scanning;
`@Bean` is an explicit factory method in a `@Configuration` class, used for third-party classes
I can't annotate or when I need custom instantiation logic.
