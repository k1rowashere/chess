import core.ChessBoard;
import core.ChessGame;
import gui.Game;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        var game = new ChessGame();
        var gui = new Game(game);


        SwingUtilities.invokeLater(() -> gui.setVisible(true));
    }
}