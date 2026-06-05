# sat-benchmark

Benchmarks external SAT solvers behind [bmc4j](https://github.com/bmc4j/bmc4j)'s
`--external-sat-solver` hook. Implements [bmc4j#13](https://github.com/bmc4j/bmc4j/issues/13).

bmc4j verifies JVM `@BmcProof`s by shelling out to **jbmc**, which bit-blasts the proof to CNF and
hands it to a SAT solver. By default that's jbmc's built-in **MiniSat 2.2.1**. For string-free
numeric/boolean proofs, bmc4j can instead route the CNF at any external DIMACS solver via
`bmc { externalSat = "..." }` (or the `-PsatPath=` Gradle property, the opt-in benchmarking hatch).
This repo measures how much faster (or slower) modern solvers are on one representative heavy proof.

## What it benchmarks

A single, deliberately heavy **string-free, SAT-dominated** proof
([`SatBenchmarkProof.java`](proofs/src/test/java/bench/SatBenchmarkProof.java)): the **Euclidean
division identity** `(a / b) * b + (a % b) == a` over wide 64-bit symbolic operands, chained over
several independent rounds plus a 32-bit multiplier distributivity identity. It is a **valid**
property (verdict `VERIFIED`), so the solver must prove the negation UNSAT across the whole search
space — it can never short-circuit on a counterexample. Division/modulo bit-blast to the densest
CNF jbmc emits, so the **SAT solve dominates** jbmc's fixed ~3-4s startup, which is exactly what
makes solver differences visible. (It must be string-free: a string forces jbmc's string-refinement
loop, which ignores `--external-sat-solver`.)

## Solvers

One **isolated GitHub Actions job per solver** (each builds/installs only its own solver):

| Job | Solver | How |
|---|---|---|
| `control-minisat` | MiniSat 2.2.1 | jbmc built-in — **the control**, no `--external-sat-solver` |
| `kissat` | [Kissat](https://github.com/arminbiere/kissat) | `./configure && make` |
| `cadical` | [CaDiCaL](https://github.com/arminbiere/cadical) | `./configure && make` |
| `cryptominisat` | [CryptoMiniSat](https://github.com/msoos/cryptominisat) | cmake static build |
| `glucose` | [Glucose](https://github.com/audemard/glucose) | `make` |

Each job runs the proof once, times the wall-clock, and uploads `result-<solver>.txt`. A final
`summary` job collects them into a comparison table (written to the run's job summary and committed
to [`RESULTS.md`](RESULTS.md)).

## Results

See **[RESULTS.md](RESULTS.md)** (updated automatically by the latest CI run) or the job summary of
the most recent [benchmark workflow run](../../actions/workflows/benchmark.yml).

## Running locally

bmc4j is consumed unpublished from a git submodule (`./bmc4j`) via `includeBuild`, so clone with
submodules:

```bash
git clone --recurse-submodules https://github.com/bmc4j/sat-benchmark
cd sat-benchmark
```

Requires the Gradle wrapper (9.x, bundled) — it auto-provisions a JDK 25 toolchain via the Foojay
resolver.

Control run (jbmc's built-in MiniSat):

```bash
./gradlew :proofs:test --no-daemon
```

Against an external DIMACS solver (the binary must read DIMACS and print competition output —
`s SATISFIABLE`/`s UNSATISFIABLE` to stdout):

```bash
./gradlew :proofs:test --no-daemon -PsatPath=/path/to/cadical
```

This is the same `-PsatPath` → `bmc.externalSat` → `--external-sat-solver` contract bmc4j's own
`model-conformance-proofs` module uses.

## Layout

```
settings.gradle.kts          includeBuild("bmc4j") + foojay resolver + :proofs
proofs/build.gradle.kts       applies id("org.bmc4j"); -PsatPath pass-through
proofs/src/test/java/bench/   the single SAT-dominated @BmcProof
bmc4j/                        git submodule, pinned (the engine + plugin, from source)
.github/workflows/benchmark.yml   per-solver matrix + summary table
```
