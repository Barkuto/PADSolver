package Board;

import com.sun.org.apache.xpath.internal.operations.Or;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Iggie on 6/20/2017.
 */
public class Board {
    private Orb[][] board;
    private int columns, rows;

    private int startX, startY, currX, currY;
    private ArrayList<Direction> moves;

    /**
     * Creates a board with the given rows and columns.
     * Sets defaults, and fills board with blank orbs.
     *
     * @param columns Columns for the board. If less than 3, columns will be set to 3.
     * @param rows    Rows for the board. If less than 3, rows will be set to 3.
     */
    public Board(int columns, int rows) {
        if (columns < 3) columns = 3;
        if (rows < 3) rows = 3;
        this.columns = columns;
        this.rows = rows;
        this.board = new Orb[rows][columns];
        this.startX = 0;
        this.startY = 0;
        this.currX = 0;
        this.currY = 0;
        this.moves = new ArrayList<Direction>();
        fillWithBlank();
    }

    /**
     * Makes a new board copying from another Board.
     *
     * @param otherBoard The other board to copy from
     */
    public Board(Board otherBoard) {
        this(otherBoard.columns, otherBoard.rows);
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                this.board[y][x] = otherBoard.board[y][x];
            }
        }
        setStart(otherBoard.startX, otherBoard.startY);
        currX = otherBoard.currX;
        currY = otherBoard.currY;
        moves = new ArrayList<>(otherBoard.moves);

    }

    /**
     * Fills Board with Blank Orbs.
     */
    public void fillWithBlank() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = Orb.BLANK;
            }
        }
    }

    /**
     * Randomizes the board until there are no matches.
     * Only uses RGBLDH orbs.
     */
    public void randomize() {
        do {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    board[i][j] = Orb.randomElementOrb();
                }
            }
        } while (getTotalMatches().size() != 0);
        currX = getStart().x;
        currY = getStart().y;
        moves.clear();
    }

    /**
     * Randomizes the board until there are no matches.
     * Uses all Orbs, except Blank orbs.
     */
    public void randomizeWithAll() {
        do {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    board[i][j] = Orb.randomOrb();
                }
            }
        } while (getInPlaceMatchesFor(Orb.FIRE).size() != 0 ||
                getInPlaceMatchesFor(Orb.WATER).size() != 0 ||
                getInPlaceMatchesFor(Orb.WOOD).size() != 0 ||
                getInPlaceMatchesFor(Orb.LIGHT).size() != 0 ||
                getInPlaceMatchesFor(Orb.DARK).size() != 0 ||
                getInPlaceMatchesFor(Orb.HEAL).size() != 0 ||
                getInPlaceMatchesFor(Orb.POISON).size() != 0 ||
                getInPlaceMatchesFor(Orb.MORTALPOISON).size() != 0 ||
                getInPlaceMatchesFor(Orb.JAMMER).size() != 0 ||
                getInPlaceMatchesFor(Orb.BOMB).size() != 0);
        currX = getStart().x;
        currY = getStart().y;
        moves.clear();
    }

    /**
     * Sets the board to the given layout through a string.
     *
     * @param layout String to set the board off of. Length must be same as the Boards rows * cols.
     */
    public void withLayout(String layout) {
        if (layout.length() == (columns * rows)) {
            int xpos = 0;
            int ypos = 0;
            for (int i = 0; i < layout.length(); i++) {
                if (xpos == columns) {
                    xpos = 0;
                    ypos++;
                }
                board[ypos][xpos] = Orb.orbFromLetter(layout.charAt(i));
                xpos++;
            }
            setStart(0, 0);
        }
    }

    /**
     * Checks if the board can currently move in the direction given.
     *
     * @param direction Direction to check.
     * @return true if can move, false otherwise.
     */
    public boolean canMove(Direction direction) {
        return new Board(this).move(direction);
    }

    /**
     * Moves the current position orb in the given direction.
     *
     * @param direction Direction to move.
     * @return Returns if the move was successful.
     */
    public boolean move(Direction direction) {
        return moveOrb(currX, currY, direction);
    }

    public boolean moveOrb(int column, int row, Direction direction) {
        int newCol = column;
        int newRow = row;
        switch (direction) {
            case UP:
                newRow = row - 1;
                break;
            case DOWN:
                newRow = row + 1;
                break;
            case LEFT:
                newCol = column - 1;
                break;
            case RIGHT:
                newCol = column + 1;
                break;
            case UPLEFT:
                newCol = column - 1;
                newRow = row - 1;
                break;
            case UPRIGHT:
                newCol = column + 1;
                newRow = row - 1;
                break;
            case DOWNLEFT:
                newCol = column - 1;
                newRow = row + 1;
                break;
            case DOWNRIGHT:
                newCol = column + 1;
                newRow = row + 1;
                break;
        }
        if (!(newRow < 0 || newRow > rows - 1 || newCol < 0 || newCol > columns - 1)) {
            Orb tmp = board[row][column];
            board[row][column] = board[newRow][newCol];
            board[newRow][newCol] = tmp;

            if (column != currX && row != currY) {
                setStart(column, row);
            } else {
                currX = newCol;
                currY = newRow;
            }
            moves.add(direction);
            return true;
        }
        return false;
    }

    /**
     * Explodes all bombs on the Board, and sets them and according orbs to Blank.
     *
     * @return A Match of all orbs that were exploded.
     */
    public Match katsu() {

        ArrayList<Point> explodedOrbPositions = new ArrayList<>();
        int numBombs = orbCount().get(Orb.BOMB);
        while (numBombs != 0) {
            ArrayList<Match> matchedBombs = getInPlaceMatchesFor(Orb.BOMB);
            ArrayList<Point> matchPoints = new ArrayList<>();

            for (Match m : matchedBombs) {
                matchPoints.addAll(m.getOrbPositions());
            }

            ArrayList<Integer> xToBoom = new ArrayList<>();
            ArrayList<Integer> yToBoom = new ArrayList<>();
            for (int y = 0; y < board.length; y++)
                for (int x = 0; x < board[y].length; x++)
                    if (board[y][x] == Orb.BOMB && !matchPoints.contains(new Point(x, y))) {
                        if (!xToBoom.contains(x))
                            xToBoom.add(x);
                        if (!yToBoom.contains(y))
                            yToBoom.add(y);
                    }
            for (Integer x : xToBoom) {
                for (int y = 0; y < board.length; y++) {
                    board[y][x] = Orb.BLANK;
                    explodedOrbPositions.add(new Point(x, y));
                }
            }
            for (Integer y : yToBoom) {
                for (int x = 0; x < board[y].length; x++) {
                    board[y][x] = Orb.BLANK;
                    explodedOrbPositions.add(new Point(x, y));
                }
            }
            numBombs--;
        }
        if (explodedOrbPositions.size() > 0)
            return new Match(explodedOrbPositions.size(), Orb.BOMB, explodedOrbPositions, true);
        return null;
    }

    /**
     * Matches the Board in place, does not cascade.
     *
     * @return ArrayList of all matches in place.
     */
    public ArrayList<Match> matchInPlace() {
        ArrayList<Match> totalMatches = new ArrayList<>();
        Match katsu = katsu();
        if (katsu != null)
            totalMatches.add(katsu);
        Orb[] orbsToLookFor = Orb.values();
        orbsToLookFor[orbsToLookFor.length - 1] = null;// Do not look for Blanks or Bombs
        for (Orb o : orbsToLookFor) {
            ArrayList<Match> matches = getInPlaceMatchesFor(o);

            // Add all the matches for this Orb type to the total matches
            totalMatches.addAll(matches);

            // "Match" the board, and remove all the matched orbs, replace with BLANK orb
            for (Match m : matches) {
                ArrayList<Point> positions = m.getOrbPositions();
                for (Point p : positions) {
                    board[p.y][p.x] = Orb.BLANK;
                }
            }
        }
        return totalMatches;
    }

    /**
     * Try to cascade the whole board once.
     *
     * @return true if moved an orb, false otherwise.
     */
    public boolean cascadeOnce() {
        boolean moved = false;
        for (int i = rows - 1; i >= 1; i--) {
            for (int j = columns - 1; j >= 0; j--) {
                if (board[i][j] == Orb.BLANK && board[i - 1][j] != Orb.BLANK) {
                    board[i][j] = board[i - 1][j];
                    board[i - 1][j] = Orb.BLANK;
                    moved = true;
                }
            }
        }
        return moved;
    }

    /**
     * Fully cascade this board until there are no Blank orbs under normal orbs
     *
     * @return true if moved an orb, false otherwise.
     */
    public boolean cascade() {
        boolean moved = false;
        for (int times = 0; times < board.length; times++)
            if (cascadeOnce())
                moved = true;
        return moved;
    }

    /**
     * Matches the whole board in place, including cascades.
     *
     * @return ArrayList of matches of all matches done.
     */
    public ArrayList<Match> match() {
        ArrayList<Match> matches = new ArrayList<>();
        ArrayList<Match> add;
        while ((add = matchInPlace()).size() > 0) {
            matches.addAll(add);
            cascade();
        }
        return matches;
    }

    /**
     * Gets the Orb at the given position.
     *
     * @param row    Row to get.
     * @param column Column to get.
     * @return Orb at the given position.
     */
    public Orb orbAt(int row, int column) {
        return board[row][column];
    }

    /**
     * Get a part of this Board, inclusive.
     *
     * @param startRow Row to start at.
     * @param startCol Column to start at.
     * @param endRow   Row to end at.
     * @param endCol   Column to end at.
     * @return A new Board with the Orbs in the given range. Note: if row/col range is < 3, resulting Board will have 3 rows/cols.
     */
    public Board getPartOfBoard(int startRow, int startCol, int endRow, int endCol) {
        Board b = new Board(endCol - startCol, endRow - startRow);
        String layout = "";
        for (int i = startRow; i < endRow; i++) {
            for (int j = startCol; j < endCol; j++) {
                b.board[i - startRow][j - startCol] = board[i][j];
            }
        }
        return b;
    }

    /**
     * @return Number of columns in this board.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * @return Number of rows in this board.
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return Return list of Directions that have been made.
     */
    public ArrayList<Direction> getMoves() {
        return moves;
    }

    /**
     * @return The starting position of this Board.
     */
    public Point getStart() {
        return new Point(startX, startY);
    }

    /**
     * @return The current position of this Board.
     */
    public Point getPosition() {
        return new Point(currX, currY);
    }

    /**
     * Gets the total matches done in this Board without changing the Board.
     *
     * @return ArrayList of all matches on board.
     */
    public ArrayList<Match> getTotalMatches() {
        Board b = new Board(this);
        ArrayList<Match> matches = new ArrayList<>();
        ArrayList<Match> add;
        while ((add = b.matchInPlace()).size() > 0) {
            matches.addAll(add);
            b.cascade();
        }
        return matches;
    }

    /**
     * Gets the in place matches for the given Orb for this board without changing the Board.
     *
     * @param o Orb to look for matches.
     * @return ArrayList of all in place matches of the given orb in this Board.
     */
    public ArrayList<Match> getInPlaceMatchesFor(Orb o) {
        ArrayList<Match> matches = new ArrayList<>();

        // Make table of positions that contain three of same orb in sequence
        boolean found[][] = new boolean[rows][columns];
        // Check for horizontal 3 matches
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns - 2; j++) {
                if (board[i][j] == o && board[i][j + 1] == o && board[i][j + 2] == o) {
                    found[i][j] = true;
                    found[i][j + 1] = true;
                    found[i][j + 2] = true;
                }
            }
        }

        // Check for vertical 3 matches
        for (int i = 0; i < rows - 2; i++) {
            for (int j = 0; j < columns; j++) {
                if (board[i][j] == o && board[i + 1][j] == o && board[i + 2][j] == o) {
                    found[i][j] = true;
                    found[i + 1][j] = true;
                    found[i + 2][j] = true;
                }
            }
        }

        // Make list of each match with each point in that match, horizontal
        ArrayList<ArrayList<Point>> horizontalMatches = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                ArrayList<Point> points = new ArrayList<>();
                int numOrbs = 0;
                int pos = j;
                while (pos < columns && found[i][pos]) {
                    numOrbs++;
                    points.add(new Point(pos, i));
                    pos++;
                }
                if (numOrbs >= 3)
                    horizontalMatches.add(points);
                j = pos;
            }
        }

        // Combine horizontal matches that have at least one orb touching
        for (int i = 0; i < horizontalMatches.size(); i++) {
            ArrayList<Point> current = horizontalMatches.get(i);
            for (int j = i + 1; j < horizontalMatches.size(); j++) {
                ArrayList<Point> next = horizontalMatches.get(j);
                boolean connected = false;
                for (Point p1 : current) {
                    for (Point p2 : next)
                        if (p1.x == p2.x)
                            if (p1.y + 1 == p2.y || p1.y - 1 == p2.y) {
                                connected = true;
                                break;
                            }
                    if (connected)
                        break;
                }
                if (connected) {
                    horizontalMatches.get(i).addAll(horizontalMatches.get(i + 1));
                    horizontalMatches.remove(i + 1);
                }
            }
        }

        // Make list of each match with each point in that match, vertical
        ArrayList<ArrayList<Point>> verticalMatches = new ArrayList<>();
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                ArrayList<Point> points = new ArrayList<>();
                int numOrbs = 0;
                int pos = i;
                while (pos < rows && found[pos][j]) {
                    numOrbs++;
                    points.add(new Point(j, pos));
                    pos++;
                }
                if (numOrbs >= 3)
                    verticalMatches.add(points);
                i = pos;
            }
        }

        // Combine vertical matches that have at least one orb touching
        for (int i = 0; i < verticalMatches.size(); i++) {
            ArrayList<Point> current = verticalMatches.get(i);
            for (int j = i + 1; j < verticalMatches.size(); j++) {
                ArrayList<Point> next = verticalMatches.get(j);
                boolean connected = false;
                for (Point p1 : current) {
                    for (Point p2 : next)
                        if (p1.y == p2.y)
                            if (p1.x + 1 == p2.x || p1.x - 1 == p2.x) {
                                connected = true;
                                break;
                            }
                    if (connected)
                        break;
                }
                if (connected) {
                    verticalMatches.get(i).addAll(verticalMatches.get(i + 1));
                    verticalMatches.remove(i + 1);
                }
            }
        }

        // Consolidate all horizontal and vertical matches
        ArrayList<ArrayList<Point>> allMatches = new ArrayList<>();
        allMatches.addAll(horizontalMatches);
        allMatches.addAll(verticalMatches);

        // Combine any horizontal and vertical matches that share orbs
        for (int i = 0; i < allMatches.size(); i++) {
            for (int j = 0; j < allMatches.size(); j++) {
                if (!allMatches.get(i).equals(allMatches.get(j))) {
                    ArrayList<Point> intersection = new ArrayList<>();

                    intersection.addAll(allMatches.get(i));
                    intersection.retainAll(allMatches.get(j));
                    if (intersection.size() > 0) {
                        allMatches.get(i).removeAll(allMatches.get(j));
                        allMatches.get(i).addAll(allMatches.get(j));
                        allMatches.remove(j);
                        i = 0;
                        j = 0;
                    }
                }
            }
        }

        // Add to Match list each type of match and how many orbs it contains.
        for (int i = 0; i < allMatches.size(); i++) {
            matches.add(new Match(allMatches.get(i).size(), o, allMatches.get(i)));
        }
        return matches;
    }

    /**
     * Gets in place matches for this Board without changing the Board.
     *
     * @return ArrayList of all in place matches for this Board.
     */
    public ArrayList<Match> getInPlaceMatches() {
        Board b = new Board(this);
        return b.matchInPlace();
    }

    /**
     * Gets the orb count of every type of Orb in the Board.
     *
     * @return HashMap where the Key is an Orb, and Value is the number of orbs.
     */
    public HashMap<Orb, Integer> orbCount() {
        HashMap<Orb, Integer> orbCount = new HashMap<>();
        for (Orb o : Orb.values()) {
            int count = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (board[i][j] == o) {
                        count++;
                    }
                }
            }
            orbCount.put(o, count);
        }
        return orbCount;
    }

    /**
     * @return Max number of combos possible in a Board with n rows, and m columns.
     */
    public int maxComboNum() {
        return (int) Math.floor((rows * columns) / 3.0);
    }

    /**
     * @return Max number of combos possible in this Board with the current Orbs.
     */
    public int maxPossibleCombos() {
        HashMap<Orb, Integer> orbCount = orbCount();
        int count = 0;
        for (Map.Entry<Orb, Integer> e : orbCount.entrySet()) {
            count += Math.floor(e.getValue() / 3.0);
        }
        return count;
    }

    /**
     * Sets the start for this Board to start moving using move(Direction).
     * Setting the start will reset any moves done, and the current position.
     *
     * @param column Column to start at.
     * @param row    Row to start at.
     */
    public void setStart(int column, int row) {
        this.startX = column;
        this.startY = row;
        this.currX = column;
        this.currY = row;
        moves.clear();
    }

    /**
     * @return Returns a long string representation for copy/paste.
     */
    public String longString() {
        StringBuilder s = new StringBuilder("");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                s.append(board[i][j].letter());
            }
        }
        return s.toString();
    }

    /**
     * @return String for use in console/printing.
     */
    public String toString() {
        StringBuilder s = new StringBuilder("  ");

        for (int i = 0; i < columns; i++) {
            s.append(i);
            s.append(" ");
        }
        s.append("\n");
        for (int i = 0; i < rows; i++) {
            s.append(i);
            s.append("|");
            for (int j = 0; j < columns; j++) {
                s.append(board[i][j].letter());
                s.append("|");
            }
            s.append("\n");
        }
        return s.toString();
    }

    /**
     * Checks if this Board is equal to another.
     * Two Boards are equal IFF:
     * -Their move sizes are the same
     * -Their current positions are the same
     * -And their Orb positions are the same
     *
     * @param object Other object to compare.
     * @return true if equal, false otherwise.
     */
    public boolean equals(Object object) {
        if (object instanceof Board) {
            Board otherBoard = (Board) object;
            if (otherBoard.getMoves().size() != getMoves().size())
                return false;
//            if (!otherBoard.getStart().equals(getStart()))
//                return false;
            if (!otherBoard.getPosition().equals(getPosition()))
                return false;
            if (columns == otherBoard.columns && rows == otherBoard.rows) {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        if (board[i][j] != otherBoard.board[i][j])
                            return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
