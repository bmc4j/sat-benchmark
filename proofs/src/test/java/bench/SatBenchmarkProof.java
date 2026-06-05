package bench;

import org.bmc4j.Bmc;
import org.bmc4j.BmcProof;

/**
 * ONE string-free, SAT-DOMINATED proof, used to benchmark external DIMACS SAT solvers behind
 * bmc4j's {@code --external-sat-solver} hook (run via {@code -PsatPath=<solver>}; no flag = jbmc's
 * built-in MiniSat = the control).
 *
 * <h2>The property</h2>
 * The Euclidean division identity, the canonical "largest-CNF" workload for a bit-vector engine:
 * for all 64-bit {@code a} and any non-zero divisor {@code b},
 * <pre>(a / b) * b + (a % b) == a</pre>
 * It is a VALID property (verdict VERIFIED) — jbmc must explore the WHOLE search space to prove
 * it, so the solver can never short-circuit on a counterexample. Division and modulo bit-blast to
 * the densest circuits jbmc emits, and we chain several independent wide divisions plus a wide
 * multiplier identity so the CNF is large enough that the SAT SOLVE — not jbmc's ~3-4s
 * startup/bit-blasting — dominates the wall-clock, which is what makes solver differences visible.
 *
 * <h2>Why it is string-free</h2>
 * Only {@code long}/{@code int} symbolic values and arithmetic. External SAT only engages for
 * string-free numeric/boolean proofs (a string would force jbmc's string-refinement loop, which
 * ignores {@code --external-sat-solver}), so there is deliberately not a single {@code String} here.
 *
 * <h2>Tuning</h2>
 * Difficulty is driven by (1) operand WIDTH — full 64-bit {@code anyLong} operands, the widest
 * dividers jbmc can build; (2) the NUMBER of chained independent division/modulo identities
 * ({@link #ROUNDS}); and (3) a 32-bit multiplier identity for extra dense AND-gate structure.
 * Raising {@link #ROUNDS} (or widening operands) raises the CNF size and the solve time — the
 * lever to pull if a future jbmc/solver makes this too fast. It is intentionally NOT made slow by
 * loop unwinding or strings. Target: tens of seconds under built-in MiniSat on a CI runner
 * (clearly SAT-bound, well under the 30-min job cap). If it ever drops sub-second, bump ROUNDS.
 */
class SatBenchmarkProof {

    /**
     * Number of independent wide division/modulo identities chained into one formula. Each adds a
     * full 64-bit divider+remainder circuit. Tuned so the SAT solve dominates jbmc startup; raise
     * to make the proof harder, lower to make it faster.
     */
    private static final int ROUNDS = 6;

    @BmcProof
    void euclidean_division_identity_holds_for_wide_symbolic_operands() {
        boolean ok = true;

        // Chain several INDEPENDENT wide division/modulo identities. Fresh symbolic operands each
        // round so the solver can't reuse structure — the CNF grows linearly in ROUNDS and stays
        // a single monolithic UNSAT-of-the-negation solve.
        for (int r = 0; r < ROUNDS; r++) {
            long a = Bmc.anyLong();
            long b = Bmc.anyLong();
            // Exclude the two cases that are *defined-but-degenerate* on the JVM, so the identity is
            // the pure math fact and the solver still sees the full 64-bit divider:
            //   b == 0           -> ArithmeticException (not an arithmetic identity)
            //   a==MIN_VALUE,b==-1 -> overflow: (a/b) overflows, identity genuinely doesn't hold.
            Bmc.assume(b != 0L);
            Bmc.assume(!(a == Long.MIN_VALUE && b == -1L));

            long q = a / b;
            long rem = a % b;
            // The identity. True for every remaining (a, b); the solver must prove the negation UNSAT.
            ok = ok && (q * b + rem == a);
        }

        // A wide 32-bit multiplier identity: distributivity over addition. Dense AND-gate network,
        // independent of the dividers above — extra SAT structure, still string-free and VALID.
        int x = Bmc.anyInt();
        int y = Bmc.anyInt();
        int z = Bmc.anyInt();
        // Two's-complement multiplication distributes over addition for all int operands (the
        // overflow wraps identically on both sides), so this is a true property.
        ok = ok && (x * (y + z) == x * y + x * z);

        Bmc.check(ok);
    }
}
