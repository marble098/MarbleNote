# Shamsa — Google Drive OAuth setup

Shamsa uses Google Identity Services `AuthorizationClient` and stores its backup inside the private Drive **appDataFolder**.

## Runtime app identity

- Package: `com.marble.shamsa`
- Signing SHA-1: read it from the in-app Drive diagnostics card

## Google Cloud items to verify

1. Create or open the correct Google Cloud project.
2. Enable **Google Drive API** in that project.
3. Create an **Android OAuth client** for:
   - package name `com.marble.shamsa`
   - the exact SHA-1 of the installed build
4. If the Android client already exists, make sure it belongs to the same project where Drive API is enabled.
5. Install/update **Google Play services** on the device and sign in to at least one Google account.

## Optional values captured when running the cloud patch

- Project ID / name: `(not provided)`
- Android OAuth client ID: `(not provided)`

## Why INTERNAL_ERROR or DEVELOPER_ERROR can happen

- **DEVELOPER_ERROR / status 10**
  - package/SHA-1 mismatch
  - wrong Google Cloud project
  - Drive API not enabled
- **INTERNAL_ERROR / status 8**
  - Google Play services issue
  - Google account state issue
  - transient Play services failure
  - wrong Android OAuth configuration can still surface as an internal failure on some devices

## What this v7 patch improves

- clearer Drive diagnostics
- stronger bilingual user-facing error text
- better restore guidance for reinstall scenarios
- Markdown notes are included in Drive snapshots
- note ordering is preserved in cloud backups too
