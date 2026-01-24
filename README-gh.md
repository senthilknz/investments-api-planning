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
