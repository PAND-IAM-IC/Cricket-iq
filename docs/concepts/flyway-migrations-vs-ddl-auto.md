## Topic: Flyway database migrations (plain terms)

**The question it answers:** how do database tables get created and changed over time?

### Two ways to create tables

1. **Hibernate auto-create (the way I used before):** Hibernate looks at my entity classes
   and builds the tables automatically. Convenient, but I don't control the exact SQL, can't
   review it, and it's risky in production (can change tables in surprising ways).
2. **Flyway migrations (what we use now):** *I* write the table-creation as SQL files, and
   Flyway runs them. I control exactly what happens.

### Why the Flyway way is better

- I control the exact SQL — nothing surprising happens to my database.
- The SQL files live in git, so every change is recorded and reviewable.
- Every machine (my laptop, a teammate's, the server) runs the same files and ends up with
  the identical database.

### What V1, V2, V3… mean

They're just version numbers marking the **order of changes** — like save points.
- `V1__init.sql` → the first change (creates the tables). That's what we wrote now.
- `V2__...sql` → the next change, made later.
- Flyway runs them in order (V1 → V2 → V3), each **exactly once**, and records which it has
  run (in a table called `flyway_schema_history`) so it never repeats one.

Naming: `V` + number + double underscore `__` + a description + `.sql`
(e.g. `V2__add_country_to_team.sql`). "init" just means "this one sets up the initial tables."

### What if I want to change the database later?

**Do NOT edit an old file (like V1) after it has run.** Instead, **add a new file with the
next number.**
- Example: to add a column later → create `V2__add_country_to_team.sql` with an
  `ALTER TABLE team ADD COLUMN country VARCHAR(60);` inside.
- Flyway sees V2 is new, runs it, done.
- Why not just edit V1? Because V1 already ran — Flyway won't run it again, and it actually
  *blocks* you from changing an already-run file (it stores a fingerprint of each one) so
  everyone's database stays consistent.

**Rule of thumb:** every database change = a new versioned file. Like git commits, but for
your database schema.

### How it pairs with our setting

We set `spring.jpa.hibernate.ddl-auto: validate`, which means Hibernate **does not** create
or change any tables — Flyway does that. Hibernate only *checks* that my entity classes match
the tables Flyway built, and complains if they don't. So Flyway owns the schema; Hibernate
just double-checks.

### Interview answers (simple)

1. *Why Flyway instead of letting Hibernate create tables?* → control, reviewable history in
   git, and the same schema on every environment; safe for production.
2. *What's a migration version?* → an ordered, run-once SQL change file (V1, V2, …).
3. *How do you change the schema later?* → add a new versioned file (never edit an old one).
