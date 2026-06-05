# SAT solver benchmark results

_Run: [27019760321](https://github.com/bmc4j/sat-benchmark/actions/runs/27019760321) — 2026-06-05T14:20:38Z_

One string-free, SAT-dominated `@BmcProof` (the Euclidean division identity over wide symbolic operands) run once per solver behind bmc4j's `--external-sat-solver` hook. `control-minisat` is jbmc's built-in MiniSat 2.2.1 (no external solver).

| Solver | Wall-clock (s) | Verdict | Speedup vs MiniSat | External solver engaged |
|---|---:|---|---:|---|
| cadical | 200.6 | VERIFIED | 2.37× | yes (2 external-solver invocation(s)) |
| kissat | 225.6 | VERIFIED | 2.11× | yes (2 external-solver invocation(s)) |
| cryptominisat | 241.4 | VERIFIED | 1.97× | yes (2 external-solver invocation(s)) |
| **control-minisat** | 475.3 | VERIFIED | 1.00× | control (jbmc built-in MiniSat 2.2.1; no --external-sat-solver) |
| glucose | 695.2 | FAILED | — | yes (32 external-solver invocation(s)) |

> Speedup > 1.00× means faster than the built-in MiniSat control. Times include jbmc startup + bit-blasting (a fixed ~3-4s for all jobs), so the _difference_ between rows is the SAT-solve delta.
>
> **External solver engaged** is proof, per job, that jbmc actually shelled out to the external solver (via a logging wrapper on `--external-sat-solver`) rather than silently falling back to built-in MiniSat — a job with no invocations is failed, not reported.

