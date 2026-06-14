## Topic: Mapping foreign keys in JPA (@ManyToOne) — plain terms

**The situation**

In the database, a foreign key is just a column holding another row's id
(e.g. `delivery.striker_id = 17` points at the player with id 17).

In JPA, instead of storing a raw `Long strikerId`, you usually store a **reference to the
actual object**, and JPA handles the id column for you:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "striker_id")
private Player striker;
```

Now you can write `delivery.getStriker().getName()` — navigate objects, not juggle ids.

**The three annotations**

- **`@ManyToOne`** — "many of THIS entity relate to ONE of that one." Many deliveries → one
  striker. Many matches → one team. (Read it many-[me]-to-one-[them].)
- **`@JoinColumn(name = "...")`** — which database column holds the foreign key. Match it to
  your schema column name exactly (e.g. `striker_id`, `team1_id`).
- **`fetch = FetchType.LAZY`** — load the related object only when you actually access it.

**The big gotcha**

`@ManyToOne` defaults to **EAGER** (loads the related object every time, always). That's the
#1 cause of the N+1 problem. So **always set `fetch = FetchType.LAZY` explicitly** on every
`@ManyToOne`. (`@OneToMany` already defaults to LAZY; `@ManyToOne` and `@OneToOne` default to
EAGER — those are the ones to watch.)

**Nullable foreign keys**

For a FK that can be empty (like `winner_team_id`), that's fine — the reference is just
`null`. For a NOT NULL FK you can optionally be explicit:
`@JoinColumn(name = "match_id", nullable = false)`.

**Field naming → column naming**

Spring Boot's default strategy maps camelCase field names to snake_case columns
automatically: `registryId` → `registry_id`, `battingStyle` → `batting_style`. So plain
columns (non-FK) usually don't need `@Column` as long as you name the field in camelCase.

**Interview answers (simple)**

1. *How do you map a foreign key in JPA?* → with `@ManyToOne` + `@JoinColumn`, holding a
   reference to the related entity.
2. *What does `@ManyToOne` default to, and why change it?* → EAGER; set it to LAZY to avoid
   loading related data you didn't ask for (prevents N+1).
3. *Difference between @ManyToOne and @OneToMany fetch defaults?* → @ManyToOne/@OneToOne are
   EAGER by default; @OneToMany is LAZY by default.
