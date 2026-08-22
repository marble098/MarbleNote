# Shamsa — Google Drive OAuth checklist

Shamsa uses Google Identity Services `AuthorizationClient` with the private
Google Drive `appDataFolder` scope.

## Release identity

- Android package: `com.marble.shamsa`
- Release SHA-1:
  `EA:92:C9:39:1A:B9:31:B7:5A:EB:F5:DC:48:F3:69:9E:66:B2:58:83`
- Scope: `https://www.googleapis.com/auth/drive.appdata`

The release SHA-1 is generated from the persistent GitHub Actions signing key
and is also stored in `.github/signing/release-cert-sha1.txt`.

## Google Cloud requirements

1. Enable **Google Drive API** in the same Google Cloud project used by Shamsa.
2. Configure the OAuth consent screen.
3. Add the Drive `drive.appdata` scope in the consent/data-access configuration.
4. Create an **Android OAuth client** with the package and SHA-1 above.
5. If the app is in OAuth testing mode, add the Google account used for testing
   as a test user.
6. If Play App Signing is later used, register the Play App Signing SHA-1 as an
   additional Android OAuth client as well.

`8: INTERNAL_ERROR` is treated as transient and Shamsa retries it with
backoff. `10: DEVELOPER_ERROR` is treated as an OAuth client/configuration
error.

Do not rotate the Android signing key merely to troubleshoot OAuth; doing so
changes the SHA-1 and invalidates the existing Android OAuth client.
