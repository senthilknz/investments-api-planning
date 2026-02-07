# Commit & Push Skill

When the user runs `/commit`, follow these steps exactly:

## Pre-flight Checks
1. Run `git remote -v` and confirm the remote matches the intended GitHub account (senthilknz or ksktechai).
2. Run `git status` to see all changed and untracked files.
3. Run `git diff` to review staged and unstaged changes.
4. If any `.java` files were modified, run `mvn test` and ensure the build passes before proceeding. If tests fail, STOP and report the failures — do NOT commit broken code.

## Staging
5. Stage files by name — do NOT use `git add -A` or `git add .` to avoid accidentally committing sensitive files (.env, credentials, etc.).

## Commit Message
6. Run `git log --oneline -5` to check the repository's commit message style.
7. Generate a concise commit message that:
   - Summarises the "why" not just the "what"
   - Uses imperative mood (e.g., "Add", "Fix", "Update")
   - Keeps the first line under 72 characters
   - Adds bullet point details in the body if multiple things changed
   - Ends with `Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>`
8. Use a HEREDOC to pass the commit message for correct formatting.

## Push
9. Push to the current branch on origin.
10. Run `git status` after push to verify success.
11. Report the commit hash, summary, and branch name to the user.