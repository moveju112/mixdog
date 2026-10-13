# Fork Deployment Boundary

This fork publishes signed Android APKs, not upstream desktop/relay deployments.
Upstream workflows remain in the tree for merges. The Android tag trigger also remains present.
`evidence: .github/workflows/mobile-android.yml:12`.

- **[MUST]** Use [tasks/RELEASE_APK.md](tasks/RELEASE_APK.md) for every fork APK build and publication.
  - why: local fork signing and four-part versions are not the upstream workflow's release contract.
  - ✅ Gradle accepts explicit mobile version properties and signing environment variables. `evidence: apps/mobile/android/app/build.gradle:12`, `evidence: apps/mobile/android/app/build.gradle:24`.

- **[MUST]** Treat retained upstream Android workflow failures as expected, not as authorization to change `.github/**` or configure secrets.
  - why: tag builds require absent upstream Firebase/signing secrets and validate only three-part versions; the fork's verified local artifact is authoritative.
  - ✅ The workflow's `mobile-v*` trigger, three-part regex and secret gate explain the mismatch. `evidence: .github/workflows/mobile-android.yml:15`, `evidence: .github/workflows/mobile-android.yml:112`, `evidence: .github/workflows/mobile-android.yml:156`.

- **[MUST]** Report FCM push as unavailable until the user explicitly sets up Firebase for the fork.
  - why: a signed APK alone does not provision Firebase; this fork has no `google-services.json`.
  - ✅ Gradle applies the Google Services plugin only when its JSON configuration can be read. `evidence: apps/mobile/android/app/build.gradle:65`.

For upstream integration, use [tasks/SYNC_UPSTREAM.md](tasks/SYNC_UPSTREAM.md).
Do not run root deployment/release automation as a substitute for either fork playbook.
