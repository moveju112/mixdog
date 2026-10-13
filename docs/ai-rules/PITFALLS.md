# Pitfalls

## Update visible upstream but not on the fork

Symptom: `"draft"` or `"prerelease"` releases are skipped.
Cause: `MixdogAppUpdater.pick` explicitly filters both flags and requires a matching APK download prefix.
Fix: publish a non-draft, non-prerelease release in the fork via [RELEASE_APK step 9](tasks/RELEASE_APK.md).
`evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:432`.

## Original-signed app cannot accept the fork APK

Symptom: update verification reports `"verify"`.
Cause: the updater compares the archive and installed signing certificates and requires a larger versionCode.
Fix: preserve and back up the fork key; original `tribgames` installations must uninstall once before installing the fork, as in [RELEASE_APK step 3](tasks/RELEASE_APK.md).
`evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:618`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:665`.

## Push notifications absent

Symptom: `"google-services.json not found, google-services plugin not applied. Push Notifications won't work"`.
Cause: this fork has no Firebase configuration file; the Gradle plugin is conditional.
Fix: accept FCM as unavailable; Firebase setup is out of scope until explicitly requested. See [DEPLOY.md](DEPLOY.md).
`evidence: apps/mobile/android/app/build.gradle:65`, `evidence: apps/mobile/android/app/build.gradle:70`.

## Upstream Android tag workflow fails

Symptom: `"Version must be X.Y.Z, got '$version'"` or `"Missing secret $name"` in the workflow.
Cause: upstream CI expects three-part versions and configured Firebase/signing secrets; fork tags are four-part and the required secrets are absent.
Fix: this is expected for the retained upstream workflow; ignore that job, not local APK verification. Use [RELEASE_APK](tasks/RELEASE_APK.md), not workflow dispatch.
`evidence: .github/workflows/mobile-android.yml:113`, `evidence: .github/workflows/mobile-android.yml:156`.

## Browser-only preview invokes a native update method

Symptom: `"Native bridge unavailable."`.
Cause: update state and installation are Android-native; a browser preview has no native bridge.
Fix: preserve the fallback fork releases URL and the native/browser separation in [CODING_RULES.md](CODING_RULES.md).
`evidence: apps/mobile/src/main.ts:71`, `evidence: apps/mobile/src/main.ts:66`.

## Version sources are confused

Symptom: a local APK reports `"1.0.0"` instead of the planned mobile release version.
Cause: Gradle defaults apply if `mobileVersionName`/`mobileVersionCode` are omitted; root `1.2.0` and mobile npm `1.0.0` are not the upstream mobile tag version.
Fix: resolve the merged upstream three-part mobile tag, exclude fork four-part tags, then pass both properties in [RELEASE_APK steps 2 and 6](tasks/RELEASE_APK.md).
`evidence: apps/mobile/android/app/build.gradle:12`, `evidence: apps/mobile/android/app/build.gradle:13`.

## Native/web origin authorization diverges

Symptom: origin vector runner prints `"FAIL "`.
Cause: TypeScript, Java and Swift policies implement the same vectors; changing just one implementation drifts the security contract.
Fix: follow the shared-policy rule in [CODING_RULES.md](CODING_RULES.md) and run mobile checks.
`evidence: apps/mobile/scripts/test-origin-policy-java.mjs:18`, `evidence: apps/mobile/src/origins.ts:1`.

## Historical marker is incomplete

Symptom: F001's TypeScript marker contains `"원본 동일 수정 시: 포크 유지"` without a condition.
Cause: the pre-existing block predates the full required format.
Fix: do not copy the incomplete form; follow [FORK_RULES.md](FORK_RULES.md) on the next authorized edit, preserving F001's fork-identity default.
`evidence: apps/mobile/src/main.ts:65`.

No specific ongoing rename/migration was established from the inspected mobile history. Do not infer one from differing npm/mobile versions; those are distinct version namespaces.
