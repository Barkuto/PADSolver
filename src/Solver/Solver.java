package Solver;

import Board.Board;
import Board.Direction;
import Board.Match;
import Board.Orb;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Iggie on 6/22/2017.
 */
public class Solver {
    private boolean solving = false;

    /**
     * Stops any current solving that might be occurring.
     */
    public void stopSolving() {
        solving = false;
    }

    /**
     * Basic solver with the given Board and Heuristic.
     *
     * @param initialBoard Board to start with.
     * @param heuristic    Heuristic to score with.
     * @return List of SolutionEntry, which each contain a Board and a score.
     */
    public ArrayList<SolutionEntry> solve(Board initialBoard, Heuristic heuristic) {
        return solve(initialBoard, heuristic, 20, 100);
    }

    /**
     * Solver with each of the given params.
     *
     * @param initialBoard Board to start with.
     * @param heuristic    Heuristic to score with.
     * @param maxMoves     Max moves to do.
     * @param pathScale    Path scalar. Decides how many total solutions to keep.
     * @return List of SolutionEntry, which each contain a Board and a score.
     */
    public ArrayList<SolutionEntry> solve(Board initialBoard, Heuristic heuristic, int maxMoves, int pathScale) {
        if (solving)
            return null;
        solving = true;
        if ((heuristic.getComboType() == ComboType.ROW || initialBoard.orbCount().get(Orb.BOMB) >= 3) && maxMoves < 50) {
            maxMoves = 50;
        }

        List<SolutionEntry> syncSolutions = Collections.synchronizedList(new ArrayList<>());

        int rows = initialBoard.getRows();
        int cols = initialBoard.getColumns();
        int solutionsToKeep = rows * cols * pathScale;

        ArrayList<Thread> threads = new ArrayList<>();

        for (int y = 0; y < rows; y++) {
            ArrayList<SolutionEntry> startingBoards = new ArrayList<>();
            for (int x = 0; x < cols; x++) {
                if (!solving)
                    return null;
                Board boardToAdd = new Board(initialBoard);
                boardToAdd.setStart(x, y);
                startingBoards.add(new SolutionEntry(boardToAdd, computeScore(boardToAdd, heuristic)));
            }
            int moves = maxMoves;
            Thread t = new Thread(() -> {
                ArrayList<SolutionEntry> solutionsFound = solve_sub(startingBoards, heuristic, moves, solutionsToKeep / rows);
                if (solutionsFound != null)
                    synchronized (syncSolutions) {
                        syncSolutions.addAll(solutionsFound);
                    }
            });
            if (!solving)
                return null;
            threads.add(t);
            t.start();
        }
        for (int i = 0; i < threads.size(); i++)
            try {
                threads.get(i).join();
            } catch (InterruptedException ignored) {
            }

        if (!solving)
            return null;
        ArrayList<SolutionEntry> solutions = simplifySolutions(new ArrayList<>(syncSolutions));
        solutions.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
        solving = false;
        return solutions;
    }

    /**
     * Solves the given Board with the given start position and default maxMoves/scale.
     *
     * @param initialBoard Board to start with.
     * @param heuristic    Heuristic to score with.
     * @param startX       Column to start at.
     * @param startY       Row to start at.
     * @return List of SolutionEntry, which each contain a Board and a score.
     */
    public ArrayList<SolutionEntry> solveWithStart(Board initialBoard, Heuristic heuristic, int startX, int startY) {
        return solveWithStart(initialBoard, heuristic, 20, 100, startX, startY);
    }

    /**
     * Solves the given Board with the given start position.
     *
     * @param initialBoard Board to start with.
     * @param heuristic    Heuristic to score with.
     * @param maxMoves     Max moves to do.
     * @param pathScale    Path scalar. Decides how many total solutions to keep.
     * @param startX       Column to start at.
     * @param startY       Row to start at.
     * @return List of SolutionEntry, which each contain a Board and a score.
     */
    public ArrayList<SolutionEntry> solveWithStart(Board initialBoard, Heuristic heuristic, int maxMoves, int pathScale, int startX, int startY) {
        if (solving)
            return null;
        solving = true;
        if ((heuristic.getComboType() == ComboType.ROW || initialBoard.orbCount().get(Orb.BOMB) >= 3) && maxMoves < 50) {
            maxMoves = 50;
        }

        List<SolutionEntry> syncSolutions = Collections.synchronizedList(new ArrayList<>());

        int rows = initialBoard.getRows();
        int cols = initialBoard.getColumns();
        int solutionsToKeep = rows * cols * pathScale;

        ArrayList<Thread> threads = new ArrayList<>();

        Board board = new Board(initialBoard);
        board.setStart(startX, startY);
        for (int i = 0; i < 4; i++) {
            if (!solving)
                return null;
            Board b = new Board(board);
            if (b.move(Direction.values()[i])) {
                if (!solving)
                    return null;
                ArrayList<SolutionEntry> startingBoard = new ArrayList<>();
                startingBoard.add(new SolutionEntry(b, computeScore(b, heuristic)));

                int moves = maxMoves;
                Thread t = new Thread(() -> {
                    ArrayList<SolutionEntry> solutionsFound = solve_sub(startingBoard, heuristic, moves, solutionsToKeep / 4);
                    if (solutionsFound != null)
                        synchronized (syncSolutions) {
                            syncSolutions.addAll(solutionsFound);
                        }
                });
                if (!solving)
                    return null;
                threads.add(t);
                t.start();
            }
        }

        for (int i = 0; i < threads.size(); i++)
            try {
                threads.get(i).join();
            } catch (InterruptedException ignored) {
            }
        if (!solving)
            return null;
        ArrayList<SolutionEntry> solutions = simplifySolutions(new ArrayList<>(syncSolutions));
        solutions.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
        solving = false;
        return solutions;
    }

