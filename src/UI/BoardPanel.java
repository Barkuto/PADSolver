package UI;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import Board.*;
import Solver.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by Iggie on 6/27/2017.
 */
public class BoardPanel extends JPanel {
    private class ImgAlphaPair {
        private BufferedImage img;
        private float alpha;

        private ImgAlphaPair(BufferedImage img, float alpha) {
            this.img = img;
            this.alpha = alpha;
        }
    }

    private Board initialBoard, board;
    private int tileWidth, tileHeight;
    private Orb pressedOrb;
    private boolean running, holding, canMoveOrbs, moved, solving;
    private int currentMouseX, currentMouseY, currentBoardX, currentBoardY;

    private ImgAlphaPair[][] boardImgs;
    private boolean matchAnimating;

    private MatchesPanel matchesPanel;
    private ArrayList<Direction> moves;
    private boolean showMoves;
    private int movesStartX, movesStartY;

    private SolutionsPanel solutionsPanel;
    private TextToBoardPanel textToBoardPanel;
    private HeuristicPanel heuristicPanel;

    private Solver solver;

    BoardPanel(int columns, int rows, MatchesPanel matchesPanel, SolutionsPanel solutionsPanel, TextToBoardPanel textToBoardPanel, HeuristicPanel heuristicPanel) {
        tileWidth = Resources.tileWidth;
        tileHeight = Resources.tileHeight;

        this.board = new Board(columns, rows);
        board.randomize();
//        board.withLayout("BDHHHRBDHDRBDHBRDHBBDRDHRHHRDD");
        initialBoard = new Board(board);
        Dimension preferredSize = new Dimension(tileWidth * board.getColumns(), tileHeight * board.getRows());
        setPreferredSize(preferredSize);

        this.matchesPanel = matchesPanel;
        matchesPanel.setPreferredSize(new Dimension(preferredSize.width / 3, preferredSize.height));

        this.solutionsPanel = solutionsPanel;
        solutionsPanel.setPreferredSize(new Dimension(preferredSize.width / 2, preferredSize.height));
        solutionsPanel.init(this);

        this.textToBoardPanel = textToBoardPanel;
        textToBoardPanel.setBoardPanel(this);

        this.heuristicPanel = heuristicPanel;

        solver = new Solver();

        MyMouseListener ml = new MyMouseListener();
        addMouseListener(ml);
        addMouseMotionListener(ml);

        moves = new ArrayList<Direction>();
        showMoves = false;
        matchAnimating = false;
        running = false;
        holding = false;
        canMoveOrbs = true;
        solving = false;

        boardImgs = new ImgAlphaPair[board.getRows()][board.getColumns()];
        updateBoardImgs();
    }

    private void updateBoardImgs() {
        for (int x = 0; x < board.getColumns(); x++)
            for (int y = 0; y < board.getRows(); y++)
                boardImgs[y][x] = new ImgAlphaPair(Resources.getOrbImage(board.orbAt(y, x)), 1.0f);
    }

    void updateMoves(Board board) {
        movesStartX = board.getStart().x;
        movesStartY = board.getStart().y;
        moves = new ArrayList<>(board.getMoves());
        showMoves = true;
    }

    void toggleMoves() {
        showMoves = !showMoves;
    }

    void resetBoard() {
        matchAnimating = false;
        board = new Board(initialBoard);

        updateBoardImgs();
        canMoveOrbs = true;
        holding = false;
        moved = false;
        pressedOrb = null;

        matchesPanel.clearMatches();
        moves.clear();
        showMoves = false;

        solutionsPanel.unSelect();
    }

    void randomize(boolean all) {
        matchAnimating = false;
        cancelSolve();
        heuristicPanel.resetStatus();
        if (all)
            board.randomizeWithAll();
        else board.randomize();
        initialBoard = new Board(board);

        updateBoardImgs();
        canMoveOrbs = true;
        holding = false;
        moved = false;
        pressedOrb = null;

        matchesPanel.clearMatches();
        moves.clear();
        showMoves = false;

        solutionsPanel.clearSolutions();

        textToBoardPanel.update();
    }

    private void lockUser() {
        canMoveOrbs = false;
        holding = false;
        moved = false;
        pressedOrb = null;
    }

    private void unlockUser() {
        canMoveOrbs = true;
        holding = false;
        moved = false;
        pressedOrb = null;
    }

