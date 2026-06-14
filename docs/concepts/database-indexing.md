## Topic: Database indexing (plain terms)

**What it is**

An index is like the index at the back of a book. Without it, to find every mention of a
word you'd read all 1,000 pages. With it, you look up the word and jump straight to the
right pages.

**Why we need it**

The `delivery` table will have millions of rows. To find "all balls where
`player_out_id = Kohli`":
- **Without an index:** the database reads *every row* and checks each one. Slow.
- **With an index on `player_out_id`:** it keeps a sorted lookup that points straight to the
  matching rows. Fast.

**The trade-off**

Indexes make *searching* faster but make *inserting/updating* a little slower (the index has
to be kept up to date too), and they use some disk space. So you only index columns you
**search/filter/join by often** — not every column.

**Which columns we index in `delivery`, and why**

- `striker_id` — we constantly ask "all balls faced by batsman X."
- `bowler_id` — "all balls bowled by bowler X."
- `innings_id` — we join deliveries back to their innings.
- `player_out_id` — dismissal analysis filters by who got *out*. Important: the player who
  gets out isn't always the striker — on a **run-out it can be the non-striker**. So "how
  was Kohli out?" must filter `player_out_id = Kohli`, not `striker_id`.

We do NOT index `over_number` on its own — it only has ~20 possible values, so the index
barely helps (the database might as well scan).

```sql
CREATE INDEX idx_delivery_striker    ON delivery(striker_id);
CREATE INDEX idx_delivery_bowler     ON delivery(bowler_id);
CREATE INDEX idx_delivery_innings    ON delivery(innings_id);
CREATE INDEX idx_delivery_player_out ON delivery(player_out_id);
```

**Interview answers (kept simple)**

1. *What's an index?* A lookup that lets the database jump straight to matching rows instead
   of scanning the whole table — like a book's index.
2. *Why not index everything?* Indexes speed up reads but slow down writes and use space, so
   you only index columns you search by a lot.
3. *(If pushed on internals)* It's stored as a sorted structure called a B-tree — but the
   practical point is: sorted lookup → fast search.
