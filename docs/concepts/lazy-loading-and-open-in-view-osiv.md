## Topic: Lazy loading, the persistence session & Open-Session-In-View (OSIV)

**What problem does it solve?**

Two related questions: (1) *when* is related data loaded from the database — up front, or
only when you ask for it? and (2) *how long* does the database session stay open during a
web request? Getting these wrong wastes database connections and silently hides the N+1
query problem.

---

### Building block 1 — the persistence session

The **session** (Hibernate `Session` / JPA persistence context) is your app's open "line"
to the database during a unit of work.

> **Analogy:** think of it as an open phone call to a warehouse. While the call is
> connected, you can keep asking "send me box 5, now box 12." The moment you hang up, you
> can't ask for anything more.

While the session is open, the entity objects you loaded are "managed" and you can still
pull more related data through that connection. Once it closes, those objects are
"detached" and trying to load more data from them fails.

---

### Building block 2 — lazy vs eager loading

When you load a `Player`, that player is linked to thousands of `deliveries` (every ball
they faced). You usually don't want to drag all of those along every time. So JPA lets you
mark a relationship:

- **LAZY** — "don't load the deliveries now. Load them only *if and when* someone actually
  calls `player.getDeliveries()`." This is the default for `@OneToMany`/`@ManyToOne` we want.
- **EAGER** — "always load the deliveries immediately, every time, alongside the player."

The catch with LAZY: to fetch the deliveries later *on demand*, **the session (phone line)
must still be open** at the moment you ask. If it has closed, you get a
`LazyInitializationException`.

> In this project we deliberately set every `@ManyToOne` to LAZY, because EAGER is the #1
> cause of the N+1 problem — we want to load related data *on purpose*, not by accident.

---

### What OSIV (Open-Session-In-View) is

OSIV is a setting that answers: **how long do we keep the session open during a web request?**

A request flows like this:
```
request → controller → service (does DB work) → returns data → data serialized to JSON → response sent
```

- **OSIV ON** (Spring Boot's *default*): the session stays open all the way to the end —
  *including* the JSON serialization step. So if a lazy field is touched while building the
  JSON, it can still quietly run a database query.
- **OSIV OFF** (`spring.jpa.open-in-view: false`, what we set): the session closes when the
  **service method finishes** (the transaction boundary). If anything touches a lazy field
  *after* that — e.g. during JSON rendering — you get a `LazyInitializationException`.

---

### Why OSIV ON is considered an anti-pattern (why we turned it off)

1. **It holds a database connection hostage for the entire request** — including the slow
   JSON-rendering phase, which doesn't need the DB at all. Under load you run out of pooled
   connections faster. Closing the session when the service finishes frees the connection
   sooner.
2. **It hides the N+1 problem.** With OSIV on, lazy fields silently fire one query each
   during serialization — scattered and invisible. With OSIV off, that blows up loudly,
   which *forces* you to load what you need explicitly in the service layer (using
   `JOIN FETCH` / `@EntityGraph`) — surfacing the N+1 where you can actually see and fix it.

---

### The trade-off (LazyInitializationException is a *feature* here)

With OSIV off you'll occasionally hit `LazyInitializationException`. That's not a bug to
paper over — it's the framework telling you "you didn't fetch this data while you had the
chance; go be intentional about it." It pushes you toward fetching exactly what you need in
the service layer, which is the discipline interviews test.

---

### How it connects to our config

- On first boot we saw the warning:
  `spring.jpa.open-in-view is enabled by default ...`
- We fixed it by setting `spring.jpa.open-in-view: false` under `spring.jpa` in
  `application.yaml` (and learned that YAML nesting matters — it has to sit under `spring.jpa`,
  not at the root, or Spring ignores it).

---

**Interview questions I can now answer:**

1. **What is lazy vs eager loading?** → Lazy loads a related collection only when accessed;
   eager loads it immediately every time. Lazy needs the session still open when accessed.
2. **What causes a `LazyInitializationException`?** → Accessing a lazy field after the
   persistence session has already closed (entity is detached).
3. **What is OSIV and why disable it?** → It keeps the session open through view/JSON
   rendering. Disabling it frees DB connections sooner and exposes N+1 problems so you fetch
   data intentionally in the service layer.
4. **How do you then load related data safely with OSIV off?** → Fetch it explicitly in the
   service while the session is open — `JOIN FETCH` in a JPQL query or an `@EntityGraph`.

**Mistakes I made:**

- First boot logged that `spring.jpa.open-in-view` was enabled by default.
- Later, my setting didn't take effect because the YAML wasn't nested under `spring.jpa`
  (indentation bug).

**How I fixed them:**

- Set `spring.jpa.open-in-view: false`, correctly nested under `spring: jpa:` in
  `application.yaml`.
