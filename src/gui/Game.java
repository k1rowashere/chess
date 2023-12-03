package gui;

import core.ChessGame;
import core.Piece;
import core.PieceType;
import core.move.CastleType;
import core.move.GameStatus;
import core.move.Move;
import core.move.QualifiedMove;
import core.square.File;
import core.square.Rank;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Game extends JFrame {
    private static final int GRID_SIZE = 8;
    private static final int CELL_SIZE = 90;
    private static final int TOTAL_SIZE = GRID_SIZE * CELL_SIZE;
    // read from env
    private static final boolean FLIP_BOARD = true;
    private final Board board;
    private Square selectedSquare = null;

    public Game(@NotNull ChessGame game) {
        this.setTitle("Image Grid");
        this.setSize(TOTAL_SIZE, TOTAL_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var gridLayout = new GridLayout(GRID_SIZE, GRID_SIZE);
        var panel = new JPanel(gridLayout);

        gridLayout.preferredLayoutSize(this);

        this.board = new Board(panel,
                new Dimension(CELL_SIZE, CELL_SIZE),
                (label1, square) -> move(game, label1, square));

        // init board
        game.board().getPieces().forEach((boardPiece) -> {
            var label = board.get(boardPiece.square());
            label.setPiece(boardPiece.piece());
        });

        setContentPane(panel);
        flip();
    }

    private void move(ChessGame game, PieceLabel label, Square square) {
        board.forEach((l, s) -> l.reset());

        if (this.selectedSquare != null) {
            try {
                var doMove = new Move(this.selectedSquare, square);

                if (game.isPromotionMove(doMove)) {
                    var promotion = requestPromotion();
                    doMove = new Move(doMove, promotion);
                }

                this.move(game.move(doMove), game);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (Objects.equals(this.selectedSquare, square)) {
            this.selectedSquare = null;
            return;
        }

        this.selectedSquare = square;
        label.select();
        game.getLegalMoves(square).forEach(move -> board.get(move).legal());
    }

    private PieceType requestPromotion() {
        PieceType[] options = {
                PieceType.Knight,
                PieceType.Bishop,
                PieceType.Rook,
                PieceType.Queen
        };

        while (true) {
            int promotion = JOptionPane.showOptionDialog(
                    this,
                    "Choose a promotion",
                    "Promotion",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    PieceType.Queen
            );

            if (promotion == JOptionPane.CLOSED_OPTION)
                continue;

            return options[promotion];
        }
    }

    private void animateMove(@NotNull Move move, Piece piece) {
        var from = board.get(move.from());
        var to = board.get(move.to());

        JPanel contentPane = (JPanel) getContentPane();
        var glassPane = (JPanel) contentPane.getRootPane().getGlassPane();
        var tempLabel = new JLabel(from.getIcon());

        glassPane.setLayout(null);
        glassPane.setVisible(true);

        tempLabel.setSize(from.getSize());
        tempLabel.setLocation(from.getX(), from.getY());

        var comp = glassPane.add(tempLabel);

        var dy = (to.getY() - from.getY()) / 10.0;
        var dx = (to.getX() - from.getX()) / 10.0;

        from.setPiece(null);

        // ease in ease out
        var timer = new Timer(10, e -> {
            var x = (int) (tempLabel.getX() + dx);
            var y = (int) (tempLabel.getY() + dy);
            comp.setLocation(x, y);
            if (x == to.getX() && y == to.getY()) {
                ((Timer) e.getSource()).stop();
                glassPane.remove(comp);
                glassPane.setVisible(false);
                to.setPiece(piece);
            }
        });
        timer.start();
    }

    private void move(QualifiedMove move, ChessGame game) {
        playSound(move);
        var piece = move.piece();

        // en passant
        if (move.enPassant()) {
            var enPassantSquare = new Square(move.to().file(),
                    move.from().rank());
            board.get(enPassantSquare).setPiece(null);
        }

        // promotion
        if (move.promotion() != null) {
            piece = new Piece(move.promotion(), move.piece().color());
        }

        // castle
        if (move.castle() != CastleType.None) {
            var rookFile = move.castle() == CastleType.Short ? File.H : File.A;
            var rookRank = move.piece().color() == core.Color.White ? Rank._1 : Rank._8;
            var rookSquare = new Square(rookFile, rookRank);

            var newRookFile = move.castle() == CastleType.Short ? File.F : File.D;
            var newRookSquare = new Square(newRookFile, rookRank);

            Piece rook = new Piece(PieceType.Rook, move.piece().color());
            animateMove(new Move(rookSquare, newRookSquare), rook);
        }

        // check
        if (move.status() == GameStatus.Check) {
            var color = move.piece().color() == core.Color.White ?
                    core.Color.Black : core.Color.White;
            var king = game.board().getKing(color);
            board.get(king.square()).check();
        }

        animateMove(new Move(move.from(), move.to()), piece);

        // Sleep for 250ms to allow the animation to finish
        var t = new Thread(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignore) {
            }

            var msg = switch (move.status()) {
                case WhiteWins -> "White Wins";
                case BlackWins -> "Black Wins";
                case Stalemate -> "Stalemate";
                case Draw -> "Draw";
                case InsufficientMaterial -> "Insufficient Material";
                case null, default -> {
                    if (FLIP_BOARD)
                        flip();
                    yield null;
                }
            };
            if (msg != null)
                JOptionPane.showMessageDialog(this, msg);
        });
        t.start();
    }

    private static void playSound(QualifiedMove move) {
        switch (move.status()) {
            case Check -> Sounds.CHECK.play();
            case WhiteWins, BlackWins -> Sounds.CHECKMATE.play();
            case Stalemate, Draw, InsufficientMaterial -> Sounds.DRAW.play();
            case InProgress -> {
                if (move.promotion() != null) Sounds.PROMOTION.play();
                else if (move.castle() != CastleType.None) Sounds.CASTLE.play();
                else if (move.capture() != null) Sounds.CAPTURE.play();
                else Sounds.MOVE.play();
            }
        }
    }

    private void flip() {
        var panel = this.getContentPane();
        var components = panel.getComponents();
        panel.removeAll();

        for (int i = GRID_SIZE - 1; i >= 0; i--) {
            for (int j = 0; j < GRID_SIZE; j++) {
                panel.add(components[i * GRID_SIZE + j]);
            }
        }

        panel.revalidate();
        panel.repaint();
    }
}
