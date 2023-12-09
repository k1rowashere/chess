package gui;

import core.move.QualifiedMove;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class MoveHistory extends JPanel {
    private final Consumer<Integer> onMoveSelect;
    private final JPanel innerPanel = new JPanel();

    /**
     * @param dim          The dimension of the panel
     * @param onMoveSelect A callback that is called when a move is selected,
     *                     with the move idx
     */
    public MoveHistory(Dimension dim, Consumer<Integer> onMoveSelect) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        GridLayout mgr = new GridLayout(0, 2);
        this.setAlignmentY(Component.TOP_ALIGNMENT);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        mgr.setHgap(10);
        mgr.setVgap(10);
        this.innerPanel.setLayout(mgr);
        this.onMoveSelect = onMoveSelect;

        this.setPreferredSize(dim);

        this.add(Box.createVerticalStrut(10));
        this.add(innerPanel);
        this.add(Box.createVerticalStrut(100000000));
    }

    public void addMove(QualifiedMove move) {
        var acn = move.partialAlgebraicNotation();
        var size = this.innerPanel.getComponentCount();

        var moveNo = (size % 2 == 0 ? (size / 2) + 1 + ". " : "");
        JLabel label = new JLabel(moveNo + acn);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMoveSelect.accept(size + 1);
            }
        });

        this.innerPanel.add(label);
    }

    public void removeLastMove() {
        var p = this.innerPanel;
        int size = p.getComponentCount();

        if (size == 0) return;

        p.remove(size - 1);
        p.revalidate();
        p.repaint();
    }

    public void clear() {
        this.innerPanel.removeAll();
    }
}
