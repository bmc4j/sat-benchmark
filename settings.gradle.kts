// sat-benchmark — benchmarks external SAT solvers behind bmc4j's --external-sat-solver hook.
//
// bmc4j is consumed UNPUBLISHED from the git submodule via includeBuild: that single
// inclusion supplies BOTH the `org.bmc4j` Gradle plugin (for `plugins { id("org.bmc4j") }`)
// AND the runtime/engine dependencies (by coordinate substitution) — no mavenLocal, no publish.
// Mirrors bmc4j's own repo-root settings.gradle.kts.
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Auto-provision JDK toolchains. The :proofs module pins a Java 25 toolchain
// (JavaLanguageVersion.of(25)); the Foojay resolver lets Gradle download a matching
// JDK on a runner that doesn't have one pre-installed. 1.0.0 is the first Foojay
// resolver release compatible with Gradle 9 (matches bmc4j's pin).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// bmc4j is a git submodule at ./bmc4j (its repo root is itself an aggregator build that
// includeBuild("core") for the product modules). One inclusion provides the plugin + runtime.
includeBuild("bmc4j")

rootProject.name = "sat-benchmark"

include("proofs")
