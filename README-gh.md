# GitHub CLI Setup and Commands

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
