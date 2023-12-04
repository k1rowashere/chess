package gui;

import core.ChessGame;

import javax.swing.*;
import java.awt.event.ActionListener;

class MenuBar extends JMenuBar {
    public MenuBar(ActionListener onUndo) {
        JMenu menu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem rollback = new JMenuItem("Undo");
        rollback.addActionListener(onUndo);
        menu.add(newGame);
        menu.add(rollback);
        this.add(menu);
    }
}