    private void match() {
        lockUser();
        ArrayList<Match> matches = new ArrayList<>();
        new Thread(() -> {
            matchAnimating = true;
            ArrayList<Match> inPlaceMatches;
            while ((inPlaceMatches = board.getInPlaceMatches()).size() > 0) {
                // Delay to start the next matching after cascading
                if (!matchAnimating)
                    return;
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Match m : inPlaceMatches) {
                    if (!matchAnimating)
                        return;
                    ArrayList<Point> points = m.getOrbPositions();
                    double totalAlpha = 1;
                    while (totalAlpha > 0) {
                        if (!matchAnimating)
                            return;
                        totalAlpha = 0;
                        for (Point p : points) {
                            boardImgs[p.y][p.x].alpha -= 0.1f;
                            if (boardImgs[p.y][p.x].alpha < 0)
                                boardImgs[p.y][p.x].alpha = 0;
                            totalAlpha += boardImgs[p.y][p.x].alpha;
                        }
                        // Fade time
                        if (!matchAnimating)
                            return;
                        try {
                            sleep(25);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // Delay between starting next match fading
                    if (!matchAnimating)
                        return;
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!matchAnimating)
                    return;
                matches.addAll(board.matchInPlace());
                while (board.cascadeOnce()) {
                    if (!matchAnimating)
                        return;
                    updateBoardImgs();
                    // Delay for cascade speed
                    try {
                        sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            if (!matchAnimating)
                return;
            matchAnimating = false;
            matchesPanel.setMatches(matches, board.getMoves().size());
            updateMoves(board);
        }).start();
    }

    boolean isSolving() {
        return solving;
    }

    void solve() {
        if (!solving) {
            solving = true;

            solutionsPanel.unSelect();
            solutionsPanel.clearSolutions();
            showMoves = false;

            new Thread(() -> {
                int periods = 0;
                while (solving) {
                    if (periods > 4)
                        periods = 0;
                    StringBuilder s = new StringBuilder("Solving.");
                    for (int i = 0; i < periods; i++) {
                        s.append(".");
                    }
                    heuristicPanel.setStatus(s.toString());
                    periods++;
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(() -> {
                long start = System.currentTimeMillis();

                ArrayList<Solver.SolutionEntry> solutions = solver.solve(initialBoard, heuristicPanel.getHeuristic(), heuristicPanel.getMaxMoves(), heuristicPanel.getScale());
//                ArrayList<Solver.SolutionEntry> solutions = solver.solveWithStart(initialBoard, heuristicPanel.getHeuristic(), heuristicPanel.getMaxMoves(), heuristicPanel.getScale(), 1, 3);
                solving = false;
                if (solutions != null) {
                    heuristicPanel.setStatus("Done in " + (System.currentTimeMillis() - start) / 1000 + "s");
                    solutionsPanel.setSolutions(solutions);
                }
            }).start();
        }
    }

    void cancelSolve() {
        solver.stopSolving();
        solving = false;
        heuristicPanel.setStatus("Canceled");
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (int x = 0; x < board.getColumns(); x++) {
            int xpos = x * tileWidth;
            for (int y = 0; y < board.getRows(); y++) {
                int ypos = y * tileHeight;
                if ((x % 2 == 0 && y % 2 == 0) || (x % 2 != 0 && y % 2 != 0))
                    g.drawImage(Resources.bg1, xpos, ypos, null);
                else
                    g.drawImage(Resources.bg2, xpos, ypos, null);
                if (holding && canMoveOrbs && y == currentBoardY && x == currentBoardX)
                    g.drawImage(DrawUtils.makeTranslucent(Resources.getOrbImage(board.orbAt(y, x)), 0.5f), xpos, ypos, null);
                else
                    g.drawImage(DrawUtils.makeTranslucent(boardImgs[y][x].img, boardImgs[y][x].alpha), xpos, ypos, null);
            }
        }

        if (holding && canMoveOrbs && pressedOrb != null) {
            BufferedImage orbImg = Resources.getOrbImage(pressedOrb);
            int drawX = currentMouseX;
            int drawY = currentMouseY;
            if (drawX + (orbImg.getWidth() / 2) > getWidth()) {
                drawX = getWidth() - (orbImg.getWidth() / 2);
            } else if (drawX - (orbImg.getWidth() / 2) < 0) {
                drawX = (orbImg.getWidth() / 2);
            }
            if (drawY + (orbImg.getHeight() / 2) > getHeight()) {
                drawY = getHeight() - (orbImg.getHeight() / 2);
            } else if (drawY - (orbImg.getHeight() / 2) < 0) {
                drawY = (orbImg.getHeight() / 2);
            }
            drawX -= (orbImg.getWidth() / 2);
            drawY -= (orbImg.getHeight() / 2);
            g.drawImage(DrawUtils.makeTranslucent(orbImg, 0.8f), drawX, drawY, null);
        }

        if (showMoves) {
            // Iterate through moves array
            // Draw from middle of current spot to the edge of where the next one is
            // curveTo using enteringEdge, middleSpot, exitingEdge
            // Have an offset for middleSpot to offset line for better overlapping differences

            // Positions vars
            int lastX;
            int lastY;
            int currX = movesStartX * tileWidth + (tileWidth / 2);
            int currY = movesStartY * tileHeight + (tileHeight / 2);

            // Offset vars
            int xOffset = 0;
            int yOffset = 0;
            int offset = tileWidth / 15;
            int xDir = 1;
            int yDir = 1;

            // Gradient vars
            Color color1 = Color.BLUE;
            Color color2 = Color.YELLOW;
            Color color3 = Color.RED;
            double p1 = 0;
            double p2 = 0;
            double pChange = 2.0 / moves.size();

            int i = 0;
            for (Direction d : moves) {
                int arrowPositions = 5;
                if (xOffset >= tileWidth / 2 - (tileWidth / arrowPositions))
                    xDir = -1;
                else if (xOffset <= -tileWidth / 2 + (tileWidth / arrowPositions))
                    xDir = 1;
                if (yOffset >= tileHeight / 2 - (tileWidth / arrowPositions))
                    yDir = -1;
                else if (yOffset <= -tileHeight / 2 + (tileWidth / arrowPositions))
                    yDir = 1;

                lastX = currX;
                lastY = currY;
                switch (d) {
                    case UP:
                        currY -= tileHeight;
                        break;
                    case DOWN:
                        currY += tileHeight;
                        break;
                    case LEFT:
                        currX -= tileWidth;
                        break;
                    case RIGHT:
                        currX += tileWidth;
                        break;
                    case UPLEFT:
                        currY -= tileHeight;
                        currX -= tileWidth;
                        break;
                    case UPRIGHT:
                        currY -= tileHeight;
                        currX += tileWidth;
                        break;
                    case DOWNLEFT:
                        currY += tileHeight;
                        currX -= tileWidth;
                        break;
                    case DOWNRIGHT:
                        currY += tileHeight;
                        currX += tileWidth;
                        break;
                }

                int cWidth = tileWidth / 2;
                int cHeight = tileHeight / 2;
                if (i == 0) {
                    int x = lastX - cWidth / 2;
                    int y = lastY - cHeight / 2;
                    g.setColor(Color.GREEN);
                    g.fillOval(x, y, cWidth, cHeight);
                    g.setColor(Color.BLACK);
                    g.drawOval(x, y, cWidth, cHeight);
                }
                if (i == moves.size() - 1) {
                    int x = currX - cWidth / 2;
                    int y = currY - cHeight / 2;
                    g.setColor(Color.RED);
                    g.fillOval(x, y, cWidth, cHeight);
                    g.setColor(Color.BLACK);
                    g.drawOval(x, y, cWidth, cHeight);
                }

                if (p1 > 1)
                    p1 = 1;
                if (p2 > 1)
                    p2 = 1;
                int red = (int) (p1 < 1 ? color1.getRed() * (1 - p1) + color2.getRed() * p1 :
                        color2.getRed() * (1 - p2) + color3.getRed() * p2);
                int green = (int) (p1 < 1 ? color1.getGreen() * (1 - p1) + color2.getGreen() * p1 :
                        color2.getGreen() * (1 - p2) + color3.getGreen() * p2);
                int blue = (int) (p1 < 1 ? color1.getBlue() * (1 - p1) + color2.getBlue() * p1 :
                        color2.getBlue() * (1 - p2) + color3.getBlue() * p2);
                if (p1 < 1)
                    p1 += pChange;
                else if (p2 < 1)
                    p2 += pChange;
                g.setColor(new Color(red, green, blue));

                Shape arrow = DrawUtils.createArrowShape(new Point(lastX + xOffset, lastY + yOffset), new Point(currX + xOffset, currY + yOffset));
                ((Graphics2D) g).fill(arrow);
                g.setColor(Color.BLACK);
                ((Graphics2D) g).draw(arrow);
                DrawUtils.drawCenteredString(g, i + 1 + "", new Rectangle((currX + lastX) / 2 + xOffset, (currY + lastY) / 2 + yOffset, 0, 0), new Font(g.getFont().getName(), Font.BOLD, 16));
                i++;

                switch (d) {
                    case UP:
                    case DOWN:
                        xOffset += offset * xDir;
                        break;
                    case LEFT:
                    case RIGHT:
                        yOffset += offset * yDir;
                        break;
                    case UPLEFT:
                    case UPRIGHT:
                    case DOWNLEFT:
                    case DOWNRIGHT:
                        xOffset += offset * xDir;
                        break;
                }
            }
        }
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        resetBoard();
        this.initialBoard = new Board(board);
        this.board = new Board(board);
        updateBoardImgs();
    }

    private Point checkBounds(int x, int y) {
        int correctX = x;
        int correctY = y;
        if (x < 0)
            correctX = 0;
        if (x > board.getColumns() - 1)
            correctX = board.getColumns() - 1;
        if (y < 0)
            correctY = 0;
        if (y > board.getRows() - 1)
            correctY = board.getRows() - 1;
        return new Point(correctX, correctY);
    }

    private Point getBoardPosFromMousePos(int mouseX, int mouseY) {
        int centerX = (mouseX < 0 ? 0 : mouseX > getWidth() ? getWidth() : mouseX) / tileWidth * tileWidth + tileWidth / 2;
        int centerY = (mouseY < 0 ? 0 : mouseY > getHeight() ? getHeight() : mouseY) / tileHeight * tileHeight + tileHeight / 2;
        int deadzoneSize = 5;
        int deadzoneX = tileWidth / 2 - deadzoneSize;
        int deadzoneY = tileHeight / 2 - deadzoneSize;
        int leftXBound = centerX - deadzoneX;
        int rightXBound = centerX + deadzoneX;
        int upYBound = centerY - deadzoneY;
        int downYBound = centerY + deadzoneY;
        if ((mouseX > leftXBound && mouseX < rightXBound && mouseY > upYBound && mouseY < downYBound)
                || (mouseX >= getWidth() || mouseX <= 0 || mouseY >= getHeight() || mouseY <= 0))
            return checkBounds(mouseX / tileWidth, mouseY / tileHeight);
        return null;
    }

    void start() {
        running = true;
        while (running) {
            repaint();
            try {
                sleep(1000 / 144);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        startTimer = new Timer(1000 / 144, (e) -> repaint());
//        startTimer.start();
//        running = true;
    }

    void stop() {
        running = false;
        lockUser();
//        startTimer.stop();
//        running = false;
//        lockUser();
    }

    private class MyMouseListener implements MouseInputListener {
        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                if (canMoveOrbs) {
                    currentMouseX = e.getX() < 0 ? 0 : e.getX() > getWidth() ? getWidth() : e.getX();
                    currentMouseY = e.getY() < 0 ? 0 : e.getY() > getHeight() ? getHeight() : e.getY();
                    Point boardPos = getBoardPosFromMousePos(currentMouseX, currentMouseY);
                    int xMove = 0;
                    int yMove = 0;
                    if (Math.abs(currentBoardX - boardPos.x) == 1 && Math.abs(currentBoardY - boardPos.y) <= 1) {
                        if (boardPos.x != currentBoardX) {
                            if (boardPos.x > currentBoardX)
                                xMove++;
                            else if (boardPos.x < currentBoardX)
                                xMove--;
                            currentBoardX = boardPos.x;
                        }
                    }
                    if (Math.abs(currentBoardY - boardPos.y) == 1 && Math.abs(currentBoardX - boardPos.x) <= 1) {
                        if (boardPos.y != currentBoardY) {
                            if (boardPos.y > currentBoardY)
                                yMove++;
                            else if (boardPos.y < currentBoardY)
                                yMove--;
                            currentBoardY = boardPos.y;
                        }
                    }

                    Direction dir = parseDir(xMove, yMove);
                    if (dir != null) {
                        board.move(dir);
                        updateBoardImgs();
                        moved = true;
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (canMoveOrbs) {
                holding = true;
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                currentBoardX = e.getX() / tileWidth;
                currentBoardY = e.getY() / tileHeight;
                pressedOrb = board.orbAt(currentBoardY, currentBoardX);
                board.setStart(currentBoardX, currentBoardY);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            holding = false;
            pressedOrb = null;
            if (moved)
                match();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private Direction parseDir(int xChange, int yChange) {
            if (xChange == 0 && yChange < 0)
                return Direction.UP;
            if (xChange == 0 && yChange > 0)
                return Direction.DOWN;
            if (xChange < 0 && yChange == 0)
                return Direction.LEFT;
            if (xChange > 0 && yChange == 0)
                return Direction.RIGHT;

            if (xChange < 0 && yChange < 0)
                return Direction.UPLEFT;
            if (xChange > 0 && yChange < 0)
                return Direction.UPRIGHT;
            if (xChange < 0 && yChange > 0)
                return Direction.DOWNLEFT;
            if (xChange > 0 && yChange > 0)
                return Direction.DOWNRIGHT;
            return null;
        }
    }
}
