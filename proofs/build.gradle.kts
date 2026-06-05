// The benchmark module: one string-free, SAT-dominated @BmcProof, run under jbmc.
//
// Applying the bmc4j plugin is the whole setup a real consumer needs — it adds bmc-runtime,
// JUnit 5, and the bundled jbmc engine for the host platform, and wires the `test` task.
plugins {
    java
    id("org.bmc4j")
}

// Pin a Java 25 toolchain (matches bmc4j's own example modules). Foojay (settings.gradle.kts)
// provisions it on a stock CI runner. The bmc4j lib/plugin themselves target Java 17, but the
// proof sources here run on the toolchain JDK like any consumer.
java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

// Opt-in external-SAT escape hatch — identical contract to bmc4j's model-conformance-proofs:
//   ./gradlew :proofs:test -PsatPath=/path/to/solver-binary
// routes the proof's CNF at an external DIMACS solver via jbmc's --external-sat-solver
// (bmc.externalSat system property -> Jbmc.addSolver). With no -PsatPath it is a no-op and
// jbmc uses its built-in MiniSat 2.2.1 — the benchmark CONTROL. External SAT bypasses string
// refinement, so the proof must be string-free (it is).
tasks.withType<Test>().configureEach {
    doFirst {
        providers.gradleProperty("satPath").orNull?.let { systemProperty("bmc.externalSat", it) }
    }
    // The benchmark is one proof: don't let JUnit parallelism (default = #CPUs) muddy the
    // wall-clock — one jbmc invocation, one solve, measured cleanly.
    maxParallelForks = 1
}

// The single proof is deliberately heavy. Don't let bmc4j's default per-proof timeout cut it
// off on a slow solver, and don't cache the verdict (we re-measure every run / every solver).
bmc {
    timeoutSeconds.set(1800)
    cache.set(false)
}
