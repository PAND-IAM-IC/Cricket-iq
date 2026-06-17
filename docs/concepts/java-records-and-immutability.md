## Topic: Java records & immutability (shallow vs deep) — plain terms

**What a record is**

A `record` is Java's short way to declare an immutable data-carrier. One line:
```java
public record Outcome(String winner) {}
```
automatically gives you: a `private final` field, a constructor, an accessor `winner()`,
plus `equals`, `hashCode`, `toString`. No setters. Great for "just hold data we read."

**Why it's immutable**

The fields are `private final` and there are no setters, so after construction you can't
change them:
```java
Outcome o = new Outcome("Sunrisers");
o.winner();             // ✅ read
o.setWinner("x");       // ❌ doesn't exist
o.winner = "x";         // ❌ field is final
```
(Compare an `@Entity` with Lombok `@Setter` — that one is mutable on purpose.)

**Shallow vs deep immutability (the catch)**

A plain record gives **shallow** immutability: the *reference* is final, but if it points at
a *mutable* object (a `List`, `Map`), the contents can still be changed:
```java
match.innings();              // reference is final — can't repoint
match.innings().add(x);       // ⚠️ COMPILES — the List itself is still mutable
```

**Making it deep: compact constructor + defensive copy**

Add a **compact constructor** (no parameter list) and copy collections into unmodifiable
ones with `List.copyOf` / `Map.copyOf`:
```java
public record InningsJson(String team, List<OverJson> overs) {
    public InningsJson {
        overs = overs == null ? List.of() : List.copyOf(overs);
    }
}
```
- `List.copyOf` / `Map.copyOf` return an **unmodifiable** snapshot → `add()` now throws.
- The `== null ? List.of()` guard is required because `copyOf` throws `NullPointerException`
  on a null collection (and Cricsheet omits `extras`/`wickets` when there are none → null).
- Jackson still works: it deserializes through the canonical constructor, so the compact
  constructor runs and the copies happen during parsing.

**How deep is deep enough?**

Copying at each record level (what we do) freezes the outer collections. A nested
`Map<String, List<String>>` still has mutable inner lists unless you copy those too. Fully
deep immutability is diminishing returns for parse-once data — know the limit, don't over-engineer.

**Interview answers (simple)**

1. *Why is a record immutable?* → fields are `private final`, no setters.
2. *Is that fully immutable?* → only shallow; a final reference to a mutable List/Map can
   still have its contents changed.
3. *How do you make it deep?* → compact constructor + `List.copyOf`/`Map.copyOf` (unmodifiable,
   with null guards).
