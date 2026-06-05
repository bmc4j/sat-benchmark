# SAT solver benchmark results

_Run: [27021002105](https://github.com/bmc4j/sat-benchmark/actions/runs/27021002105) — 2026-06-05T14:44:31Z_

One string-free, SAT-dominated `@BmcProof` — bmc4j's own `BigDecimalLaws.setScale_widen_then_narrow_round_trips` at wide bound (division-heavy via the model's `roundDiv` rounding-divider) — run once per solver behind bmc4j's `--external-sat-solver` hook. `control-minisat` is jbmc's built-in MiniSat 2.2.1 (no external solver).

| Solver | Wall-clock (s) | Verdict | Speedup vs MiniSat | External solver engaged |
|---|---:|---|---:|---|
| kissat | 181.3 | VERIFIED | 2.45× | yes (2 external-solver invocation(s)) |
| cadical | 210.6 | VERIFIED | 2.10× | yes (2 external-solver invocation(s)) |
| **control-minisat** | 443.3 | VERIFIED | 1.00× | control (jbmc built-in MiniSat 2.2.1; no --external-sat-solver) |
| glucose | 707.1 | FAILED | — | yes (32 external-solver invocation(s)) |

> Speedup > 1.00× means faster than the built-in MiniSat control. Times include jbmc startup + bit-blasting (a fixed ~3-4s for all jobs), so the _difference_ between rows is the SAT-solve delta.
>
> **External solver engaged** is proof, per job, that jbmc actually shelled out to the external solver (via a logging wrapper on `--external-sat-solver`) rather than silently falling back to built-in MiniSat — a job with no invocations is failed, not reported.

