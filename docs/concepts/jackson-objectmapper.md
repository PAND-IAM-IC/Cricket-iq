## Topic: Jackson's ObjectMapper — the JSON ↔ Java object translator

### The real-life picture

Think of a **bilingual translator/interpreter**. JSON is one language (text full of braces and
brackets). Java objects are another. The `ObjectMapper` is the translator who sits between
them: hand it a JSON document plus "I want this as a `CricsheetMatch`," and it reads the JSON
and builds the Java object, filling each field by matching names. Hand it a Java object and it
produces JSON text. It translates **both directions**.

### What it is

`ObjectMapper` is Jackson's central class for converting between JSON and Java objects
("Object" + "Mapper" = it *maps* JSON to/from objects). Two directions:
- **Deserialize**: JSON → Java object — `readValue(...)`. (What our parser uses.)
- **Serialize**: Java object → JSON — `writeValue(...)`. (What Spring uses for REST responses.)

### How `readValue` works (the mechanism)

```java
CricsheetMatch match = objectMapper.readValue(file, CricsheetMatch.class);
```
1. Reads the file's JSON text.
2. Looks at the target type `CricsheetMatch` (a record with components `info`, `innings`).
3. For each JSON key, finds the matching component **by name** (using the naming strategy) and
   converts the JSON value to the Java type — **recursively** for nested objects (`Info`,
   `InningsJson`, …) and arrays (`List`).
4. For records, it calls the **canonical constructor** with the matched values — so our compact
   constructors run, and the `List.copyOf` defensive copies happen during parsing.
5. Returns the fully built object tree.

### The two settings we configure, and WHY

```java
private final ObjectMapper objectMapper = new ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
```
- **`SNAKE_CASE`** — by default Jackson matches JSON keys to Java field names *exactly*
  (camelCase). But Cricsheet JSON is snake_case (`match_type`, `non_striker`, `player_out`).
  This setting tells the translator "the JSON is snake_case," so it auto-maps
  `match_type` ↔ `matchType`. WITHOUT it, those fields silently come back `null`.
- **`FAIL_ON_UNKNOWN_PROPERTIES = false`** — by default, if the JSON has a key with no matching
  Java field, Jackson throws. Cricsheet's `info` block has many fields we don't model
  (`officials`, `toss`, `gender`…). This setting says "ignore keys I don't have a field for."

### Why a dedicated ObjectMapper (not Spring's shared one)

Spring Boot auto-configures a global `ObjectMapper` used for your REST API JSON. You do NOT
want snake_case there (your API uses camelCase). So the parser makes its **own** mapper
configured for Cricsheet, leaving the global one untouched.

### Interview answers

1. *What is ObjectMapper?* → Jackson's class that converts between JSON and Java objects
   (deserialize with `readValue`, serialize with `writeValue`).
2. *How does it map snake_case JSON to camelCase fields?* → a `PropertyNamingStrategy`
   (`SNAKE_CASE`), or per-field `@JsonProperty`.
3. *How do you ignore JSON fields you don't model?* → disable
   `FAIL_ON_UNKNOWN_PROPERTIES` (or `@JsonIgnoreProperties(ignoreUnknown = true)`).
