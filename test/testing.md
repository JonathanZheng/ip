# Automated testing

Use Java 25. The suite targets nearly all non-GUI production code, superseding the
earlier approximately 50% testing target for this branch. No application behavior
is changed by this testing work.

## Commands

```bash
./gradlew check
./gradlew testChineseLocale
```

`check` runs JUnit, both Checkstyle tasks, generates the coverage report, and enforces
at least 99% line coverage and 97% branch coverage for non-GUI production classes.
`testChineseLocale` runs the same suite in a fresh JVM with `zh-CN` defaults.
For just JUnit and the report, run `./gradlew test jacocoTestReport`.
Use `gradlew.bat` instead of `./gradlew` on Windows.

Generated reports (do not commit these):

- `build/reports/tests/test/index.html`: default-locale JUnit results.
- `build/reports/tests/testChineseLocale/index.html`: Chinese-locale results.
- `build/reports/jacoco/test/html/index.html`: line and branch coverage.
- `build/reports/jacoco/test/jacocoTestReport.xml`: machine-readable coverage.

JaCoCo 0.8.14 is pinned because its
[release notes](https://www.jacoco.org/jacoco/trunk/doc/changes.html) specify Java 25 support.
Only `SevenSixGui`, `DialogBox`, and `Launcher` are excluded from the report and gate.
The console interface, including `SevenSix.main`, remains in scope.

## Coverage and test design

The tests exercise these behaviors through public APIs, without reflecting into
private methods or changing production code for coverage:

- Every task constructor, completion transition, type marker, and display format.
- List ordering, removal, replacement, collection ownership, Unicode searches,
  duplicate identity, and changes to each event endpoint field.
- Every supported date/time format, leap-day validation, midnight/noon boundaries,
  malformed commands, and repeated or missing parameters.
- Exact storage records, invalid field counts, escaping, corrupt input, file
  permissions, atomic replacement, empty files, and blocked writes.
- Successful command workflows, persistence across restart, failed-command rollback,
  copied undo histories, and independent snapshots of all task types.
- Exact console separators, greeting, startup warnings, EOF, CRLF input, and stopping
  at `bye` without processing later lines.
- Real child-JVM startup with missing/blank configuration, default paths, Unicode
  paths containing spaces, and an obstructed data directory.
- English, Chinese, and Turkish locale behavior for dates and case-insensitive search.
- Assertion contracts for invalid internal model, parser, and list operations.

Filesystem fixtures use JUnit temporary directories. Real console processes have
isolated working directories and timeouts. Their JaCoCo execution data is appended
to the parent test report using an absolute destination. Tests restore process-wide
streams, properties, and locale settings after use. POSIX permission and symlink
tests use assumptions on unsupported platforms; inspect skipped results on each OS.

## Recorded result

On 2026-09-14, macOS 26.5.1 with Zulu JavaFX JDK 25.0.3:

| Check | Result |
| --- | --- |
| Default-locale JUnit | 162 passed, none skipped |
| Chinese-locale JUnit | 162 passed, none skipped |
| Non-GUI line coverage | 511/512 (99.8%) |
| Non-GUI branch coverage | 264/267 (98.9%) |
| Non-GUI method coverage | 152/152 (100%) |
| Checkstyle and coverage gates | Passed |
| Recorded console UI cases | 16 passed |

The previous suite had 63 tests, 85.9% line coverage, and 77.5% branch coverage with
the same GUI exclusions. Coverage measures execution, not proof of correctness.

The remaining uncovered line is the defensive catch for failure to delete an
abandoned temporary file. Reproducing that requires filesystem fault injection or
a timing-dependent permission change during save. Three remaining branches in
`TaskValidation` cover blank descriptions already rejected by constructors and
mismatched model subtypes with identical type markers. These are not reached by
valid application-created tasks. They remain visible in the report rather than
being excluded to inflate coverage.

See [the manual GUI plan](gui-test-plan.md) for rendering checks and the pending
cross-platform, physical input, display-scaling, and OS-language matrix. JVM locale
tests do not establish compatibility with a different OS or input method.
