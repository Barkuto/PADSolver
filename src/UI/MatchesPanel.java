package UI;

import Board.Match;
import Board.Orb;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Iggie on 6/29/2017.
 */
public class MatchesPanel extends JPanel {
    private ArrayList<Match> matches;
    private int moves;

    MatchesPanel() {
        matches = new ArrayList<>();
        moves = 0;
    }

    private void refresh() {
        repaint();
        setVisible(false);
        setVisible(true);
    }

    void setMatches(ArrayList<Match> matches, int moves) {
        this.matches = new ArrayList<>(matches);
        this.moves = moves;
        this.matches.sort((m1, m2) -> {
            if (m1.getOrbType() != m2.getOrbType())
                return m1.getOrbType().ordinal() - (m2.getOrbType().ordinal());
            else
                return m2.getNumOrbs() - m1.getNumOrbs();
        });
        refresh();
    }

    void clearMatches() {
        matches.clear();
        moves = 0;
        refresh();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize()));
        int rightBound = getWidth();
        int middle = rightBound / 2;
        int topBound = 0;

        topBound += getHeight() / 10;

        int numCombos = matches.size();
        for (Match m : matches)
            if (m.isExplodedOrbs())
                numCombos--;

        DrawUtils.drawCenteredString(g, "Combos: " + numCombos, new Rectangle(middle - (middle / 2), topBound, 0, 0), g.getFont());
        DrawUtils.drawCenteredString(g, "Moves: " + moves, new Rectangle(middle + (middle / 2), topBound, 0, 0), g.getFont());

        int colCenter = middle / 2 - 16;
        int xpos = colCenter;
        int ypos = topBound;
        int orbTopBound = ypos += 40;
        boolean nextCol = false;

//        g.drawLine(middle / 2, 0, middle / 2, getHeight());
//        g.drawLine((middle / 2) + middle, 0, (middle / 2) + middle, getHeight());

        for (int i = 0; i < matches.size(); i++) {
            if (!matches.get(i).isExplodedOrbs()) {
                if (i >= matches.size() / 2) {
                    xpos += middle;
                    if (!nextCol)
                        ypos = orbTopBound;
                    nextCol = true;
                }
                Orb orb = matches.get(i).getOrbType();
                int numOrbs = matches.get(i).getNumOrbs();
                BufferedImage orbImg = Resources.getOrbImage(orb);

                DrawUtils.drawCenteredString(g, numOrbs + " X", new Rectangle(xpos - 10, ypos, 0, 0), g.getFont());

                int newWidth = orbImg.getWidth() / 2;
                int newHeight = orbImg.getHeight() / 2;
                g.drawImage(orbImg, xpos, ypos - (newHeight / 2), newWidth, newHeight, null);

                ypos += newHeight;
                xpos = colCenter;
            }
        }
    }
}