    /**
     * Helper function that is to be run by a single Thread, with the given params.
     *
     * @param startingBoards  Boards to start generating solutions from.
     * @param heuristic       Heuristic to score with.
     * @param maxMoves        Max moves to make.
     * @param solutionsToKeep Num solutions to keep = rows * columns * scale.
     * @return List of solutions from the given start board.
     */
    private ArrayList<SolutionEntry> solve_sub(ArrayList<SolutionEntry> startingBoards, Heuristic heuristic, int maxMoves, int solutionsToKeep) {
        ArrayList<SolutionEntry> solutions = startingBoards;

        while (true) {
            ArrayList<SolutionEntry> solutionsToAdd = new ArrayList<>();
            int maxMovesMade = 0;
            for (int i = 0; i < solutions.size(); i++) {
                Board b = solutions.get(i).getBoard();
                if (b.getMoves().size() > maxMovesMade)
                    maxMovesMade = b.getMoves().size();
                if (b.getMoves().size() < maxMoves) {
                    for (int j = 0; j < 4; j++) {
                        if (!solving)
                            return null;
                        Board board = new Board(b);
                        ArrayList<Direction> moves = board.getMoves();
                        Direction lastDir = null;
                        if (moves.size() > 0)
                            lastDir = moves.get(moves.size() - 1);
                        if (lastDir == null || !lastDir.isOppositeTo(Direction.values()[j])) {
                            if (board.move(Direction.values()[j])) {
                                SolutionEntry e = new SolutionEntry(board, computeScore(board, heuristic));
                                if (!solutions.contains(e))
                                    solutionsToAdd.add(e);
                            }
                        }
                    }
                }
                if (!solving)
                    return null;
            }
            solutions.addAll(solutionsToAdd);

            solutions.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));

            if (solutions.size() > solutionsToKeep)
                solutions = new ArrayList<>(solutions.subList(0, solutionsToKeep));

