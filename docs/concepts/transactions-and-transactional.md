## Topic: @Transactional — how it works and how it achieves atomicity

### The real-life picture (hold this the whole way)

You walk into a bank to move ₹500 from account A to account B. You ask for a **teller** — but
you never reach the teller directly. A **supervisor stands in front of the teller's desk**,
and every request goes *through* them:

1. The supervisor opens a fresh **ledger page** for your request.
2. Hands it to the **teller**, who does the work: *debit A, credit B*.
3. Teller finished cleanly → supervisor **stamps the page "FINAL"** → it's official.
4. Something broke halfway (debited A, never credited B) → supervisor **shreds the page** → as
   if nothing happened.

The page is *either* fully stamped *or* fully shredded — never half. Mapping:
**supervisor = proxy · ledger page = transaction · stamp = commit · shred = rollback ·
all-or-nothing = atomicity.**

### What a transaction is

A group of database operations that must **all succeed or all fail together**. That
all-or-nothing property is **atomicity** (the "A" in ACID).

### What @Transactional does (and the key reframe)

`@Transactional` does NOT itself achieve atomicity — the **database** does. `@Transactional`
only controls the *boundaries*: when to say BEGIN, COMMIT, ROLLBACK. Two layers:

**Layer 1 — Spring manages the boundaries.** At startup Spring wraps your bean in a proxy
(the supervisor). When the method is called, the proxy:
```java
class IngestionService$$Proxy extends IngestionService {
    private final IngestionService real;                 // the teller
    private final PlatformTransactionManager txManager;  // the ledger system

    @Override
    public void ingestMatch(File file) {
        TransactionStatus tx = txManager.getTransaction(...); // (A) open ledger page: BEGIN
        try {
            real.ingestMatch(file);                           // (B) teller does the work
            txManager.commit(tx);                             // (C) stamp FINAL: COMMIT
        } catch (RuntimeException e) {
            txManager.rollback(tx);                           // (C') shred page: ROLLBACK
            throw e;
        }
    }
}
```
Step (A) does three things: borrows ONE connection from the pool, sends `BEGIN`, and **binds
that connection to the current thread**. That binding is why every `repository.save(...)`
inside the method joins the *same* transaction — they all run on the same thread, so they all
pick up the same bound connection.

**Layer 2 — the database achieves atomicity.** Your changes aren't permanent until COMMIT.
Postgres (via **MVCC**) tags every row your transaction inserts with your transaction id, in a
"not yet committed" state invisible to others.
- **COMMIT** → marks your transaction id *committed* → all those rows become visible/permanent
  at once.
- **ROLLBACK** → marks it *aborted* → all those rows are ignored forever (cleaned up later by
  vacuum), as if never written.
Because it's one flag flip for the whole transaction, you can never end up with A debited but
B not credited. That single flip **is** atomicity. (The **Write-Ahead Log / WAL** adds
durability: on a crash, committed transactions are replayed and uncommitted ones discarded.)

### Why ingestion needs it

Importing one match = many inserts (teams, players, match, innings, every delivery). Fail at
delivery #40 without a transaction → a half-saved, corrupt match. With `@Transactional`, the
failure undoes the whole match.

### Rollback rule (interview gotcha)

Rolls back by default ONLY on **unchecked** exceptions (`RuntimeException`). NOT on checked
exceptions unless you set `rollbackFor`. Our `IngestionException` extends `RuntimeException`,
so a failure mid-ingestion auto-rolls-back.

### Self-invocation trap (because it's a proxy)

The BEGIN/COMMIT lives in the proxy (supervisor). So it only runs for calls that go *through*
the proxy. If your method calls another `@Transactional` method in the same class via
`this.x()`, that goes teller-to-teller directly, bypassing the supervisor — no transaction
starts. Fix: call it from a different bean.

### Interview answers

1. *How does `@Transactional` work?* → Spring proxies the bean; the proxy opens a transaction
   on one thread-bound connection, runs your method, commits on normal return or rolls back on
   a RuntimeException.
2. *How is atomicity achieved?* → by the database: changes stay provisional (tagged with the
   txn id, MVCC) and only flip to permanent on commit, or are discarded on rollback — one flag
   flip for the whole transaction.
3. *When does it roll back?* → unchecked exceptions by default; checked only with `rollbackFor`.
4. *Why does a same-class call not start a transaction?* → self-invocation bypasses the proxy.
