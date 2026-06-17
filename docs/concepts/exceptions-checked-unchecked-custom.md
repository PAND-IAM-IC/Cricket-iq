## Topic: Exceptions — checked vs unchecked, and custom exceptions — plain terms

**Checked vs unchecked**

- **Checked exception** (extends `Exception`): the compiler *forces* you to handle it —
  either `try/catch` it or declare `throws X` on your method. Example: `IOException`.
  The obligation spreads to every caller and clutters code.
- **Unchecked exception** (extends `RuntimeException`): no obligation — you *can* catch it,
  but you don't have to. It bubbles up on its own until something handles it.

**Why we made a custom `IngestionException`**

Jackson's `readValue(...)` throws a **checked** `IOException` when a file is bad/malformed.
We don't want that obligation spreading through the whole app, and `IOException` is a
low-level, generic name. So in the parser we **catch it and re-throw our own**:

```java
public class IngestionException extends RuntimeException {
    public IngestionException(String message, Throwable cause) { super(message, cause); }
}

// in the parser:
catch (IOException e) {
    throw new IngestionException("Failed to parse Cricsheet file: " + file.getName(), e);
}
```

Benefits:
- Extends `RuntimeException` → **unchecked**, so it bubbles up cleanly (no forced try/catch).
- **Meaningful name** — "ingestion failed" tells you exactly what broke, not a generic IO error.
- Passes the original error as the **cause** (`e`) → underlying details aren't lost.

This pattern — wrapping a low-level exception in a meaningful domain one — is called
**exception translation**.

**How it connects to @Transactional**

`@Transactional` rolls back by default on unchecked exceptions. Because `IngestionException`
is a `RuntimeException`, a failure mid-ingestion automatically rolls back the whole match.

**How it connects to the global handler (coming later)**

Because it's a specific named type, the `@RestControllerAdvice` global handler can catch
`IngestionException` specifically and return a clean HTTP error response.

**Interview answers (simple)**

1. *Checked vs unchecked?* → checked must be handled/declared (compiler-enforced); unchecked
   (`RuntimeException`) has no such obligation.
2. *Why create a custom exception?* → a meaningful, specific type you can catch precisely and
   that carries domain meaning; often used to translate low-level checked exceptions.
3. *What is exception translation?* → catching a low-level exception and re-throwing it wrapped
   in a higher-level, domain-specific one (keeping the original as the cause).
