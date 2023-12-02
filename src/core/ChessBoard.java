package core;

import core.square.File;
import core.square.Rank;
import core.square.Square;

import javax.swing.*;
import java.awt.Color;
import java.awt.*;

public class ChessBoard {
    private JPanel Main;
    private JButton startButton;
    private JButton newGameButton;
    private JButton undoButton;
    private JButton resignButton;
    private JPanel Options;
    private JPanel Squares;
    private boolean startFlag = false;
    private ChessGame game;

    public ChessBoard(ChessGame game) {
        this.game = game;
        startButton.addActionListener(e -> {
            if (!startFlag) {
                setPieces();
                startFlag = true;
            }
        });
        newGameButton.addActionListener(e -> {
            disableRest();
            setPieces();
        });
    }

    public void SquaresButtons() {

    }

    public void setPieces() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0 || i == 1 || i == 6 || i == 7)
                    Squares.getComponent(i * 9 + j).setEnabled(true);
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 0) {
                    switch (j) {
                        case 0, 7:
                            ((JButton) Squares.getComponent(j)).setIcon(resizeIcon("BlackRook.png"));
                            break;
                        case 1, 6:
                            ((JButton) Squares.getComponent(j)).setIcon(resizeIcon("BlackKnight.png"));
                            break;
                        case 2, 5:
                            ((JButton) Squares.getComponent(j)).setIcon(resizeIcon("BlackBishop.png"));
                            break;
                        case 3:
                            ((JButton) Squares.getComponent(j)).setIcon(resizeIcon("BlackQueen.png"));
                            break;
                        case 4:
                            ((JButton) Squares.getComponent(j)).setIcon(resizeIcon("BlackKing.png"));
                            break;
                        default:
                            break;
                    }
                } else if (i == 1) {
                    if (j < 8) {
                        ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("BlackPawn.png"));
                    }
                } else if (i == 6) {
                    if (j < 8) {
                        ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("WhitePawn.png"));
                    }
                } else if (i == 7) {
                    switch (j) {
                        case 0, 7:
                            ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("WhiteRook.png"));
                            break;
                        case 1, 6:
                            ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("WhiteKnight.png"));
                            break;
                        case 2, 5:
                            ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("WhiteBishop.png"));
                            break;
                        case 3:
                            ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("WhiteQueen.png"));
                            break;
                        case 4:
                            ((JButton) Squares.getComponent(i * 9 + j)).setIcon(resizeIcon("WhiteKing.png"));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }


    public void disableRest() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Squares.getComponent(i * 9 + j).setEnabled(i == 0 || i == 1 || i == 6 || i == 7);

            }
        }
    }

    public ImageIcon resizeIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        java.awt.Image img = icon.getImage();
        java.awt.Image resizedImage = img.getScaledInstance(55, 55, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public JPanel getMain() {
        return Main;
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
        var panel = new JPanel();
        panel.setLayout(new GridLayout(9, 9));
        SquareButton[][] buttons;
        buttons = new SquareButton[8][8];
        for (int i = 0; i < 8; i++) {
            char c = 'A';
            for (int j = 0; j < 8; j++) {
                buttons[i][j] = new SquareButton(new Square(File.values()[j], Rank.values()[7 - i]), this.game, buttons);
                buttons[i][j].setEnabled(false);
                buttons[i][j].setActionCommand(String.valueOf(c) + (8 - i));
                if ((i + j) % 2 == 0) {
                    buttons[i][j].setBackground(new Color(240, 217, 181));
                } else {
                    buttons[i][j].setBackground(new Color(181, 136, 99));
                }
                panel.add(buttons[i][j]);
                c++;
            }
            JLabel label = new JLabel(String.valueOf(8 - i));
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            panel.add(label);
        }
        for (int i = 0; i < 8; i++) {
            JLabel label = new JLabel(String.valueOf((char) ('A' + i)));
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            panel.add(label);
        }
        Squares = panel;

    }

    private class SquareButton extends JButton {
        Square square;
        SquareButton[][] buttons;
        ChessGame game;

        public SquareButton(Square square, ChessGame game, SquareButton[][] buttons) {
            this.square = square;
            this.buttons = buttons;
            this.game = game;
            this.addActionListener(e -> {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        this.buttons[i][j].setEnabled(this.buttons[i][j].getIcon() != null);
                        if ((i + j) % 2 == 0) {
                            this.buttons[i][j].setBackground(new Color(240, 217, 181));
                        } else {
                            this.buttons[i][j].setBackground(new Color(181, 136, 99));
                        }
                    }
                }
                game.getLegalMoves(square).forEach(move -> {
                    buttons[7 - move.rank().ordinal()][move.file().ordinal()].setEnabled(true);
                    buttons[7 - move.rank().ordinal()][move.file().ordinal()].setBackground(new Color(62, 178, 101));
                });

            });
        }


    }
}

