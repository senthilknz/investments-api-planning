# Claude Code Project Instructions

## FIRST THING - REQUIRED

**STOP. Before doing anything else, ask the user:**
> "Which GitHub account should we use for this session - **senthilknz** or **ksktechai**?"

Do not proceed with any other tasks until this is answered.

---

## GitHub Account Setup

This machine has multiple GitHub accounts configured:

| Account | SSH Host | Usage |
|---------|----------|-------|
| senthilknz | `github.com` (default) | Personal projects |
| ksktechai | `github.com-ksktechai` | Secondary account |

**Before any `git push`, verify:**
```bash
git remote -v
```
Confirm the remote matches the intended account before pushing.

## Current Repository

- **Repo:** investments-api-planning
- **Default account:** senthilknz
- **Remote:** git@github.com:senthilknz/investments-api-planning.git

## Reference

See `README-gh.md` for full GitHub CLI setup and multi-account management guide.

## Git Workflow
- Always commit and push changes after completing each task unless explicitly told otherwise.
- Use clear, descriptive commit messages summarising the "why" not just the "what".
- Before any git push, run `git remote -v` and confirm the remote matches the intended account.
- When committing, include `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>` in the commit message.

## Naming & Conventions
- NEVER rename projects, files, packages, or variables that appear to be typos without asking first.
- Assume ALL existing names are intentional (e.g., `xapi-wone-investments` is correct, not a typo).
- When in doubt about any naming, STOP and ask before proceeding.

## API Design Standards
- This project follows a standard **ApiRequest/ApiResponse envelope pattern** for REST APIs.
- Do NOT add endpoints, classes, or fields beyond what is explicitly requested.
- When designing new APIs, ask for an example from an existing project before assuming patterns.
- Reference existing controllers and DTOs in `src/main/java/**/api/` to understand established patterns before generating new code.

## Scope Discipline
- ONLY implement exactly what is requested. Nothing more.
- Do NOT proactively add extra endpoints, classes, features, or refactorings.
- If you think something additional is needed, **suggest it but do not implement it**.
- When given a list of items to create, create ONLY those items.

## Documentation
- When creating documentation with diagrams, use **PlantUML rendered to PNG** for Bitbucket/Confluence compatibility.
- Store PlantUML source in `docs/diagrams/plantuml/` and PNGs in `docs/diagrams/images/`.
- Optionally keep Mermaid fallback versions in `docs/diagrams/mermaid/` for GitHub/GitLab viewing.
- Embed PNGs directly in markdown rather than relying on PlantUML or Mermaid rendering.

## Testing Standards
- For integration/component tests, use **SpringBoot + WireMock** to test the full stack with mocked external dependencies.
- Use Spring `RestClient` (not TestRestTemplate or WebClient) for test HTTP calls.
- Store reusable WireMock stubs as JSON files in `src/test/resources/wiremock/`.
- Follow existing test conventions: `@Nested`, `@DisplayName`, AssertJ assertions.

## Session Start Checklist
1. Read and follow ALL instructions in this file before doing anything else.
2. Ask which GitHub account to use (senthilknz or ksktechai).
3. Verify `git remote -v` matches the intended account.
4. When working on API design, read existing project patterns before generating code.

