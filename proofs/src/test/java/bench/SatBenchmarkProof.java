package bench;

import org.bmc4j.Bmc;
import org.bmc4j.BmcProof;

/**
 * ONE string-free, SAT-dominated proof used to benchmark external DIMACS SAT solvers behind
 * bmc4j's {@code --external-sat-solver} hook (run via {@code -PsatPath=<solver>}; no flag = jbmc's
 * built-in MiniSat = the control).
 *
 * <h2>The property</h2>
 * Two's-complement multiplication distributes over addition: {@code x*(y+z) == x*y + x*z} for all
 * operands (overflow wraps identically on both sides), chained over {@link #ROUNDS} independent
 * rounds. It is VALID (verdict VERIFIED), so the solver must prove the negation UNSAT — it can
 * never short-circuit on a counterexample, and the work is real SAT solving rather than jbmc's
 * ~3-4s startup.
 *
 * <h2>Why multiplication, and why bounded width</h2>
 * Division and full-width (64- or 32-bit) multiplier <em>equivalence</em> are pathologically hard
 * for CDCL SAT — at full width every solver simply times out, so the benchmark can't discriminate
 * (measured: 64-bit Euclidean division ran &gt;40&nbsp;min even with operands bounded to ±10^6 — the
 * divider <em>circuit</em>, not the value range, is the cost). The smoothly-tunable knob is the
 * operand <b>bit-width</b>: a multiplier's CNF grows ~quadratically in width, so masking operands
 * to a fixed width ({@link #MASK}) puts the solve in the "medium" zone — seconds to tens of
 * seconds, not milliseconds and not a timeout — where modern solvers pull apart from MiniSat.
 *
 * <h2>String-free</h2>
 * Only {@code int} symbolic values and arithmetic — not a single {@code String}. External SAT only
 * engages for string-free proofs (a string forces jbmc's string-refinement loop, which ignores
 * {@code --external-sat-solver}).
 *
 * <h2>Tuning</h2>
 * {@link #MASK}'s width is the primary (quadratic) difficulty knob — add a few bits to make the
 * proof markedly harder, drop bits to make it faster. {@link #ROUNDS} is the secondary (linear)
 * knob. Target: tens of seconds under built-in MiniSat on a CI runner, comfortably under the
 * benchmark's {@code -Dbmc.timeoutSeconds} ceiling.
 */
class SatBenchmarkProof {

    /** Operand mask — 18-bit operands (0..262143). Width is the quadratic difficulty knob. */
    private static final int MASK = (1 << 18) - 1;

    /** Independent rounds chained into one formula — the linear difficulty knob. */
    private static final int ROUNDS = 6;

    @BmcProof
    void multiplication_distributes_over_addition_for_wide_symbolic_operands() {
        boolean ok = true;

        // Each round multiplies fresh symbolic operands, masked to a fixed bit-width so the solve
        // stays in the medium zone (full 32-bit multiplier equivalence is SAT-pathological). The
        // distributive identity holds for all operands, so the whole formula is one monolithic
        // UNSAT-of-the-negation solve.
        for (int r = 0; r < ROUNDS; r++) {
            int x = Bmc.anyInt() & MASK;
            int y = Bmc.anyInt() & MASK;
            int z = Bmc.anyInt() & MASK;
            ok = ok && (x * (y + z) == x * y + x * z);
        }

        Bmc.check(ok);
    }
}
