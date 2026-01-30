# Code Review Prompts for GitHub Copilot (Claude Opus 4.5)

Below are battle-tested prompt templates you can paste directly into Copilot Chat inside IntelliJ to review your Spring Boot codebase for:
-	Best practices
-	Unit test coverage
-	Memory leaks
-	Thread/concurrency issues
-	Readability & maintainability
-	Spring Boot gotchas


✅ 1. Start With a “Senior Reviewer” Framing Prompt
Use this first to set the context:
```shell
Act as a senior Java Spring Boot architect and code reviewer.

Review this code for:
1) Best practices (Java 17+/Spring Boot)
2) Clean code principles
3) Thread safety & concurrency issues
4) Memory leaks or object lifecycle issues
5) Performance bottlenecks
6) Unit test coverage gaps
7) Readability and maintainability
8) Potential production risks

When reviewing:
- Be specific
- Point to exact lines or patterns
- Suggest refactored examples
- Mention severity: Critical / Medium / Minor
- Do not rewrite everything unless necessary
```

✅ 2. Whole Project Review Prompt

Use this when Copilot has project-wide context:

```shell
Review my entire Spring Boot project for production readiness.

Focus on:
- Dependency injection correctness
- Bean lifecycle issues
- Thread safety in services
- Blocking calls in async/reactive code
- Memory usage patterns
- Exception handling consistency
- Logging best practices
- Testability
- Code smells
- Security risks

Output format:
1) Critical issues
2) Performance risks
3) Code quality improvements
4) Testing gaps
5) Suggested refactoring priorities
```

✅ 3. Unit Test Coverage Review Prompt

```shell
Analyze this code and identify:
- Untested logic branches
- Missing edge case tests
- Mocking improvements
- Flaky test risks
- Opportunities for parameterized tests
- Integration vs unit test boundaries

Then propose:
- A list of recommended test cases
- Example JUnit 5 test snippets
```

✅ 4. Memory Leak & Resource Safety Prompt

```shell
Review this code for potential memory leaks or resource mismanagement.

Look for:
- Unclosed streams
- ExecutorServices not shutdown
- Static references to large objects
- ThreadLocal misuse
- Caching issues
- Bean scope misuse
- Event listeners not deregistered
- Reactive streams not disposed

Explain:
- Why it may leak
- How to fix it
```

✅ 5. Concurrency & Thread Safety Prompt

```shell
Analyze this code for concurrency issues.

Focus on:
- Shared mutable state
- Synchronization issues
- Unsafe collections
- Race conditions
- Deadlock risks
- Blocking calls inside async methods
- Improper use of @Async or CompletableFuture

Provide:
- Severity level
- Refactored thread-safe example
```

✅ 6. Spring Boot Best Practices Prompt

```shell
Review this code against Spring Boot best practices:

Check:
- Proper use of @Service, @Component, @Repository
- Constructor injection vs field injection
- Configuration management
- Transaction management
- Exception handling
- Logging
- DTO separation
- Validation
- Package structure

Highlight:
- Anti-patterns
- Improvements
```

✅ 7. Readability & Maintainability Prompt

```shell
Review this code for readability and maintainability.

Look for:
- Long methods
- Complex conditionals
- Poor naming
- Duplicate logic
- High coupling
- Low cohesion
- Violations of SOLID principles

Suggest:
- Refactoring strategies
- Better method decomposition
```

✅ 8. Production Readiness Audit Prompt

```shell
Act as a production readiness reviewer.

Would you approve this code for deployment?

Check:
- Error handling
- Logging coverage
- Monitoring readiness
- Security risks
- Scalability concerns
- Failure recovery
- Test coverage
- Code maintainability

Provide:
- Approve / Reject verdict
- Key risks
- Actionable fixes
```

💡 Pro Tips for Better Results in IntelliJ Copilot Chat

✔️ 1. Review One Layer at a Time

Ask Claude to review:
•	Controller layer
•	Service layer
•	Repository layer
•	Config classes
•	Tests

Instead of dumping everything.


✔️ 2. Ask for Refactored Examples

Always add:

```shell
Provide refactored example code where applicable.
```

Claude is much stronger when asked explicitly.

✔️ 3. Ask for Severity Ratings

```shell
Suggest tools or static analyzers I should run to catch these issues automatically.
```

You’ll get suggestions like:
•	SonarQube
•	SpotBugs
•	ErrorProne
•	JProfiler
•	JMC

🔥 Power Prompt (Best Overall)

Use this as your master review prompt:

```shell
Act as a senior Java architect and perform a deep production-level review of this Spring Boot code.

Evaluate:
- Best practices
- Thread safety
- Memory management
- Performance
- Test coverage
- Maintainability
- Security
- Readability

Output:
1) Critical issues
2) Performance risks
3) Code smells
4) Missing tests
5) Refactoring suggestions
6) Overall production readiness score (1–10)

Be specific and actionable.
```
