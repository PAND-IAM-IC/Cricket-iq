## Topic: Data modeling — "should this be its own table/entity?" (the 2-question test)

When you read a domain and list the candidate "things," use this test on each one to decide
whether it deserves its own table (entity) or is just a column on another table.

### The two questions

1. **Does it have attributes of its own?** (facts that belong to *it* and nothing else)
2. **Is it referenced/shared by many rows — something you filter or group by, that needs its
   own consistent identity?**

**Yes to either (usually both) → make it a table.** If it's just a bare value with no life
of its own → it's a column.

### Why "either" — a column count doesn't decide it

A table does NOT need lots of columns. Question 2 alone can justify a table.

### How it played out in this project

| Candidate | Q1 attributes of its own? | Q2 referenced / needs identity? | Verdict |
|-----------|---------------------------|---------------------------------|---------|
| Match | Yes (date, venue, format, season) | Yes (filter by it) | **table** |
| Innings | Yes (batting team, innings number) | Yes (groups deliveries) | **table** |
| Player | A few (name, styles) | Yes (referenced by every delivery) | **table** |
| Team | Barely (just a name) | **Yes** (referenced everywhere) | **table** (on Q2 alone) |
| Over | No (just a number) | Not really | **column** (`over_number`) |
| Dismissal | Few (kind, who) | 1-to-1 with a delivery | **columns on delivery** |

### The takeaway

You're not memorizing "cricket needs 5 tables." You're applying one repeatable test, so you
can do it for any domain (e-commerce, banking, etc.).

**Interview answer:** *"I decide if something is an entity by asking: does it have its own
attributes, and is it referenced/identified by many other rows? If either holds, it's a
table; if it's just a bare value, it's a column. That's why even a thin 'Team' (just a name)
is its own table — it's referenced everywhere and needs one identity."*
