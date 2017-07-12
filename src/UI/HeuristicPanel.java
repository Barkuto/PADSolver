package UI;

import Board.Orb;
import Solver.ComboType;
import Solver.Heuristic;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

/**
 * Created by Iggie on 7/9/2017.
 */
public class HeuristicPanel extends JPanel {
    private JComboBox<ComboType> comboTypes;
    private JComboBox<Orb> orbTypes;
    private JTextArea maxMoves, pathScale;

    private JLabel status, combo, orb, moves, scale;

    private JButton reset;
    private String defaultStatus = "IDLE";

    HeuristicPanel() {
        comboTypes = new JComboBox<>(ComboType.values());
        orbTypes = new JComboBox<>(Orb.values());

        maxMoves = new JTextArea();
        pathScale = new JTextArea();

        combo = new JLabel("Heuristic");
        orb = new JLabel("Primary Orb");
        moves = new JLabel("Max Moves");
        scale = new JLabel("Logic scalar");
        status = new JLabel(defaultStatus);
        reset = new JButton("Reset");

        ((JLabel) comboTypes.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        ((JLabel) orbTypes.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        combo.setHorizontalAlignment(SwingConstants.CENTER);
        orb.setHorizontalAlignment(SwingConstants.CENTER);
        moves.setHorizontalAlignment(SwingConstants.CENTER);
        scale.setHorizontalAlignment(SwingConstants.CENTER);
        status.setHorizontalAlignment(SwingConstants.CENTER);
        reset.setHorizontalAlignment(SwingConstants.CENTER);

        Font f = new Font(comboTypes.getFont().getName(), comboTypes.getFont().getStyle(), 16);
        Font f2 = new Font(comboTypes.getFont().getName(), comboTypes.getFont().getStyle(), 30);

        comboTypes.setFont(f);
        orbTypes.setFont(f);
        maxMoves.setFont(f);
        pathScale.setFont(f);

        status.setFont(f2);
        combo.setFont(f);
        orb.setFont(f);
        moves.setFont(f);
        scale.setFont(f);

        orbTypes.setSelectedItem(Orb.BLANK);

        reset.addActionListener((e) -> reset());

        maxMoves.setColumns(4);
        maxMoves.setBorder(new LineBorder(Color.BLACK));
        maxMoves.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                try {
                    Integer.parseInt(str);
                } catch (Exception e) {
                    return;
                }
                if (maxMoves.getText().length() >= 4) {
                    return;
                }
                super.insertString(offs, str, a);
            }
        });

        pathScale.setColumns(4);
        pathScale.setBorder(new LineBorder(Color.BLACK));
        pathScale.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                try {
                    Integer.parseInt(str);
                } catch (Exception e) {
                    return;
                }
                if (pathScale.getText().length() >= 4) {
                    return;
                }
                super.insertString(offs, str, a);
            }
        });

        maxMoves.append("20");
        pathScale.append("100");

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridwidth = 2;
        add(status, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 1;
        add(combo, c);
        c.gridx = 1;
        add(comboTypes, c);

        c.gridx = 0;
        c.gridy = 2;
        add(orb, c);
        c.gridx = 1;
        add(orbTypes, c);

        c.gridx = 0;
        c.gridy = 3;
        add(moves, c);
        c.gridx = 1;
        add(maxMoves, c);

        c.gridx = 0;
        c.gridy = 4;
        add(scale, c);
        c.gridx = 1;
        add(pathScale, c);

        c.gridx = 1;
        c.gridy = 5;
        add(reset, c);
    }

    Orb getOrb() {
        return (Orb) orbTypes.getSelectedItem();
    }

    ComboType getComboType() {
        return (ComboType) comboTypes.getSelectedItem();
    }

    Heuristic getHeuristic() {
        return new Heuristic(getOrb(), getComboType());
    }

    int getMaxMoves() {
        return Integer.parseInt(maxMoves.getText());
    }

    int getScale() {
        return Integer.parseInt(pathScale.getText());
    }

    void setStatus(String status) {
        this.status.setText(status.toUpperCase());
    }

    void resetStatus() {
        this.status.setText(defaultStatus);
    }

    void reset() {
        comboTypes.setSelectedItem(ComboType.COMBO);
        orbTypes.setSelectedItem(Orb.BLANK);
        maxMoves.setText(null);
        maxMoves.append("20");
        pathScale.setText(null);
        pathScale.setText("100");
    }
}
