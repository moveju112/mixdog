# Fork Changes Ledger

Last upstream sync: **v1.2.0 / ab53d8226**
Upstream mobile baseline: **mobile-v0.1.13**
Baseline full commit: `ab53d8226e1d73594180d5026aa6f313fb45513b`.

The ledger is append-preserving: retain retired entries and history. Marker and retirement requirements live in [FORK_RULES.md](FORK_RULES.md).
Next unused ID: **F007** (check all active and retired entries before allocation).

## F001 — Fork update channel

| Field | Value |
|---|---|
| Status | active |
| Files | `apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java`; `apps/mobile/src/main.ts` |
| Summary | Automatic APK update lookup/download and fallback link target `moveju112/mixdog` releases |
| Upstream behavior | `RELEASES_API`, `DOWNLOAD_PREFIX` and `RELEASES_URL` target `tribgames/mixdog` releases |
| Fork behavior | API: `https://api.github.com/repos/moveju112/mixdog/releases?per_page=20`; download prefix: `https://github.com/moveju112/mixdog/releases/download/`; fallback: `https://github.com/moveju112/mixdog/releases` |
| Reason | The fork APK has its own signing key and must never update from the original repository or lose fork customization |
| Default decision | **Fork keeps** (`포크 유지`); identity case d, reapply alongside upstream security fixes |
| Baseline upstream commit | `ab53d8226` (`v1.2.0`, upstream mobile `0.1.13`) |

Evidence: `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:60`, `evidence: apps/mobile/src/main.ts:61`.

### History

- 2026-10-13: introduced in commit `765857f75` on top of `v1.2.0`.
- 2026-10-13: TypeScript marker completed with its condition (release links always target the fork).
- No upstream retirement commit; identity remains active.

## F002 — Navigation bar always visible

| Field | Value |
|---|---|
| Status | active |
| Files | `apps/mobile/android/app/src/main/java/io/mixdog/app/MainActivity.java` (`onWindowFocusChanged`) |
| Summary | Disable the immersive shell so a single swipe reaches Home |
| Upstream behavior | Hides the navigation bar and shows it transiently on edge swipe (`BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`) |
| Fork behavior | Shows the navigation bar whenever the window gains focus |
| Reason | With the bar hidden, leaving the app needed two upward swipes |
| Default decision | **Adopt upstream** (`원본 채택`) if upstream also keeps the navigation bar visible; otherwise case b |
| Baseline upstream commit | `ab53d8226` (`v1.2.0`, upstream mobile `0.1.13`) |

Evidence: `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MainActivity.java:140`.

History: 2026-10-13 introduced for fork build `0.1.13.002`.

## F003 — Wider phone session drawer

| Field | Value |
|---|---|
| Status | active |
| Files | `apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogNativeHost.java` (`register`, `DRAWER_WIDTH_SCRIPT`) |
| Summary | Inject a document-start style that sets the portrait drawer width to `89vw`, halving the empty band on the right |
| Upstream behavior | The relay web app sizes the drawer with `--mx-side-sheet-width` (78% of the screen) in `apps/desktop/src/renderer/desktop/07-mobile-chrome.css` |
| Fork behavior | `html[data-mixdog-mobile-tabs]{--mx-side-sheet-width:89vw !important}` in portrait; landscape stays upstream |
| Reason | More room for session titles without forking the PC app (the phone page is served by the PC) |
| Default decision | **Merge** (`병합`): adopt upstream if it widens the drawer; if the variable or selector is renamed, reapply with the new name |
| Coupling | Depends on upstream CSS names `--mx-side-sheet-width` and `data-mixdog-mobile-tabs`; on every sync, grep both in `apps/desktop/src/renderer/desktop/` — a rename silently disables the override |
| Baseline upstream commit | `ab53d8226` (`v1.2.0`, upstream mobile `0.1.13`) |

Evidence: `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogNativeHost.java:83`.

History: 2026-10-13 introduced for fork build `0.1.13.003`.

## F004 — Launch straight into the saved PC

| Field | Value |
|---|---|
| Status | active |
| Files | `apps/mobile/src/main.ts` (`boot`, `renderLaunching`) |
| Summary | With a saved host, show only "연결 중…" and navigate right after `syncOrigins`; the QR/pairing screen is skipped |
| Upstream behavior | Renders the pairing screen first, reads the cached update state, then opens the saved host; a pending update offer keeps the pairing screen |
| Fork behavior | Saved host and no `?manage` → launching note → `openHost(book.last)`; connection checks and the update banner are left to the relay web app (`AppUpdateBanner`) |
| Reason | The QR screen flashed on every launch |
| Default decision | **Adopt upstream** (`원본 채택`) if upstream also skips the pairing screen for saved hosts |
| Baseline upstream commit | `ab53d8226` (`v1.2.0`, upstream mobile `0.1.13`) |

