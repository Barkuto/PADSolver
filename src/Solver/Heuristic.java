package Solver;

import Board.Orb;

/**
 * Created by Iggie on 6/22/2017.
 */
public class Heuristic {
    private Orb primaryOrb;
    private ComboType comboType;
    private int minConnectedOrbs, minRowOrbs;

    public static Heuristic DEFAULT = new Heuristic(Orb.BLANK, ComboType.COMBO);

    /**
     * Creates a Heuristic based on the given params.
     *
     * @param primaryOrb       Primary Orb.
     * @param comboType        Combo type.
     * @param minConnectedOrbs minimum connected Orbs.
     * @param minRowOrbs       minimum row orbs needed.
     */
    public Heuristic(Orb primaryOrb, ComboType comboType, int minConnectedOrbs, int minRowOrbs) {
        setPrimaryOrb(primaryOrb);
        setComboType(comboType);
        setMinConnectedOrbs(minConnectedOrbs);
        setMinRowOrbs(minRowOrbs);
    }

    /**
     * Creates a Heuristic based on the given params.
     * minRowOrbs set to 6.
     *
     * @param primaryOrb       Primary Orb.
     * @param comboType        Combo type.
     * @param minConnectedOrbs minimum connected Orbs.
     */
    public Heuristic(Orb primaryOrb, ComboType comboType, int minConnectedOrbs) {
        this(primaryOrb, comboType, minConnectedOrbs, 6);
    }

    /**
     * Creates a Heuristic based on the given params.
     * minRowOrbs set to 6, and minConnectedOrbs set to 3.
     *
     * @param primaryOrb Primary Orb.
     * @param comboType  Combo type.
     */
    public Heuristic(Orb primaryOrb, ComboType comboType) {
        this(primaryOrb, comboType, 3, 6);
    }

    /**
     * @return Returns the primary Orb of this Heuristic.
     */
    public Orb getPrimaryOrb() {
        return primaryOrb;
    }

    /**
     * @return Returns the ComboType of this Heuristic.
     */
    public ComboType getComboType() {
        return comboType;
    }

    /**
     * @return Returns the minimum required connected Orbs.
     */
    public int getMinConnectedOrbs() {
        return minConnectedOrbs;
    }

    /**
     * @return Returns the minimum required row Orbs.
     */
    public int getMinRowOrbs() {
        return minRowOrbs;
    }

    /**
     * Sets the primary Orb for this Heuristic.
     *
     * @param primaryOrb New primary Orb to set to.
     */
    public void setPrimaryOrb(Orb primaryOrb) {
        this.primaryOrb = primaryOrb;
    }

    /**
     * Sets the ComboType for this Heuristic
     *
     * @param comboType New ComboType to set to.
     */
    public void setComboType(ComboType comboType) {
        this.comboType = comboType;
    }

    /**
     * Sets min connected Orbs for this Heuristic
     *
     * @param minConnectedOrbs New min connected Orbs to set to.
     */
    public void setMinConnectedOrbs(int minConnectedOrbs) {
        if (minConnectedOrbs >= 3)
            this.minConnectedOrbs = minConnectedOrbs;
        else this.minConnectedOrbs = 3;
    }

    /**
     * Sets min orbs needed for a row.
     *
     * @param minRowOrbs New min row orbs set to.
     */
    public void setMinRowOrbs(int minRowOrbs) {
        if (minRowOrbs >= 6)
            this.minRowOrbs = minRowOrbs;
        else this.minRowOrbs = 6;
    }
}
