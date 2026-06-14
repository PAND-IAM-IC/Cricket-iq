## Topic: Spring IoC Container, Beans & Dependency Injection

**What problem does it solve?**

In plain Java, every class creates and wires its own dependencies with `new`. With 200
interdependent classes that becomes unmanageable, tightly coupled, and hard to test.
Spring takes over creating and wiring objects for you.

**How does it work internally?**

- **Inversion of Control (IoC):** control of *object creation* is inverted from your code
  to the framework. You no longer call `new PlayerService(...)`; Spring builds it.
- **The container** is the thing that creates, configures, wires, and manages those
  objects. In code it's the `ApplicationContext`. `SpringApplication.run()` boots it.
- **A bean** = a plain Java object whose lifecycle Spring owns (creates it, injects its
  dependencies, keeps a single shared instance by default, destroys it at shutdown).
- **Dependency Injection (DI):** when Spring builds `PlayerService` and sees it needs a
  `PlayerRepository`, it finds that bean and injects it automatically. Result: loose
  coupling, easy testing (swap in a mock), centrally managed instances.

**Interview questions I can now answer:**
1. What is the Spring IoC container? → `ApplicationContext`; creates/manages beans + DI.
2. What is Inversion of Control? → object creation/wiring handed from my code to Spring.
3. What is a bean? → an object whose lifecycle Spring manages.
4. Why is DI useful? → loose coupling, testability, single managed instances.

**Mistakes I made:**

_(fill in)_

**How I fixed them:**

_(fill in)_
