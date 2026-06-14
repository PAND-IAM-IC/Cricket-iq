## Topic: Where JPA/Hibernate stores its cache (the three levels) — plain terms

When we said "JPA can reuse player 17 instead of querying it again," the obvious question
is: *where does it keep that?* There are three different caches, at three different scopes.

### 1. First-level cache (always on, automatic)

- **Where:** in memory, **inside the Hibernate Session** (the "persistence context") — just
  a Java map in your app's RAM.
- **How long:** one **session / transaction** — basically one request. When the request
  finishes and the session closes, this cache is **thrown away**.
- **What it does:** within that one unit of work, if you access the same row twice (e.g.
  player 17 across many deliveries), Hibernate loads it from the DB **once** and reuses the
  same object. This is the "identity map."
- You can't turn it off. It only helps *within* a single transaction, not across requests.

### 2. Second-level cache (optional, OFF by default)

- **Where:** also inside your app's memory, but **shared across sessions/transactions**.
- **What it does:** keeps entities cached *between* requests so repeated loads skip the DB.
- We are **not** using it — you saw `HHH000026: Second-level cache disabled` in the boot log.
  It needs extra setup (a cache provider) and you only add it if profiling shows you need it.

### 3. Redis — application cache (what this project adds later)

- **Where:** a **separate server** (its own Docker container), outside your app.
- **How long:** lives across many requests until it expires or you evict it.
- **What it does:** caches **expensive computed results** — e.g. a player's full matchup
  stats — so you don't recompute/re-query them every time. We control it explicitly with
  `@Cacheable` / `@CacheEvict`.
- This is *not* automatic like the first-level cache — you decide what to cache.

### Quick comparison

| Cache | Where | Lives for | On by default? | You control it? |
|-------|-------|-----------|----------------|-----------------|
| First-level | In the Hibernate session (app RAM) | One transaction | Yes (can't disable) | No (automatic) |
| Second-level | In app RAM, shared | Across transactions | No | Yes (setup needed) |
| Redis | Separate server | Across requests, until evicted | N/A (we add it) | Yes (`@Cacheable`) |

### Interview answers (simple)

1. *Where is Hibernate's first-level cache?* → in memory inside the session/persistence
   context; lives for one transaction, then discarded.
2. *First vs second-level cache?* → first-level is per-transaction and always on; second-level
   is shared across transactions and optional (off by default).
3. *How is Redis different from these?* → it's a separate cache for computed results across
   requests, controlled explicitly with `@Cacheable`, not an automatic Hibernate cache.
