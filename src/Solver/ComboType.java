package Solver;

/**
 * Created by Iggie on 6/22/2017.
 */
public enum ComboType {
    /**
     * Combo types for Heuristic to use.
     * Combo: Prioritize max combos on board.
     * <p>
     * Row: Prioritize rows of some color on board.
     * <p>
     * TPA: Prioritize TPAs of some color on board.
     * <p>
     * Cross: Prioritize Crosses of some color on board.
     */
    COMBO, ROW, TPA, SPARKLE, CROSS;
}
