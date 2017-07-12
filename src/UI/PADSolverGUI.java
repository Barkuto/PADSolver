package UI;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

/**
 * Created by Iggie on 6/27/2017.
 */
public class PADSolverGUI {
    private JFrame mainFrame;
    private JPanel buttonPanel;
    private BoardPanel boardPanel;
    private MatchesPanel matchesPanel;
    private SolutionsPanel solutionsPanel;
    private TextToBoardPanel textPanel;
    private HeuristicPanel heuristicPanel;

    public PADSolverGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Resources.load();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | IOException | UnsupportedLookAndFeelException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            e.printStackTrace();
        }

        int columns = 6;
        int rows = 5;
        mainFrame = new JFrame("PAD Solver by Barkuto");
        matchesPanel = new MatchesPanel();
        solutionsPanel = new SolutionsPanel();
        textPanel = new TextToBoardPanel();
        heuristicPanel = new HeuristicPanel();
        boardPanel = new BoardPanel(columns, rows, matchesPanel, solutionsPanel, textPanel, heuristicPanel);
        initButtonPanel();

        mainFrame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        mainFrame.add(boardPanel, c);

        c = new GridBagConstraints();
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        mainFrame.add(buttonPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        mainFrame.add(matchesPanel, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        mainFrame.add(solutionsPanel, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        mainFrame.add(textPanel, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        mainFrame.add(heuristicPanel, c);

        mainFrame.setResizable(false);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 5));

        JButton resetButton = new JButton("Reset Board");
        resetButton.addActionListener(e -> boardPanel.resetBoard());

        JButton randomButton = new JButton("Randomize(RBGLDH)");
        randomButton.addActionListener(e -> boardPanel.randomize(false));

        JButton randomButton2 = new JButton("Randomize(All)");
        randomButton2.addActionListener(e -> boardPanel.randomize(true));

        JButton movesToggleButton = new JButton("Toggle Moves");
        movesToggleButton.addActionListener(e -> boardPanel.toggleMoves());

        JButton solveButton = new JButton("Solve");

        String solveText = "Solve";
        String cancelText = "Cancel Solve";
        solveButton.addActionListener(e -> {
            if (solveButton.getText().equalsIgnoreCase(solveText)) {
                solveButton.setText(cancelText);
                boardPanel.solve();
                new Thread(() -> {
                    while (boardPanel.isSolving()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                    solveButton.setText(solveText);
                }).start();
            } else if (solveButton.getText().equalsIgnoreCase(cancelText)) {
                solveButton.setText(solveText);
                boardPanel.cancelSolve();
            }
        });

        buttonPanel.add(resetButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(randomButton2);
        buttonPanel.add(movesToggleButton);
        buttonPanel.add(solveButton);
    }

    public void start() {
        mainFrame.setVisible(true);
        boardPanel.start();
    }

    public static void main(String[] args) {
        PADSolverGUI gui = new PADSolverGUI();
        gui.start();
    }
}
