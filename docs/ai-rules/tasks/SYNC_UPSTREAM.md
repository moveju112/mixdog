# Sync Upstream

Use only with authorization for the named merge; this procedure does not authorize an unrelated commit, push, release or destructive operation.
Classifications and retirement are owned by [FORK_RULES.md](../FORK_RULES.md); state is in [FORK_CHANGES.md](../FORK_CHANGES.md).

1. Inspect `git status --short` and the current branch. Before merging, commit or branch away uncommitted work with user authorization; do not erase it or merge onto a dirty `main`. Confirm `origin` is `moveju112/mixdog` and `upstream` is `tribgames/mixdog` (fetch only).
   `evidence: apps/mobile/src/main.ts:61` (existing fork work must survive).
2. From the root, run `git fetch upstream --tags`. Select `upstream/main` or an upstream release tag `vX.Y.Z`; record its resolved commit and inspect the proposed changes before merging.
   `evidence: package.json:3` (baseline release namespace).
3. List blocks with `git grep -n "\[FORK:"`. For every active ledger entry, inspect upstream changes to its files with `git diff <last-sync>..upstream/main -- <files>`; when merging a release tag, use that tag instead of `upstream/main`. Initially `<last-sync>` is `ab53d8226`.
   `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:60`, `evidence: apps/mobile/src/main.ts:61`.
4. Classify each touched intent as a–e under FORK_RULES. If any case c exists, show a decision table **before resolving anything**, including ID/files, upstream intent, fork intent, marker default, proposed result and approval status; wait for the user's decision. Identity stays fork; security fixes land before safe reapplication.
   `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:64` (F001 default).
5. On clean `main`, run `git merge upstream/main` or `git merge vX.Y.Z`. Resolve approved conflicts per block; preserve unrelated upstream code. Never use rebase/reset onto upstream, whole-file ours/theirs or `-X ours/theirs`. If new case c conflicts emerge, stop resolution and return to step 4.
   `evidence: apps/mobile/src/main.ts:61`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:60`.
6. After applying the merged code, run `cd apps/mobile`, `npm run check` and `npm run test:java`. Diagnose failures without silently discarding fork intent or upstream fixes.
   `evidence: apps/mobile/package.json:13`, `evidence: apps/mobile/package.json:11`.
7. Update the ledger's last-sync line with the actual merged upstream ref and full commit; update active/retired statuses and append history. Retire redundant non-identity blocks only through FORK_RULES, including the upstream implementing commit. Resolve the merged upstream mobile tag using RELEASE_APK's three-part filter; if its version changed, the next fork build resets `n` to 1 while versionCode must still increase.
   `evidence: apps/mobile/android/app/build.gradle:12`, `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:665`.
8. Complete any conflict merge and ledger commit only within the approved Git scope. If pushing is authorized, use only `git push origin main`; never push to upstream, never push upstream tags, never `--tags`, never force. Otherwise report the local merge and leave it unpushed.
   `evidence: apps/mobile/android/app/src/main/java/io/mixdog/app/MixdogAppUpdater.java:65` (fork repository identity).

Decision table columns: `ID / files | upstream change | fork intent | case | marker default | proposed resolution | user approval`.
