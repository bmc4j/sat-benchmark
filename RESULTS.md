# SAT solver benchmark results

_Run: [27019108792](https://github.com/bmc4j/sat-benchmark/actions/runs/27019108792) — 2026-06-05T14:05:04Z_

One string-free, SAT-dominated `@BmcProof` (the Euclidean division identity over wide symbolic operands) run once per solver behind bmc4j's `--external-sat-solver` hook. `control-minisat` is jbmc's built-in MiniSat 2.2.1 (no external solver).

| Solver | Wall-clock (s) | Verdict | Speedup vs MiniSat | External solver engaged |
|---|---:|---|---:|---|
| glucose | 393.2 | FAILED | — | yes (152 external-solver invocation(s)) |
| **control-minisat** | 395.5 | FAILED | — | control (jbmc built-in MiniSat 2.2.1; no --external-sat-solver) |
| cryptominisat | 399.2 | FAILED | — | yes (2 external-solver invocation(s)) |
| cadical | 403.1 | FAILED | — | yes (2 external-solver invocation(s)) |
| kissat | 409.0 | FAILED | — | yes (2 external-solver invocation(s)) |

> Speedup > 1.00× means faster than the built-in MiniSat control. Times include jbmc startup + bit-blasting (a fixed ~3-4s for all jobs), so the _difference_ between rows is the SAT-solve delta.
>
> **External solver engaged** is proof, per job, that jbmc actually shelled out to the external solver (via a logging wrapper on `--external-sat-solver`) rather than silently falling back to built-in MiniSat — a job with no invocations is failed, not reported.

