# Shamsa / شمسا

A bilingual Persian/English Android reminder app built around **live reverse countdowns** and the **Solar Hijri (Jalali) calendar**.

## Product features

- Solar Hijri date-first reminder creation, display and timeline; no Gregorian date picker leaks into the user flow.
- Live reverse countdown cards with Compact, Cards and Focus layouts, subtle urgency pulse and animated countdown transitions.
- Search, Today/Upcoming/Completed filters and Due/Priority/Created sorting.
- Categories with editable icons and accent colors; reminders can choose category, icon, priority, color/gradient and full-screen behavior.
- Persian RTL and English LTR resources, locale switching, and weight-aware typography.
- First-run onboarding for language, notification permission, exact alarm access, full-screen reminder access and optional Drive setup.
- Exact `AlarmManager` delivery when Android grants exact-alarm access, with WorkManager fallback and boot/time-change rescheduling.
- High-priority notification actions for Complete and Snooze plus optional full-screen alarm UI when Android permits full-screen intents.
- Local-first Room database. Every reminder/category change survives offline and queues cloud/widget refresh work.
- Google Drive `appDataFolder` sync using Google Identity authorization, merge-by-updated-time conflict handling and deletion tombstones.
- Cloud restore reschedules restored alarms automatically.
- Glance home-screen widget showing the nearest reminders in the selected app language.
- Material 3 light/dark/system themes, animated navigation and adaptive launcher icon.

## Architecture

- Kotlin + Jetpack Compose + Material 3
- MVVM/state flows
- Hilt dependency injection
- Room + DataStore
- WorkManager + AlarmManager
- Glance AppWidget
- Kotlin serialization + OkHttp for Drive REST
- Google Identity AuthorizationClient

Dependencies are pinned to a modern known-compatible baseline and Dependabot checks Gradle/GitHub Actions weekly, so version bumps happen through reviewable pull requests instead of silently breaking production builds.

## Google Drive setup

Drive sync uses `https://www.googleapis.com/auth/drive.appdata`. Before Drive authorization can succeed in a distributable build:

1. Enable **Google Drive API** in a Google Cloud project.
2. Configure the OAuth consent screen.
3. Create an **Android OAuth client** for package `com.marble.shamsa`.
4. Register the SHA-1 of the app's release signing certificate.

The Termux deployer creates only the encrypted GitHub repository secret `SHAMSA_SIGNING_MASTER`. On the first `main` build, the GitHub-hosted runner creates the Android release JKS with `keytool`, encrypts that JKS on the runner, and commits only the encrypted JKS plus the public certificate SHA-1 to `.github/signing/`. The raw JKS and signing passwords are never committed. Future runs restore the same encrypted JKS, preserving the app signing identity.

After the first successful Actions run, use `.github/signing/release-cert-sha1.txt` for the Android OAuth client. No OAuth client secret belongs in the Android APK. Drive data is kept in the application's private `appDataFolder`, not the user's visible Drive root.

## Android platform constraints

Full-screen intents and exact alarms are permission/policy controlled on modern Android; Shamsa requests/uses them only when the OS permits them and keeps safe fallbacks. Home-screen widgets are rendered by the launcher, so Glance widgets cannot run the same continuous animations as in-app Compose UI.

## Signing and automatic versions

Release signing is initialized and maintained entirely on GitHub Actions. The only repository secret required by this source package is:

- `SHAMSA_SIGNING_MASTER`

The first trusted `main` workflow run creates the release JKS on the GitHub-hosted runner, encrypts it with the signing master, and stores only `.github/signing/shamsa-release.jks.enc` plus the public SHA-1 fingerprint in the repository. Future builds decrypt that same JKS on the runner, so the signing identity stays stable across versions.

CI derives a monotonic `versionCode` from GitHub run metadata and a `1.0.<run>` version name for `main` builds; tags such as `v1.2.0` become the release version name.

## Cloud build outputs

`.github/workflows/build.yml` performs unit tests, Android lint, signed release builds, ABI splits for `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`, a universal APK, AAB, SHA-256 checksums, source archive, Actions artifact upload and GitHub Release publishing.

GitHub Actions is the intended build environment. The repository does not need to be cloned by the Termux deployer.

## Cloud-only deployment note

The Termux deployer is only a GitHub bootstrap/uploader. It does **not** install or run Java, Gradle, Android SDK, OpenSSL, or keytool on the phone. Android release-key creation, keystore encryption/restoration, tests, lint, APK/AAB builds, signature verification, versioning, and release publishing all run on GitHub-hosted Actions runners. The deployer creates only the `SHAMSA_SIGNING_MASTER` GitHub Secret (from Android/Linux `/dev/urandom`) before publishing `main`; the secret never enters the repository.
