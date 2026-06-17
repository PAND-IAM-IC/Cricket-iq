## Topic: AOP & proxies (and @Cacheable / @Transactional) — detailed

### WHY — the problem AOP solves

Say you want every service method to run in a transaction. WITHOUT AOP you'd write this in
every method:
```java
public void ingestMatch(File f) {
    Transaction tx = db.beginTransaction();   // plumbing
    try {
        // ... the actual logic ...
        tx.commit();                          // plumbing
    } catch (Exception e) {
        tx.rollback();                        // plumbing
        throw e;
    }
}
```
Now add logging, caching, security checks → every method balloons with the same repeated
plumbing and the real logic drowns. These concerns (transactions, logging, caching, security)
**cut across** many methods — "cross-cutting concerns." Copy-pasting them everywhere is
repetitive and bug-prone.

**AOP's idea:** write that plumbing ONCE, declare *where* it applies, and the framework injects
it around the matching methods. Your method shrinks back to its real job:
```java
@Transactional
public void ingestMatch(File f) {
    // ... just the actual logic. No tx plumbing. ...
}
```

### The real-life picture

A **receptionist sits in front of an expert**. You always talk to the receptionist (the
proxy). Before passing your request to the expert (your real method), they do paperwork (open
a transaction / check the cache); afterwards they file the result (commit / store it). You
think you're talking to the expert directly, but the receptionist is quietly in between.

### The vocabulary (so interview terms click)

- **Aspect** — the module holding the cross-cutting behavior (e.g. "transaction management").
- **Advice** — the code that runs, and when: `@Before`, `@After`, `@Around`.
- **Join point** — a place advice *can* run. In Spring AOP, always a **method call**.
- **Pointcut** — the rule selecting which join points (e.g. "every `@Transactional` method").
- **Weaving** — combining the aspect with your code. Spring does this at runtime via proxies.

### HOW — the proxy mechanism, step by step

At **startup**, Spring sees your bean has an annotated method and puts a **proxy** in the
container instead of your raw object:
```java
IngestionService real = new IngestionService(...);   // your object (the expert)
IngestionService bean = wrapWithProxy(real);          // the receptionist
context.register("ingestionService", bean);           // the PROXY goes in the container
```
Any bean that injects `IngestionService` receives the proxy. When it calls a method, the
proxy runs first:
```java
class IngestionService$$Proxy extends IngestionService {
    private final IngestionService real;
    @Override public void ingestMatch(File f) {
        // ADVICE BEFORE  (open transaction / check cache)
        try {
            real.ingestMatch(f);    // delegate to the real method
            // ADVICE AFTER SUCCESS  (commit / store in cache)
        } catch (RuntimeException e) {
            // ADVICE ON FAILURE  (rollback)
            throw e;
        }
    }
}
```

### Two proxy types (common follow-up)

- **JDK dynamic proxy** — when your bean implements an interface; Java builds a runtime class
  implementing that interface, delegating to your object.
- **CGLIB proxy** — when there's no interface; Spring generates a **subclass** of your class at
  runtime and overrides methods. (Spring Boot defaults to CGLIB in most cases.)
Spring picks automatically. (This is runtime, proxy-based AOP — Spring's flavor. The heavier
**AspectJ** can weave at compile time and intercept more than method calls.)

### What rides on this

- `@Transactional` → proxy opens a tx before, commits after (or rolls back on RuntimeException).
- `@Cacheable` → first call runs the method, proxy stores the result; next call with the same
  args, the proxy returns the stored result *without running the method* (we'll back this with Redis).
- `@CacheEvict` → proxy removes a cached entry (e.g. after new data is ingested).

### The self-invocation trap (classic interview question)

The behavior lives in the proxy (receptionist), so it only runs for calls that go *through* it.
If your method calls another annotated method in the same class via `this.someMethod()`, that's
expert-to-expert directly — it bypasses the receptionist, so no transaction/cache logic runs.
Fix: call it from a different bean.

### Interview answers

1. *What is AOP?* → adding cross-cutting behavior (transactions, caching, logging) around methods
   without putting it inside them.
2. *How does Spring implement it?* → proxies (JDK dynamic or CGLIB) that intercept calls and run
   advice before/after your method.
3. *Why does `@Transactional`/`@Cacheable` fail on a same-class call?* → self-invocation bypasses
   the proxy.
