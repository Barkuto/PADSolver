package UI;

import Board.*;
import Solver.Solver;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by Iggie on 7/4/2017.
 */
public class SolutionsPanel extends JPanel {
    private ArrayList<Solver.SolutionEntry> solutions;
    private JList<Board> list;
    private DefaultListModel<Board> model;

    SolutionsPanel() {
        solutions = new ArrayList<>();
        setLayout(new GridLayout());
    }

    void init(BoardPanel boardPanel) {
        list = new JList<Board>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//                label.setText(((Board) value).longString());
                return new SolutionLabel((Board) value, solutions.get(index).getScore(), isSelected, index + 1);
            }
        });
        list.addListSelectionListener((e) -> {
            if (list.getSelectedIndex() >= 0)
                boardPanel.updateMoves((Board) model.get(list.getSelectedIndex()));
        });
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(boardPanel.getPreferredSize());
        add(scrollPane);
    }

    void unSelect() {
        list.clearSelection();
    }

    private void refresh() {
        model = new DefaultListModel<>();
        for (Solver.SolutionEntry e : solutions) {
            Board b = e.getBoard();
            ArrayList<Match> matches = b.getTotalMatches();
            if (matches.size() == 1 && matches.get(0).isExplodedOrbs()) {
            } else model.addElement(b);
        }
        list.setModel(model);
        repaint();
        setVisible(false);
        setVisible(true);
    }

    void setSolutions(ArrayList<Solver.SolutionEntry> solutions) {
        this.solutions = solutions;
        refresh();
    }

    void clearSolutions() {
        solutions.clear();
        refresh();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private class SolutionLabel extends JLabel {
        private Board board;
        private ArrayList<Match> matches;
        private boolean isSelected;
        private int index;
        private double score;

        SolutionLabel(Board board, double score, boolean isSelected, int index) {
            this.board = board;
            this.isSelected = isSelected;
            this.index = index;
            this.score = score;
            this.matches = board.getTotalMatches();
            for (int i = 0; i < matches.size(); i++) {
                if (matches.get(i).isExplodedOrbs()) {
                    matches.remove(i);
                    break;
                }
            }
            this.matches.sort((m1, m2) -> {
                if (m1.getOrbType() != m2.getOrbType())
                    return m1.getOrbType().ordinal() - (m2.getOrbType().ordinal());
                else
                    return m2.getNumOrbs() - m1.getNumOrbs();
            });
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setPreferredSize(new Dimension(getWidth(), 60));
        }

        public void paintComponent(Graphics g) {
            if (isSelected) {
                g.setColor(new Color(255, 255, 186));
                g.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            int xpos = getWidth() / 10;
            int ypos = getHeight() / 10;

            g.setColor(Color.BLACK);
            Font font = new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize());
            FontMetrics metrics = g.getFontMetrics(font);

            DrawUtils.drawCenteredString(g, "#" + index, new Rectangle(xpos, ypos, 0, 0), font);
            xpos = getWidth() * 2 / 8;
            DrawUtils.drawCenteredString(g, "W: " + score, new Rectangle(xpos, ypos, 0, 0), font);
            xpos = getWidth() * 3 / 6;
            DrawUtils.drawCenteredString(g, "Combos: " + matches.size(), new Rectangle(xpos, ypos, 0, 0), font);
            xpos = getWidth() * 5 / 6;
            DrawUtils.drawCenteredString(g, "Moves: " + board.getMoves().size(), new Rectangle(xpos, ypos, 0, 0), font);

            xpos = 0;
            ypos += metrics.getAscent() * 2;

            int imgScale = 4;
            int increment = getWidth() / matches.size();
            for (Match m : matches) {
                Orb orb = m.getOrbType();
                BufferedImage img = Resources.getOrbImage(orb);
                int newWidth = img.getWidth() / imgScale;
                int newHeight = img.getHeight() / imgScale;

                int middleX = (xpos + increment) / 2 - (newWidth / 2);
                int middleY = ypos + (img.getHeight() / imgScale - (img.getHeight() / imgScale));

                xpos += increment * 2;
                g.drawImage(img, middleX, middleY, newWidth, newHeight, null);

                ypos -= metrics.getAscent();
                String str = m.getNumOrbs() + "";
                DrawUtils.drawCenteredString(g, str, new Rectangle(middleX + newWidth / 2, middleY - newHeight / 4, 0, 0), font);
                ypos += metrics.getAscent();
            }
        }
    }
}
