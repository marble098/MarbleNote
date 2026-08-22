# Shamsa — Google Drive OAuth and restore

Shamsa uses Google Identity Services `AuthorizationClient` with the private
Google Drive `appDataFolder` scope.

## Release identity

- Android package: `com.marble.shamsa`
- Release SHA-1:
  `EA:92:C9:39:1A:B9:31:B7:5A:EB:F5:DC:48:F3:69:9E:66:B2:58:83`
- Scope: `https://www.googleapis.com/auth/drive.appdata`
- Google Play services auth library: `21.5.0+`

The release SHA-1 comes from the persistent GitHub Actions signing key and is
also stored in `.github/signing/release-cert-sha1.txt`.

## Required Google Cloud configuration

All of these must belong to the same Google Cloud project:

1. Google Drive API is enabled.
2. The OAuth consent screen / Google Auth Platform data access includes
   `https://www.googleapis.com/auth/drive.appdata`.
3. An Android OAuth client exists with:
   - package `com.marble.shamsa`
   - SHA-1 `EA:92:C9:39:1A:B9:31:B7:5A:EB:F5:DC:48:F3:69:9E:66:B2:58:83`
4. If the app audience is in Testing, the account used on the phone is an
   allowed test user.
5. If Play App Signing is introduced later, its app-signing SHA-1 must be
   registered as an additional Android OAuth client.

No client secret should be embedded in the Android application.

## Runtime error meanings

- `8: INTERNAL_ERROR` is emitted by Google Play services as an internal error.
  Shamsa retries and offers an authorization reset. If it remains reproducible,
  inspect Play services and the Cloud configuration above.
- `10: DEVELOPER_ERROR` means the application is misconfigured, normally an
  Android OAuth identity mismatch.

## Second restore layer

Shamsa v6 also allows `shamsa.db` to participate in Android/Google cloud backup
and device transfer. The app DataStore is intentionally not included, because
it contains the short-lived Drive access token and device-local settings.

This second layer does not replace Drive sync and is subject to the user's
Android backup settings and the device/OEM backup transport. It does make
reminders, notes and categories eligible for restore after reinstall when the
system backup service is enabled.
