# draw.io Diagram Skill

Generates polished `.drawio` XML files that open directly in [draw.io / diagrams.net](https://app.diagrams.net).

Supported diagram types: **flowcharts**, **architecture diagrams**, **sequence diagrams**, **ERDs**, and **mind maps**.

---

## Using in Claude Code

The skill is installed and triggers automatically. Just describe what you want:

```
Create a draw.io architecture diagram for the investments API
Make a flowchart for the portfolio rebalancing process
Generate an ERD for the holdings database tables
Draw a sequence diagram for the order execution flow
```

---

## Using in IntelliJ GitHub Copilot (Option A — `#file` reference)

IntelliJ Copilot does not support custom `/` prompt commands. Instead, reference the skill file directly in the chat panel using `#file:`.

### Syntax

```
#file:docs/skills/drawio/SKILL.md  <your diagram request>
```

### Examples

**Component diagram:**
```
#file:docs/skills/drawio/SKILL.md  create a draw.io component diagram for the investments API — show the Spring Boot controllers, services, repositories, and their dependencies
```

**Architecture diagram:**
```
#file:docs/skills/drawio/SKILL.md  create a draw.io architecture diagram for the investments microservice — include the API gateway, service layer, PostgreSQL database, and Redis cache
```

**Flowchart:**
```
#file:docs/skills/drawio/SKILL.md  create a draw.io flowchart for the trade order execution process — from order submission through validation, risk checks, execution, and settlement
```

**Sequence diagram:**
```
#file:docs/skills/drawio/SKILL.md  create a draw.io sequence diagram showing the OAuth2 authentication flow between the client, API gateway, and auth service
```

**ERD:**
```
#file:docs/skills/drawio/SKILL.md  create a draw.io ERD for the portfolio domain — tables: portfolios, holdings, transactions, instruments, with all foreign keys
```

### How to open the output

Once the `.drawio` file is generated, open it by:
- Dragging the file into [app.diagrams.net](https://app.diagrams.net), or
- Using **File → Open** in the draw.io desktop app

---

## Files

| File | Purpose |
|------|---------|
| `SKILL.md` | Full diagram generation guide (color palettes, shape styles, layout rules) |
| `README.md` | This file |
