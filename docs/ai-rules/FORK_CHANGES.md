# Fork Changes Ledger

Last upstream sync: **v1.2.0 / ab53d8226**
Upstream mobile baseline: **mobile-v0.1.13**
Baseline full commit: `ab53d8226e1d73594180d5026aa6f313fb45513b`.

The ledger is append-preserving: retain retired entries and history. Marker and retirement requirements live in [FORK_RULES.md](FORK_RULES.md).
Next unused ID: **F002** (check all active and retired entries before allocation).

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

| Upstream mobile | n | versionName | versionCode | Date | Source SHA | Outcome | Release tag |
|---|---|---|---|---|---|---|---|
| `mobile-v0.1.13` | 1 | `0.1.13.1` | `101301` | 2026-10-13 | `765857f75` | published | `mobile-v0.1.13.1` |

On each authorized fork build append: upstream mobile version/tag, counter `n`, versionName, versionCode, build date, source SHA, outcome and release tag (if published).
Reserve/increment counters for builds even when publication fails; never reuse a counter already assigned to a fork build.
Use [tasks/RELEASE_APK.md](tasks/RELEASE_APK.md) to reset the counter after an upstream mobile version change and enforce increasing versionCode.
