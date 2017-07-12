package UI;

import Board.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Created by Iggie on 7/8/2017.
 */
class TextToBoardPanel extends JPanel {
    private JTextPane textPane;
    private BoardPanel boardPanel;

    void setBoardPanel(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;

        int rows = boardPanel.getBoard().getRows();
        int columns = boardPanel.getBoard().getColumns();

        textPane = new JTextPane();
        textPane.setEditorKit(new WrapEditorKit());
        textPane.setDocument(new DefaultStyledDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null || textPane.getText().length() >= columns * rows) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
                super.insertString(offs, validateString(str), a);
            }
        });

        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String fontName = "Times New Roman";
        for (String s : fonts)
            if (s.equalsIgnoreCase("Courier") || s.contains("Courier")) {
                fontName = s;
                break;
            }
        textPane.setFont(new Font(fontName, Font.PLAIN, 30));
        FontMetrics m = textPane.getFontMetrics(textPane.getFont());
        textPane.setPreferredSize(new Dimension(m.charWidth('A') * columns + m.getMaxAdvance() / 2, m.getHeight() * rows));
        textPane.setText(boardPanel.getBoard().longString());

        JButton apply = new JButton("Apply");
        apply.addActionListener((e) -> {
            Board b = new Board(boardPanel.getBoard());
            b.withLayout(removeSpecial(textPane.getText()));
            boardPanel.setBoard(b);
        });

        setLayout(new BorderLayout());
        add(textPane, BorderLayout.CENTER);
        add(apply, BorderLayout.SOUTH);
    }

    void update() {
        textPane.setText(null);
        textPane.setText(boardPanel.getBoard().longString());
    }

    private String removeSpecial(String str) {
        return str.replaceAll("[^A-Za-z0-9]", "");
    }

    private String validateString(String str) {
        StringBuilder sb = new StringBuilder();
        sb.ensureCapacity(str.length());
        for (int i = 0; i < str.length(); i++) {
            char toAdd = str.toUpperCase().charAt(i);
            if (isValidOrbChar(toAdd))
                sb.append(toAdd);
            else sb.append('X');
        }
        return sb.toString();
    }

    private boolean isValidOrbChar(char c) {
        return Orb.orbFromLetter(c) != null;
    }

    /**
     * https://stackoverflow.com/questions/22128564/jtextpane-line-wrap
     */
    private class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();

        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

    }

    private class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null)
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            // default to text display
            return new LabelView(elem);
        }
    }

    private class WrapLabelView extends LabelView {
        WrapLabelView(Element elem) {
            super(elem);
        }

        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }

    }
}
