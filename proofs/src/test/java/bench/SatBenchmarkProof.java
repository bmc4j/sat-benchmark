package bench;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.bmc4j.Bmc;
import org.bmc4j.BmcProof;

/**
 * ONE string-free, SAT-dominated proof used to benchmark external DIMACS SAT solvers behind
 * bmc4j's {@code --external-sat-solver} hook (run via {@code -PsatPath=<solver>}; no flag = jbmc's
 * built-in MiniSat = the control).
 *
 * <h2>The property — a real, known-slow bmc4j proof</h2>
 * This is bmc4j's own {@code BigDecimalLaws.setScale_widen_then_narrow_round_trips} law: widening
 * then narrowing a symbolic {@code BigDecimal} round-trips to the original. Narrowing {@code setScale}
 * goes through the model's {@code roundDiv} kernel, which bit-blasts to a dense rounding-<b>divider</b>
 * circuit — the densest CNF the BigDecimal model produces. It is VALID (verdict VERIFIED): the
 * identity holds for every value, so the solver must prove the negation UNSAT and can never
 * short-circuit on a counterexample. This is the exact formula prior external-SAT measurement used
 * (CryptoMiniSat ~25% faster than built-in MiniSat on it), so it is a genuine, representative,
 * SAT-dominated workload rather than a synthetic one.
 *
 * <h2>String-free</h2>
 * Only the long-backed {@code BigDecimal} numeric surface ({@code valueOf(long,int)}, {@code setScale},
 * {@code compareTo}) and a {@code RoundingMode} enum — not a single {@code String}. External SAT only
 * engages for string-free proofs (a string forces jbmc's string-refinement loop, which ignores
 * {@code --external-sat-solver}).
 *
 * <h2>Tuning</h2>
 * {@link #BOUND} is the difficulty knob: it sets the unscaled value range and thus the bit-width the
 * rounding divider must reason about. In bmc4j's own suite this law runs at {@code bound = 1_000}
 * (~10s, kept fast so CI doesn't flake); here it is widened to put the solve in the slow,
 * clearly-SAT-bound zone (~3 min under MiniSat) where modern solvers pull apart. Lower it if a CI
 * runner can't finish under the benchmark's {@code -Dbmc.timeoutSeconds} ceiling.
 */
class SatBenchmarkProof {

    /** Wide unscaled-value bound -> wide rounding-divider -> the slow, SAT-dominated regime. */
    private static final int BOUND = 1_000_000;

    /** A symbolic BigDecimal at the given scale, unscaled value in [-BOUND, BOUND]. */
    private static BigDecimal anyBd(int scale) {
        return BigDecimal.valueOf((long) Bmc.anyInt(-BOUND, BOUND), scale);
    }

    @BmcProof
    void setScale_widen_then_narrow_round_trips() {
        BigDecimal a = anyBd(2);
        BigDecimal widened = a.setScale(4, RoundingMode.HALF_UP);
        Bmc.check(widened.setScale(2, RoundingMode.HALF_UP).compareTo(a) == 0);
    }
}