            boolean greaterMovesFound = false;
            for (SolutionEntry e : solutions) {
                if (!solving)
                    return null;
                Board b = e.getBoard();
                if (b.getMoves().size() > maxMovesMade) {
                    greaterMovesFound = true;
                    break;
                }
            }
            if (!solving)
                return null;
            if (!greaterMovesFound)
                break;
        }
        return solutions;
    }

    /**
     * Simplifies the given solutions.
     * Keeps every unique solution, which is a solution with it's own type of Matches.
     * The unique solution with the lowest moves is kept.
     *
     * @param solutions Solutions to simplify.
     * @return List of the simplified solutions.
     */
    public ArrayList<SolutionEntry> simplifySolutions(ArrayList<SolutionEntry> solutions) {
        ArrayList<SolutionEntry> simplified = new ArrayList<>();
        ArrayList<SolutionEntry> toCheck = new ArrayList<>(solutions);

        int index = 0;
        while (toCheck.size() != 0) {
            simplified.add(new SolutionEntry(toCheck.remove(0)));

            SolutionEntry currEntry = simplified.get(index);
            Board current = currEntry.getBoard();
            ArrayList<Match> currMatches = current.getTotalMatches();
            int currMoves = current.getMoves().size();

            for (int j = 0; j < toCheck.size(); j++) {
                SolutionEntry entry = toCheck.get(j);
                Board board = entry.getBoard();
                ArrayList<Match> matches = board.getTotalMatches();
                int moves = board.getMoves().size();

                if (equalsMatchArrays(currMatches, matches)) {
                    if (moves < currMoves) {
                        simplified.set(index, entry);
                    }
                    toCheck.remove(j);
                    j--;
                }
            }
            index++;
        }
        return simplified;
    }

    /**
     * Checks if the two matches are equal, such that both have the same exact matches.
     *
     * @param matches1 First matches to compare.
     * @param matches2 Second matches to compare.
     * @return true if the same, false otherwise.
     */
    private boolean equalsMatchArrays(ArrayList<Match> matches1, ArrayList<Match> matches2) {
        if (matches1.size() != matches2.size())
            return false;
        ArrayList<Match> toCompare = new ArrayList<>(matches2);
        for (int i = 0; i < matches1.size(); i++) {
            boolean found = false;
            for (int j = 0; j < toCompare.size(); j++) {
                Match m1 = matches1.get(i);
                Match m2 = toCompare.get(j);
                if (m1.getOrbType() == m2.getOrbType()
                        && m1.getNumOrbs() == m2.getNumOrbs()) {
                    found = true;
                    toCompare.remove(j);
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compute score of this Board given the Heuristic.
     *
     * @param board     Board to score.
     * @param heuristic Heuristic to score off of.
     * @return A score for this Board.
     */
    public double computeScore(Board board, Heuristic heuristic) {
        ComboType comboType = heuristic.getComboType();
        Orb primaryOrb = heuristic.getPrimaryOrb();
        HashMap<Orb, Integer> orbCount = board.orbCount();
        ArrayList<Match> matches = board.getTotalMatches();
        int matchesMade = matches.size();
        double score = 0;

        double comboWeight = 0.5;
        double typeWeight = 0.5;
        double bombWeight = 5;

        double matchesMadeScore = matchesMade;
        double typeScore = 0;
        double bombScore = 0;

        int bombsMatched = board.getInPlaceMatchesFor(Orb.BOMB).size();
        int numBombs = orbCount.get(Orb.BOMB);
        if (numBombs >= 3 && bombsMatched < numBombs)
            bombScore -= numBombs;

        switch (comboType) {
            case COMBO:
                comboWeight = 4.0;
                matchesMadeScore -= board.maxPossibleCombos() - matchesMade;
                break;
            case TPA:
                break;
            case ROW:
                comboWeight = 0.25;
                typeWeight = 4.0;
                break;
            case SPARKLE:
                typeWeight = 3.0;
                break;
            case CROSS:
                typeWeight = 2.0;
                break;
        }

        int rows = 0;
        for (Match m : matches) {
            if (!m.isExplodedOrbs()) {
                matchesMadeScore += m.getNumOrbs();
                if (m.getNumOrbs() >= heuristic.getMinConnectedOrbs())
                    matchesMadeScore += 2;
                switch (comboType) {
                    case COMBO:
                        if (m.getNumOrbs() > 3)
                            matchesMadeScore -= 2 * (m.getNumOrbs() - 3);
                        break;
                    case TPA:
                        if (m.getNumOrbs() == 4) {
                            if (m.getOrbType() == primaryOrb)
                                typeScore += 2;
                            else
                                typeScore += 1;
                        }
                        break;
                    case ROW:
                        if (m.getOrbType() == primaryOrb) {
                            if (m.isRow()) {
                                typeScore += m.getNumOrbs();
                                rows++;
                                if (m.getNumOrbs() < heuristic.getMinRowOrbs())
                                    typeScore -= 2;
                            } else if (m.getNumOrbs() > orbCount.get(primaryOrb) % 6) {
                                typeScore -= 4.0;
                            }
                        }
                        break;
                    case SPARKLE:
                        if (m.getNumOrbs() == 5)
                            if (m.getOrbType() == primaryOrb)
                                typeScore += 2;
                        break;
                    case CROSS:
                        if (m.isCross() && m.getOrbType() == primaryOrb)
                            typeScore += 2;
                        break;
                }
            }
        }

        if (comboType == ComboType.ROW)
            if (rows < orbCount.get(primaryOrb) / 6)
                typeScore -= (orbCount.get(primaryOrb) / 6);

        score += matchesMadeScore * comboWeight;
        score += typeScore * typeWeight;
        score += bombScore * bombWeight;
        return score;
    }

    /**
     * Class to contain a Board and it's score.
     * Prevents multiple calls of computeScore() to increase solve time.
     */
    public class SolutionEntry {
        private final Board board;
        private final Double score;

        SolutionEntry(Board board, double score) {
            this.board = new Board(board);
            this.score = score;
        }

        SolutionEntry(SolutionEntry otherEntry) {
            this(new Board(otherEntry.getBoard()), otherEntry.getScore());
        }

        public Board getBoard() {
            return board;
        }

        public double getScore() {
            return score;
        }

        public boolean equals(Object o) {
            if (o instanceof SolutionEntry) {
                SolutionEntry other = (SolutionEntry) o;
                return board.equals(other.board);
            }
            return false;
        }

        public String toString() {
            return board.getTotalMatches() + ": " + score;
        }
    }
}
