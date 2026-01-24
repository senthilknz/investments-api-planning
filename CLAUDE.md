# Claude Code Project Instructions

## GitHub Account Setup

This machine has multiple GitHub accounts configured:

| Account | SSH Host | Usage |
|---------|----------|-------|
| senthilknz | `github.com` (default) | Personal projects |
| ksktechai | `github.com-ksktechai` | Secondary account |

**At the start of each session, ask the user:**
> "Which GitHub account should we use for this session - **senthilknz** or **ksktechai**?"

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
