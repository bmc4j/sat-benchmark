# SAT solver benchmark results

_Run: [27014537245](https://github.com/bmc4j/sat-benchmark/actions/runs/27014537245) — 2026-06-05T12:56:29Z_

One string-free, SAT-dominated `@BmcProof` (the Euclidean division identity over wide symbolic operands) run once per solver behind bmc4j's `--external-sat-solver` hook. `control-minisat` is jbmc's built-in MiniSat 2.2.1 (no external solver).

| Solver | Wall-clock (s) | Verdict | Speedup vs MiniSat | External solver engaged |
|---|---:|---|---:|---|
| glucose | 1879.7 | FAILED | — | yes (27 external-solver invocation(s)) |
| cadical | 1899.8 | FAILED | — | yes (2 external-solver invocation(s)) |
| **control-minisat** | 1904.0 | FAILED | — | control (jbmc built-in MiniSat 2.2.1; no --external-sat-solver) |
| kissat | 1905.9 | FAILED | — | yes (2 external-solver invocation(s)) |
| cryptominisat | 1916.4 | FAILED | — | yes (2 external-solver invocation(s)) |

> Speedup > 1.00× means faster than the built-in MiniSat control. Times include jbmc startup + bit-blasting (a fixed ~3-4s for all jobs), so the _difference_ between rows is the SAT-solve delta.
>
> **External solver engaged** is proof, per job, that jbmc actually shelled out to the external solver (via a logging wrapper on `--external-sat-solver`) rather than silently falling back to built-in MiniSat — a job with no invocations is failed, not reported.

