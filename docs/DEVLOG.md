# AIPlane — Development Log

A running journal of **engineering decisions, trade-offs, and dead ends**.

This is intentionally separate from [`CHANGELOG.md`](../CHANGELOG.md):

| | CHANGELOG | DEVLOG |
|---|---|---|
| Audience | Users / adopters | Contributors / future-you |
| Answers | *What* shipped (by version) | *Why* we chose it, and what we rejected |
| Tone | Concise, release-oriented | First-person narrative |

## Format

**One file:** this document (`docs/DEVLOG.md`), with reverse-chronological dated
entries (`## YYYY-MM-DD — title`). Newest entry at the top.

We chose a single file over `docs/devlog/YYYY-MM-DD-slug.md` because the project
is still early and a showcase benefits from one searchable narrative. If this
file grows past ~a few dozen entries or parallel authors start colliding, split
into dated files then — do not fork the format prematurely.

### When to add an entry

See [`CONTRIBUTING.md`](../CONTRIBUTING.md). Short version: non-trivial architecture
decisions, explicit trade-offs, and dead ends that someone would otherwise have to
reverse-engineer from git history.

---

## 2026-07-22 — JdbcTemplate first, JPA later (and living with the SPEC drift)

I scaffolded `api-server` with `spring-boot-starter-jdbc` and wrote the first
integration test against raw `JdbcTemplate` + Flyway + Testcontainers. That was
the fastest path to prove "Spring Boot boots, migrations apply, health is up."

`docs/SPEC.md` §1 still lists **Spring Data JPA** as the persistence story. I did
not resolve that contradiction in the scaffold PR on purpose: renaming the data
access approach mid-scaffold would have bloated an already large "get the modular
monolith compiling" change. The Cursor rule in
`.cursor/rules/spring-boot-backend.mdc` now calls the drift out so Phase 1 Prompt
CRUD cannot pretend it does not exist.

What I almost did and rejected: adding JPA entities with no repositories and no
endpoints "for later." Empty JPA on the classpath looks like progress and is
actually debt — Hibernate would idle next to an unused entity model while Flyway
owned the real schema. Prefer one honest stack until Prompt CRUD forces the
choice: either adopt JPA for real in that PR, or update the SPEC to say
JdbcTemplate / Spring JDBC is the deliberate approach.

Related: [#13](https://github.com/madmmas/aiplane/issues/13) / [PR #43](https://github.com/madmmas/aiplane/pull/43),
backend testing follow-up [#48](https://github.com/madmmas/aiplane/issues/48).

---

## 2026-07-16 — Rebranded the product, left the Java package as `aimanager`

The UI, docs, and Maven `groupId` became **AIPlane** / `dev.madmmas.aiplane`. The
Java package root is still `dev.madmmas.aimanager`. That is not an oversight I
forgot to finish in the same PR — it is a deliberate deferral.

A full package rename touches every `.java` file, logging config in
`application.yml`, both Dockerfiles, and any import path a future contributor
copy-pastes from the SPEC. Doing it inside the rebrand PR would have mixed a
marketing/docs change with a mechanical blast radius that is hard to review and
easy to get subtly wrong (one missed import, one stale log category). The SPEC
still documents `dev.madmmas.aimanager` as the package root for that reason.

The cost is cognitive: newcomers will wonder if "aimanager" means the product
was renamed incompletely. The answer lives here — use AIPlane everywhere
user-facing; treat the Java package rename as its own issue when we are ready to
land it as a focused chore, not as drive-by cleanup inside a feature PR.

Related: [#5](https://github.com/madmmas/aiplane/issues/5) / [PR #32](https://github.com/madmmas/aiplane/pull/32).

---

## 2026-07-22 — OSS standard setup as a mergeable baseline, not a wish list

Issue [#7](https://github.com/madmmas/aiplane/issues/7) asked for the usual open-source
floor: license, community docs, lint/format, CI, Dependabot, and branch protection.
I treated that as "make `main` safe to collaborate on," not "document what we hope
to do later."

What mattered in practice:

- **Biome over ESLint/Prettier** so the frontend monorepo has one formatter/linter
  story. Adding a second tool in the same role is how config drifts start.
- **Husky blocking direct commits to `main`** locally, matching GitHub branch
  protection that requires the `ci` job. Protection that only exists on GitHub
  still lets you push a broken main from a laptop that skipped the UI.
- Closing the loop in [PR #35](https://github.com/madmmas/aiplane/pull/35) after the
  earlier OSS PR, so the issue tracker and the repo state agreed.

What I did not bolt on in that pass (and should not pretend was done): CODEOWNERS,
CHANGELOG, DEVLOG, and real test execution in CI. Those became follow-up hygiene
issues so the foundation PR stayed reviewable. A showcase repo that claims
"contributions welcome" without a green required check and a documented workflow
is performative — the baseline had to be real first.

Related: [#7](https://github.com/madmmas/aiplane/issues/7) / [PR #35](https://github.com/madmmas/aiplane/pull/35),
workflow notes in [`ISSUE_WORKFLOW.md`](ISSUE_WORKFLOW.md).
