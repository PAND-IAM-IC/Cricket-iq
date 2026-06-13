# How we set up personal GitHub for this project (plain-English notes)

These are notes-to-self explaining what we did and why, in normal words. If you ever
get a new laptop or forget how this works, read this.

---

## The problem we were solving

This is a **work MacBook**, and **GitHub Desktop already holds your company GitHub login.**
For this personal project, you wanted your **personal GitHub** to be used instead — with
zero chance of accidentally pushing code or commits under your work account.

---

## What an SSH key is (the simple version)

Think of GitHub as a locked building you enter many times a day. Instead of typing a
password every time, you use a matching pair — **a padlock and its one key**:

- **Public key = the padlock.** You give it to GitHub and they bolt it on your account's
  door. It's safe for anyone to see a padlock.
- **Private key = the only key that opens it.** It stays inside your Mac and never leaves.

When your Mac talks to GitHub, it proves "this is really me" by showing it owns the key
that fits the padlock. No passwords, and nobody can fake it.

On the Mac these are two files:
- `~/.ssh/id_ed25519_personal`  → the secret **key** (private — never share)
- `~/.ssh/id_ed25519_personal.pub` → the **padlock** (public — this is what we pasted into GitHub)

We made a **brand-new** pair just for personal use, so it never mixes with the existing
work key (`id_rsa`).

---

## What we did, step by step

1. **Looked at existing keys** so we wouldn't disturb the work one:
   ```
   ls -al ~/.ssh
   ```

2. **Made the new personal padlock + key:**
   ```
   ssh-keygen -t ed25519 -C "shauryastar777@gmail.com" -f ~/.ssh/id_ed25519_personal
   ```

3. **Created a little "address book" file** so the Mac knows when to use the personal key:
   ```
   touch ~/.ssh/config
   open -e ~/.ssh/config
   ```
   We pasted this into it:
   ```
   Host github.com-personal
       HostName github.com
       User git
       IdentityFile ~/.ssh/id_ed25519_personal
       IdentitiesOnly yes
       AddKeysToAgent yes
       UseKeychain yes
   ```
   In words: *"Whenever I use the nickname `github.com-personal`, connect to the real
   github.com but use ONLY the personal key."* That nickname is the on/off switch between
   personal and work.

4. **Saved the key in the Mac keychain** so you don't retype the passphrase constantly:
   ```
   ssh-add --apple-use-keychain ~/.ssh/id_ed25519_personal
   ```

5. **Handed the padlock to GitHub:** copied the public key and pasted it into
   personal GitHub → Settings → SSH and GPG keys → New SSH key.
   ```
   pbcopy < ~/.ssh/id_ed25519_personal.pub
   ```

6. **Tested it knocked on the right door:**
   ```
   ssh -T git@github.com-personal
   ```
   It answered `Hi PAND-IAM-IC!` — that's the personal account, so it worked.

7. **Told THIS project folder to sign commits as personal you** (not global, so work is
   unaffected):
   ```
   git init
   git branch -m main
   git config user.name "Shaurya Chaturvedi"
   git config user.email "shauryastar777@gmail.com"
   ```

---

## The ONE rule that keeps this working

When we connect this project to GitHub, the web address must use the **nickname**, like:

```
git@github.com-personal:PAND-IAM-IC/<repo-name>.git
```

NOT the normal `git@github.com:...`. The nickname is what flips it to "personal."
If a push ever fails or shows the wrong account, this is the first thing to check.

---

## Quick mental model

- **Work stuff** → GitHub Desktop, the old `id_rsa` key, your global git settings. Untouched.
- **This project** → the new personal key + the `github.com-personal` nickname +
  this folder's personal name/email. Isolated.

Two separate lanes that never cross.
