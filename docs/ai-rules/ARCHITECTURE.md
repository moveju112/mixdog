# Architecture

## Fork boundary and baseline

This is `moveju112/mixdog`, a fork of `tribgames/mixdog`. The entire upstream tree is retained for merges; only the Android/mobile app is customized.
Baseline: upstream `v1.2.0`, commit `ab53d8226`; upstream mobile `mobile-v0.1.13`.
The root package's version is not the mobile release version (`evidence: package.json:3`, `evidence: apps/mobile/package.json:3`).
For ownership and exceptions, read [FORK_RULES.md](FORK_RULES.md).

## Directory map and stack

| Area | Role / technology | Fork treatment |
|---|---|---|
| `apps/mobile/src/` | TypeScript pairing shell, host book, native HTTP adapter | Primary customization surface |
| `apps/mobile/android/` | Capacitor Android shell, Java native bridge/updater, Gradle | Primary customization surface |
| `apps/mobile/ios/` | Swift native implementation of shared mobile contracts | Retain upstream; Android is the fork's purpose |
| `apps/mobile/scripts/` | Web bundling and native contract checks | Reuse mobile commands |
| `apps/mobile/test-vectors/` | Shared native/web security vectors | Keep implementations consistent |
| `apps/desktop/` | Desktop app and shared native-app adapter | Upstream-owned dependency |
| `apps/relay/` | Relay web UI and pairing service | Upstream-owned |
| `src/`, `native/`, `scripts/` | Agent/CLI runtime, native helpers, upstream tooling | Retained for upstream merges |
| `.github/workflows/` | Upstream automation, including Android tag builds | Retain; use fork release playbook |
| `docs/ai-rules/`, `.rule-architect/` | Fork rules and generation audit state | Fork-owned additions |

The root is an ESM Node package; mobile uses Capacitor 7, TypeScript, esbuild and Android Java 21.
`evidence: package.json:6`, `evidence: apps/mobile/package.json:6`, `evidence: apps/mobile/package.json:21`, `evidence: apps/mobile/android/app/capacitor.build.gradle:5`.

## Data flow and subsystem roles

1. The bundled `main.ts` shell renders the pairing flow (`pairing-ui.ts`), scans a QR code or redeems a six-digit code.
   `evidence: apps/mobile/src/main.ts:1`, `evidence: apps/mobile/src/main.ts:24`.
2. Native HTTP handles code redemption on devices; browser fetch remains the web fallback.
   `evidence: apps/mobile/src/main.ts:39`.
3. `openHostFlow` remembers the host, persists it, syncs the native allowlist, then navigates to the relay. Cancellation restores the previous book.
   `evidence: apps/mobile/src/open-host.ts:17`, `evidence: apps/mobile/src/main.ts:131`.
4. The post-pairing UI is served by the relay, not bundled into the APK.
   `evidence: apps/mobile/scripts/build-web.mjs:1`.
5. Android owns update state, release selection, download and APK verification; the shell mirrors state and supplies a fallback releases link.
   `evidence: apps/mobile/src/main.ts:48`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:428`.

## Build and contract boundaries

`build:web` regenerates `apps/mobile/www/**` from `apps/mobile/src/main.ts` and copies existing desktop/design assets; those inputs are dependencies, not permission to edit upstream-owned trees.
`evidence: apps/mobile/scripts/build-web.mjs:9`, `evidence: apps/mobile/scripts/build-web.mjs:14`, `evidence: apps/mobile/scripts/build-web.mjs:26`.

Capacitor consumes the generated web output with application ID `io.mixdog.app`.
`evidence: apps/mobile/capacitor.config.json:2`, `evidence: apps/mobile/capacitor.config.json:4`.

Origin policy is a shared TypeScript/Java/Swift contract pinned by test vectors, not three independent policies.
`evidence: apps/mobile/src/origins.ts:1`, `evidence: apps/mobile/scripts/test-origin-policy-java.mjs:10`.

No controller/API/ORM rule documents were selected by the scan. Repository-wide enum and recurring-release signals were inspected and omitted as out of scope; deployment has a narrow fork-specific document.
