package gui;

import javax.swing.*;
import java.awt.event.ActionListener;

class MenuBar extends JMenuBar {
    public MenuBar(ActionListener onUndo, ActionListener onNewGame) {
        JMenu menu = new JMenu("Game");
        createMenuItem("Undo", onUndo, menu);
        createMenuItem("New Game", onNewGame, menu);

        this.add(menu);
    }

    private static void createMenuItem(String name, ActionListener action, JMenu menu) {
        var item = new JMenuItem(name);
        item.addActionListener(action);
        menu.add(item);
    }
}
