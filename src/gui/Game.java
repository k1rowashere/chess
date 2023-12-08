package gui;

import core.ChessGame;
import core.Color;
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
    private static final boolean FLIP_BOARD = false;
    private final Board board;
    private final ChessGame game;
    private Square selectedSquare = null;
    private QualifiedMove lastMove = null;

    public Game(@NotNull ChessGame game) {
        this.game = game;
        this.setTitle("Kiro & Nor | Chess™");
        this.setSize(TOTAL_SIZE, TOTAL_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(new MenuBar(
                e -> onUndo(),
                e -> {
                    game.undo(1, Color.White);
                    this.refresh(game.board());
                }
        ));

        var gridLayout = new GridLayout(GRID_SIZE, GRID_SIZE);
        var panel = new JPanel(gridLayout);

        gridLayout.preferredLayoutSize(this);

        this.board = new Board(panel,
                new Dimension(CELL_SIZE, CELL_SIZE),
                this::onClick,
                this::selectSquare
        );

        // init board
        game.board().getPieces().forEach((boardPiece) -> {
            var label = board.get(boardPiece.square());
            label.setPiece(boardPiece.piece());
        });

        setContentPane(panel);
        flip();
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

    private void onClick(PieceLabel label, Square square, Boolean isDrag) {
        if (this.selectedSquare != null) {
            try {
                Move move = new Move(this.selectedSquare, square);

                if (game.isPromotionMove(move)) {
                    var promotion = requestPromotion();
                    move = new Move(move, promotion);
                }

                this.move(this.game.move(move), !isDrag);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (Objects.equals(this.selectedSquare, square)) {
            board.forEach((l, s) -> l.deselect());
            this.selectedSquare = null;
            return;
        }

        selectSquare(label, square);
    }

    private void selectSquare(PieceLabel label, Square square) {
        board.forEach((l, s) -> l.deselect());
        this.selectedSquare = square;
        label.select();
        game.getLegalMoves(square).forEach(move -> board.get(move).legal());
    }

    private void onUndo() {
        if (lastMove == null) return;
        var move = game.undo();

        animateMove(new Move(lastMove.to(), lastMove.from()), lastMove.piece());
        playSound(lastMove);

        lastMove = move.orElse(null);

        new Timer(300, e -> {
            if (FLIP_BOARD)
                flip();
            this.refresh(game.board());
            move.ifPresent(m -> {
                board.get(m.from()).lastMove();
                board.get(m.to()).lastMove();
            });
            ((Timer) e.getSource()).stop();
        }).start();
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

    private void move(QualifiedMove move, boolean animate) {
        this.lastMove = move;
        board.forEach((label, square) -> label.reset());

        playSound(move);
        var piece = move.piece();
        System.out.println(move.partialAlgebraicNotation());

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

        if (animate)
            animateMove(new Move(move.from(), move.to()), piece);
        else {
            board.get(move.from()).setPiece(null);
            board.get(move.to()).setPiece(piece);
        }

        new Timer(250, e -> {
            board.get(move.from()).lastMove();
            board.get(move.to()).lastMove();

            var msg = switch (move.status()) {
                case WhiteWins -> "White Wins";
                case BlackWins -> "Black Wins";
                case Stalemate -> "Stalemate";
                case Draw -> "Draw";
                case InsufficientMaterial -> "Insufficient Material";
                case null, default -> {
                    if (FLIP_BOARD) {
                        flip();
                    }
                    yield null;
                }
            };
            if (msg != null)
                JOptionPane.showMessageDialog(this, msg);

            ((Timer) e.getSource()).stop();
        }).start();
    }

    private void animateMove(@NotNull Move move, Piece piece) {
        final int STEPS = 20;
        final int DURATION = 100;

        var from = board.get(move.from());
        var to = board.get(move.to());

        // account for the offset of the board
        var window = this.getLocationOnScreen();
        var board = this.getContentPane().getLocationOnScreen();
        var offset = new Point(board.x - window.x, board.y - window.y);

        Point src = from.getLocation();
        src.translate(offset.x, offset.y);

        Point dest = to.getLocation();
        dest.translate(offset.x, offset.y);

        var glassPane = (JPanel) this.getRootPane().getGlassPane();
        glassPane.setLayout(null);
        glassPane.setVisible(true);

        var tempLabel = new JLabel(from.getIcon());
        tempLabel.setSize(from.getSize());
        tempLabel.setLocation(src);

        var comp = glassPane.add(tempLabel);

        var dx = (to.getX() - from.getX()) / STEPS;
        var dy = (to.getY() - from.getY()) / STEPS;
        var step = Math.sqrt(dx * dx + dy * dy);

        from.setPiece(null);
        new Timer(DURATION / STEPS, e -> {
            var newLoc = comp.getLocation();
            newLoc.translate(dx, dy);
            comp.setLocation(newLoc);

            // check if the piece has reached its destination (or passed it)
            if (newLoc.distance(dest) <= step) {
                ((Timer) e.getSource()).stop();
                glassPane.remove(comp);
//                glassPane.setVisible(false);
                to.setPiece(piece);
            }
        }).start();
    }

    public void refresh(core.Board board) {
        this.board.forEach((label, square) -> {
            label.setPiece(null);
            label.reset();
        });

        board.getPieces().forEach((boardPiece) -> {
            var label = this.board.get(boardPiece.square());
            label.setPiece(boardPiece.piece());
        });
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
