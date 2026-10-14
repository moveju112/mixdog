# Fork Ownership and Upstream Conflict Rules

`origin` = `https://github.com/moveju112/mixdog.git` (fork; push here).
`upstream` = `https://github.com/tribgames/mixdog.git` (original; fetch only).
Baseline: `v1.2.0` / `ab53d8226`; mobile baseline: `mobile-v0.1.13`.
The full tree is retained only to merge upstream updates; the fork customizes Android/mobile.

## Scope and traceability

- **[MUST]** Make fork product edits only under `apps/mobile/**`; an unavoidable outside edit requires a stated necessity, the same markers and a ledger entry.
  - why: preserving upstream-owned code is the fork's mergeability contract, not a license to customize the entire product.
  - ✅ F001 changes the native updater and shell fallback within mobile. `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:60`, `evidence: apps/mobile/src/main.ts:61`.

- **[MUST]** Record every fork change in [FORK_CHANGES.md](FORK_CHANGES.md); use sequential IDs `F001`, `F002`, …, never reuse an active or retired ID.
  - why: one intent may span several files and must remain reviewable after a merge.
  - ✅ F001 spans both updater constants and the web fallback. `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:65`, `evidence: apps/mobile/src/main.ts:66`.

- **[MUST]** Surround each fork code change with the exact Korean marker metadata below, using the file's comment syntax.
  - why: markers preserve the intent and default decision at the precise merge site.
  - ✅ The Java F001 block surrounds `RELEASES_API` and `DOWNLOAD_PREFIX`; the TypeScript F001 block surrounds `RELEASES_URL`. `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:60`, `evidence: apps/mobile/src/main.ts:61`.

Exact marker text (angle-bracket fields are filled in, not copied literally):

```text
[FORK:Fxxx] 시작 — <한 줄 요약>
원본: <upstream behavior>
포크: <fork behavior>
이유: <why>
원본 동일 수정 시: 원본 채택|포크 유지|병합 — <condition>
... changed code ...
[FORK:Fxxx] 끝
```

Choose exactly one of `원본 채택`, `포크 유지`, `병합`; the `|` above denotes alternatives.
Use `//` on each metadata line for Java/TypeScript/Gradle, `/* ... */` for CSS, and `<!-- ... -->` for XML/HTML.
The start and end delimiters belong in comments around the changed code, not around unrelated upstream code.
For a format without comments, do not invent invalid syntax: ask the user for an approved traceability alternative before editing.

- **[NEVER]** Change fork code without a marker or silently delete a FORK block; removal goes only through the retire procedure below.
  - why: an unrecorded removal destroys the fork intent even when a conflict appears resolved.
  - ✅ Both current update endpoints remain enclosed by F001 delimiters. `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:67`, `evidence: apps/mobile/src/main.ts:67`.

Rule files are the explicit exception: `AI_RULES.md`, `CLAUDE.md`, `AGENTS.md`, `docs/ai-rules/**`, `.rule-architect/**` are fork-owned additions recorded in the ledger's ownership section; no code markers are needed.

## Collision classification and priority

- **[MUST]** Classify each upstream collision by intent, using these cases rather than whichever side merges most easily.
  - why: correctness, fork identity and security take precedence over textual conflict convenience.
  - ✅ F001 is an identity change because both release API and fallback must keep pointing at the fork. `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:62`, `evidence: apps/mobile/src/main.ts:64`.

| Case | Situation | Required decision |
|---|---|---|
| a | Upstream implements the same bug fix or feature intent | Adopt upstream; remove the redundant block through retirement and record the upstream commit |
| b | Upstream changes surrounding code; fork intent is still needed | Reapply fork intent on the new upstream code and retain markers |
| c | Upstream moves in a conflicting direction | Present both sides and the marker's default decision; wait for user approval |
| d | Fork identity: repository URLs, signing, versioning, update channel (F001) | Always preserve fork identity, even if upstream changes the same lines |
| e | Upstream security fix | Adopt the security fix, then reapply fork intent without weakening the fix |

Identity case d overrides same-intent retirement for identity values; case e is mandatory for security fixes.
If a safe reapplication is unclear or other intent conflicts, use case c instead of deciding alone.

- **[NEVER]** Resolve with whole-file `git checkout --ours/--theirs` or merge `-X ours/theirs`, or rebase/reset `main` onto upstream.
  - why: these shortcuts can erase unrelated fork intent or upstream security changes without per-block review.
  - ✅ Review the paired F001 sites individually and use the merge-only [sync playbook](tasks/SYNC_UPSTREAM.md). `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:60`, `evidence: apps/mobile/src/main.ts:61`.

## Retire procedure

1. Confirm that upstream satisfies the same intent and that the block is not fork identity; record the implementing upstream commit.
2. Apply the selected upstream implementation and remove only the now-redundant marker block, preserving unrelated code and other IDs.
3. Mark the ledger ID `retired`, retain its original files/intent/baseline/default, append the retirement date and upstream commit to history. Never delete the ledger entry or reuse the ID.
4. Run the affected mobile checks and record the sync result using [tasks/SYNC_UPSTREAM.md](tasks/SYNC_UPSTREAM.md).

Publishing and Git mutations need task-scoped user authorization or the phone-app-update standing authorization in [AI_RULES.md](../../AI_RULES.md). That standing scope includes task-only commit, `origin/main` push and verified APK publication, not unrelated or destructive operations; explicit narrower requests override it.