Evidence: `evidence: apps/mobile/src/main.ts:414`, `evidence: apps/mobile/src/main.ts:103`.

History: 2026-10-13 introduced for fork build `0.1.13.003`.

## F005 — Update lookup on every app launch

| Field | Value |
|---|---|
| Status | active |
| Files | `apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java` (`LAUNCH_LOOKUP_DONE`, `get`) |
| Summary | The first update-state request of each process looks up releases regardless of the 6-hour interval |
| Upstream behavior | Looks up only when 6 hours (`CHECK_INTERVAL_MS`) passed since the last lookup, even across restarts |
| Fork behavior | One lookup per process start, then the upstream interval; resuming from background does not force a lookup |
| Reason | New fork releases stayed invisible for up to 6 hours after restarting the app |
| Default decision | **Adopt upstream** (`원본 채택`) if upstream adds a launch-time lookup |
| Baseline upstream commit | `ab53d8226` (`v1.2.0`, upstream mobile `0.1.13`) |

Evidence: `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:79`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:194`.

History: 2026-10-13 introduced for fork build `0.1.13.003`.

## F006 — Web links open in the system browser on the phone

| Field | Value |
|---|---|
| Status | active |
| Files | `apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogNativeHost.java` (`register`, `SYSTEM_BROWSER_LINK_SCRIPT`, `openExternalUrl`) |
| Summary | A document-start window-capture click handler sends unmodified taps on cross-origin `http(s)` `a[href]` links to `openExternalUrl` (Android `ACTION_VIEW`) |
| Upstream behavior | `linkOpenTarget` (`apps/desktop/src/renderer/link-open-target.ts`) opens chat web links in the session's Mixdog browser pane (Browser Use) |
| Fork behavior | Phone link taps go to the system browser; same-origin, non-web, modified and long-press menu actions are untouched; agent-opened pages still use the pane |
| Reason | Browser Use is not wanted on the phone |
| Default decision | **Merge** (`병합`): adopt upstream if the phone default becomes the system browser; reapply if link DOM changes |
| Coupling | Depends on chat web links being `a[href]` with the target URL (`apps/desktop/src/renderer/MarkdownLink.tsx`) and on `data-mixdog-mobile-tabs`; check both on every sync |
| Baseline upstream commit | `ab53d8226` (`v1.2.0`, upstream mobile `0.1.13`) |

Evidence: `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogNativeHost.java:93`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogNativeHost.java:273`.

History: 2026-10-13 introduced for fork build `0.1.13.004`.

## Fork-owned files (no markers required)

These are additions owned by the fork, not modifications to upstream product code.

| Paths | Purpose |
|---|---|
| `AI_RULES.md` | Runtime-neutral rule index |
| `CLAUDE.md`, `AGENTS.md` | Relative symlinks to `AI_RULES.md` |
| `docs/ai-rules/ARCHITECTURE.md`, `docs/ai-rules/CODING_RULES.md`, `docs/ai-rules/PITFALLS.md` | Project map, mobile conventions and failure diagnosis |
| `docs/ai-rules/FORK_RULES.md`, `docs/ai-rules/FORK_CHANGES.md` | Fork intent, merge priorities and this ledger |
| `docs/ai-rules/DEPLOY.md` | Fork deployment/CI boundary |
| `docs/ai-rules/tasks/SYNC_UPSTREAM.md`, `docs/ai-rules/tasks/RELEASE_APK.md` | Upstream merge and signed APK publication playbooks |
| `.rule-architect/**` | Scan evidence, conditional decisions, generated-file manifest and future quiz records |

Ownership history: initial English v2 rule generation; shared symlink entry mode.
New rule/audit files under these owned paths extend this section's ownership without product markers.

## Fork build history

| Upstream mobile | NNN | versionName | versionCode | Date | Source | Outcome | Release tag |
|---|---|---|---|---|---|---|---|
| `mobile-v0.1.13` | 1 (pre-padding) | `0.1.13.1` | `101301` | 2026-10-13 | `765857f75` | published | `mobile-v0.1.13.1` |
| `mobile-v0.1.13` | 002 | `0.1.13.002` | `101302` | 2026-10-13 | release tag target | published | `mobile-v0.1.13.002` |
| `mobile-v0.1.13` | 003 | `0.1.13.003` | `101303` | 2026-10-13 | release tag target | published | `mobile-v0.1.13.003` |
| `mobile-v0.1.13` | 004 | `0.1.13.004` | `101304` | 2026-10-13 | release tag target | published | `mobile-v0.1.13.004` |

On each authorized fork build append: upstream mobile version/tag, build number `NNN`, versionName, versionCode, build date, source, outcome and release tag (if published).
Reserve numbers for builds even when publication fails; never reuse an `NNN` or a versionCode.
`NNN` resets to `001` after an upstream mobile version change; versionCode never resets (previous + 1). See [tasks/RELEASE_APK.md](tasks/RELEASE_APK.md).
