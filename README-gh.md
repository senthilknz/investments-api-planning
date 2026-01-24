# GitHub CLI Setup and Commands

> **Session Reminder:** When starting a new session with Claude Code, specify which GitHub account to use:
> - "We're pushing to **senthilknz** today"
> - "Use **ksktechai** for this project"
>
> This ensures commits and pushes go to the correct account.

## Initial Setup

### Install GitHub CLI (macOS)

```bash
brew install gh
```

### Authenticate with GitHub

```bash
gh auth login
```

Follow the prompts:
1. Select `GitHub.com`
2. Select preferred protocol: `SSH` (recommended if you have SSH keys configured)
3. Authenticate via browser or paste token

### Verify Authentication

```bash
gh auth status
```

Example output:
```
✓ Logged in as senthilknz
✓ Git operations for github.com configured to use ssh protocol
✓ Token: gho_****
```

## SSH Key Setup

If using SSH protocol, ensure your key is added to GitHub:

1. Generate SSH key (if needed):
   ```bash
   ssh-keygen -t ed25519 -C "your_email@example.com"
   ```

2. Add to ssh-agent:
   ```bash
   eval "$(ssh-agent -s)"
   ssh-add ~/.ssh/id_ed25519
   ```

3. Add public key to GitHub via Settings > SSH Keys, or:
   ```bash
   gh ssh-key add ~/.ssh/id_ed25519.pub --title "My Key"
   ```

4. Verify:
   ```bash
   gh ssh-key list
   ```

## Repository Commands

### Create a New Public Repository

```bash
gh repo create <repo-name> --public --source=. --push
```

Options:
- `--public` / `--private` - Repository visibility
- `--source=.` - Use current directory as source
- `--push` - Push local commits to the new repository

### Clone a Repository

```bash
gh repo clone <owner>/<repo>
```

### View Repository Info

```bash
gh repo view
gh repo view <owner>/<repo> --web  # Open in browser
```

## Pull Request Commands

### Create a Pull Request

```bash
gh pr create --title "PR Title" --body "Description"
```

### List Pull Requests

```bash
gh pr list
```

### View a Pull Request

```bash
gh pr view <pr-number>
gh pr view <pr-number> --web  # Open in browser
```

### Checkout a Pull Request

```bash
gh pr checkout <pr-number>
```

## Issue Commands

### Create an Issue

```bash
gh issue create --title "Issue Title" --body "Description"
```

### List Issues

```bash
gh issue list
```

### View an Issue

```bash
gh issue view <issue-number>
```

## Useful Tips

### Check Current Git Config

```bash
git config --global user.name
git config --global user.email
```

### Set Git Config

```bash
git config --global user.name "Your Name"
git config --global user.email "your_email@example.com"
```

### View All gh Commands

```bash
gh help
```

## Managing Multiple GitHub Accounts

### Option 1: Switch GitHub CLI Account

```bash
# Logout current account
gh auth logout

# Login with different account
gh auth login
```

This switches globally - only one account active at a time.

### Option 2: Multiple SSH Keys (Recommended)

1. Generate separate SSH keys for each account:
   ```bash
   ssh-keygen -t ed25519 -C "personal@email.com" -f ~/.ssh/id_ed25519_personal
   ssh-keygen -t ed25519 -C "work@company.com" -f ~/.ssh/id_ed25519_work
   ```

2. Add each public key to the respective GitHub account.

3. Configure `~/.ssh/config`:
   ```
   # Personal GitHub
   Host github.com-personal
     HostName github.com
     User git
     IdentityFile ~/.ssh/id_ed25519_personal

   # Work GitHub
   Host github.com-work
     HostName github.com
     User git
     IdentityFile ~/.ssh/id_ed25519_work
   ```

4. Clone using the host alias:
   ```bash
   git clone git@github.com-personal:username/repo.git
   git clone git@github.com-work:workuser/repo.git
   ```

5. For existing repos, update the remote:
   ```bash
   git remote set-url origin git@github.com-personal:username/repo.git
   ```

#### Concrete Example: senthilknz + ksktechai

1. Generate SSH key for ksktechai account:
   ```bash
   ssh-keygen -t ed25519 -C "your-email@example.com" -f ~/.ssh/id_ed25519_ksktechai
   ```

2. Copy and add public key to GitHub:
   ```bash
   cat ~/.ssh/id_ed25519_ksktechai.pub
   ```
   Add at: https://github.com/settings/keys (logged in as ksktechai)

3. Configure `~/.ssh/config`:
   ```
   # Primary account (senthilknz)
   Host github.com
     HostName github.com
     User git
     IdentityFile ~/.ssh/id_ed25519

   # Secondary account (ksktechai)
   Host github.com-ksktechai
     HostName github.com
     User git
     IdentityFile ~/.ssh/id_ed25519_ksktechai
   ```

4. Usage:
   ```bash
   # Clone as senthilknz (default)
   git clone git@github.com:senthilknz/repo.git

   # Clone as ksktechai
   git clone git@github.com-ksktechai:ksktechai/repo.git

   # Create repo for ksktechai account
   gh repo create repo-name --public --source=. --push
   git remote set-url origin git@github.com-ksktechai:ksktechai/repo-name.git
   ```

### Option 3: Per-Repository Git Config

Set different user identity per repo:

```bash
# Inside a specific repo
git config user.name "Work Name"
git config user.email "work@company.com"
```

### Option 4: Conditional Git Config (Directory-Based)

Automatically use different identities based on directory location.

In `~/.gitconfig`:
```
[user]
  name = Personal Name
  email = personal@email.com

[includeIf "gitdir:~/work/"]
  path = ~/.gitconfig-work

[includeIf "gitdir:~/personal/"]
  path = ~/.gitconfig-personal
```

In `~/.gitconfig-work`:
```
[user]
  name = Work Name
  email = work@company.com
```

This automatically applies work config to any repo under `~/work/`.

## Pre-Push Checklist

Before pushing, verify which account your repo is configured for:

### Quick Check Commands

```bash
# Check remote URL (shows which account)
git remote -v

# Check commit identity for this repo
git config user.name
git config user.email
```

### Understanding the Output

**Remote URL tells you the account:**
```
origin  git@github.com:senthilknz/repo.git           → senthilknz (default SSH key)
origin  git@github.com-ksktechai:ksktechai/repo.git  → ksktechai (custom SSH key)
```

**Commit identity shows who the author will be:**
```
Senthil Kumar
senthilknz.anz@gmail.com
```

### One-Liner Status Check

```bash
echo "Remote:" && git remote -v | head -1 && echo "Identity:" && git config user.name && git config user.email
```

### Switching Account for a Repo

If you need to push to a different account:

```bash
# Switch to ksktechai
git remote set-url origin git@github.com-ksktechai:ksktechai/repo-name.git
git config user.name "ksktechai"
git config user.email "ksktechai-email@example.com"

# Switch back to senthilknz
git remote set-url origin git@github.com:senthilknz/repo-name.git
git config user.name "Senthil Kumar"
git config user.email "senthilknz.anz@gmail.com"
```

### Best Practice: Directory-Based Organization

Keep repos organized by account:
```
~/senthilknz/    → All senthilknz repos
~/ksktechai/     → All ksktechai repos
```

Combined with conditional git config, this eliminates manual switching.
