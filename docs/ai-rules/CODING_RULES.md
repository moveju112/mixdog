# Mobile Coding Rules

Fork ownership, Korean markers and the ledger are defined in [FORK_RULES.md](FORK_RULES.md); load it before editing.

- **[PREFER]** Follow the touched mobile file's existing naming, indentation and module style; avoid whole-file reformatting.
  - why: upstream-shaped code keeps future merges small and preserves recognizable contracts.
  - ✅ TypeScript uses camelCase functions, PascalCase types, two-space indentation, single-quoted ESM imports and explicit TypeScript extensions; Java uses camelCase methods. `evidence: apps/mobile/src/open-host.ts:1`, `evidence: apps/mobile/src/open-host.ts:17`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:462`.

- **[MUST]** Keep implementation identifiers and ordinary explanatory comments in English; write fork-marker metadata in Korean.
  - why: the upstream code remains readable while fork-only merge decisions stay visually distinct.
  - ✅ `openHostFlow` has an English flow comment; the F001 update URL block has Korean fork metadata. `evidence: apps/mobile/src/open-host.ts:15`, `evidence: apps/mobile/src/main.ts:61`.

- **[MUST]** Reuse `openHostFlow` when changing host opening: persist the book, sync origins, then navigate; retain cancellation checks and rollback.
  - why: navigating early can grant the wrong bridge origin or resurrect a cancelled pairing attempt.
  - ✅ `await deps.saveBook();` → cancellation check → `await deps.syncOrigins();` → cancellation check → navigation. `evidence: apps/mobile/src/open-host.ts:26`, `evidence: apps/mobile/src/main.ts:131`.

- **[MUST]** Keep origin policy behavior aligned across TypeScript, Android Java and iOS Swift, updating shared vectors for any intentional change.
  - why: disagreement between WebView authorization and native bridge authorization is a security boundary failure.
  - ✅ `origins.ts` names all three implementations and the shared vectors; the Java runner reads those same vectors. `evidence: apps/mobile/src/origins.ts:1`, `evidence: apps/mobile/scripts/test-origin-policy-java.mjs:10`.

- **[MUST]** Preserve native HTTP for device-side code redemption, with browser fetch only on non-native platforms.
  - why: the bundled page and relay have different origins; browser CORS failure hides the meaningful outdated-relay response.
  - ✅ `Capacitor.isNativePlatform() ? nativeFetch(input, init) : fetch(input, init)`. `evidence: apps/mobile/src/main.ts:39`, `evidence: apps/mobile/src/main.ts:45`.

- **[MUST]** Keep both English and Korean pairing strings consistent when adding a mobile UI action.
  - why: the shell selects one complete language table at runtime rather than translating missing entries.
  - ✅ `const strings = korean ? STRINGS.ko : STRINGS.en;` uses the tables declared in `pairing-ui.ts`. `evidence: apps/mobile/src/main.ts:75`, `evidence: apps/mobile/src/pairing-ui.ts:27`.

- **[PREFER]** Add mobile TypeScript regression tests beside the affected module and reuse shared native vectors instead of introducing another harness.
  - why: the existing mobile check already discovers adjacent tests and exercises native configuration.
  - ✅ `test` uses `src/**/*.test.ts`; `check` includes typecheck, tests, native verification and web bundling. `evidence: apps/mobile/package.json:10`, `evidence: apps/mobile/package.json:13`.

APK versioning, signing and publication belong to [tasks/RELEASE_APK.md](tasks/RELEASE_APK.md), not the root or mobile package's npm version.
