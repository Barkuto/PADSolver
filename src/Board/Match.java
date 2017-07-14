package Board;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Iggie on 6/20/2017.
 */
public class Match {
    private int numOrbs;
    private Orb orbType;
    private ArrayList<Point> orbPositions;
    private boolean isExplodedOrbs;

    /**
     * Creates a Match with the given params.
     *
     * @param numOrbs        Number of orbs in this match.
     * @param orbType        Orb type of the Match.
     * @param orbPositions   Positions on the board of this Match.
     * @param isExplodedOrbs Whether or not these Orbs were exploded.
     */
    public Match(int numOrbs, Orb orbType, ArrayList<Point> orbPositions, boolean isExplodedOrbs) {
        this.numOrbs = numOrbs;
        this.orbType = orbType;
        this.orbPositions = orbPositions;
        this.isExplodedOrbs = isExplodedOrbs;
    }

    /**
     * Default type of Match, that is not exploded Orbs.
     *
     * @param numOrbs      Number of orbs in this match.
     * @param orbType      Orb type of the Match.
     * @param orbPositions Positions on the board of this Match.
     */
    public Match(int numOrbs, Orb orbType, ArrayList<Point> orbPositions) {
        this(numOrbs, orbType, orbPositions, false);
    }

    /**
     * @return Returns if this Match is exploded Orbs
     */
    public boolean isExplodedOrbs() {
        return isExplodedOrbs;
    }

    /**
     * Returns if this match is a row or not based on how many columns are given.
     * A match can only be a row if it spans left to right in a single line somewhere in the match.
     *
     * @param boardColumns The number of columns the Board has to check for this match.
     * @return Returns true if this Match is a row( numOrbs >= Board columns, and at least Board columns orb y positions are equal ).
     */
    public boolean isRow(int boardColumns) {
        boolean isRow = false;
        if (orbPositions.size() >= boardColumns) {
            int maxY = 0;
            for (int i = 0; i < orbPositions.size(); i++) {
                int y = orbPositions.get(i).y;
                if (y > maxY)
                    maxY = y;
            }

            int[] counts = new int[maxY + 1];
            for (int i = 0; i < orbPositions.size(); i++) {
                int y = orbPositions.get(i).y;
                counts[y]++;
                if (counts[y] >= boardColumns)
                    return true;
            }
        }
        return isRow;
    }

    /**
     * @return Returns true if this Match is a cross, 5 orbs in a cross formation.
     */
    public boolean isCross() {
        if (orbPositions.size() == 5) {
            Point left = orbPositions.get(0);
            Point right = orbPositions.get(1);
            Point down = orbPositions.get(2);
            Point middle = orbPositions.get(3);
            Point up = orbPositions.get(4);
            return middle.x == left.x + 1 && middle.y == left.y &&
                    middle.x == right.x - 1 && middle.y == right.y &&
                    middle.y == up.y - 1 && middle.x == up.x &&
                    middle.y == down.y + 1 && middle.x == down.x;
        }
        return false;
    }

    /**
     * @return Returns the number of orbs in this Match.
     */
    public int getNumOrbs() {
        return numOrbs;
    }

    /**
     * @return Returns what type of Orbs this Match has.
     */
    public Orb getOrbType() {
        return orbType;
    }

    /**
     * @return Returns the positions of the Orbs in this Match.
     */
    public ArrayList<Point> getOrbPositions() {
        return orbPositions;
    }

    /**
     * @return String of this Match: "OrbType:NumOrbs".
     */
    public String toString() {
        return orbType + ":" + numOrbs;
    }

    /**
     * Checks if this and another Object are equal.
     * Two Matches are equal IFF their:
     * -OrbTypes are equal
     * -NumOrbs are equal
     * -isRow() are the same
     * -isCross() are the same
     * -isExplodedOrbs() are the same
     *
     * @param o Other object to compare to.
     * @return true if matches are equal, false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Match) {
            Match otherMatch = (Match) o;

            return getOrbType() == otherMatch.getOrbType()
                    && getNumOrbs() == otherMatch.getNumOrbs()
                    && isRow(6) == otherMatch.isRow(6)
                    && isCross() == otherMatch.isCross()
                    && isExplodedOrbs() == otherMatch.isExplodedOrbs();
        }
        return false;
    }
}