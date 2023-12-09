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
import java.awt.event.ActionEvent;
import java.util.Objects;

interface Callback {
    void run();
}

public class Game extends JFrame {
    private static final int GRID_SIZE = 8;
    private static final int CELL_SIZE = 90;
    private static final int BOARD_SIZE = GRID_SIZE * CELL_SIZE;
    private static final boolean FLIP_BOARD = false;
    private final ChessGame game;
    private final JPanel boardPanel;
    private final MoveHistory moveHistory;
    private final Board board;
    private boolean locked = false;
    private Square selectedSquare = null;
    private QualifiedMove lastMove = null;

    public Game(@NotNull ChessGame game) {
        this.game = game;
        this.setTitle("Kiro & Nor | Chess™");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(new MenuBar(this::onUndo, this::onNewGame));
        this.boardPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE));
        boardPanel.setSize(new Dimension(BOARD_SIZE, BOARD_SIZE));

        var mainPanel = new JPanel(new BorderLayout());

        this.board = new Board(boardPanel,
                new Dimension(CELL_SIZE, CELL_SIZE),
                this::onPieceClick,
                this::selectSquare
        );

        // init board
        game.board()
                .getPieces()
                .forEach((boardPiece) -> {
                    var label = board.get(boardPiece.square());
                    label.setPiece(boardPiece.piece());
                });

        this.moveHistory = new MoveHistory(
                new Dimension(200, BOARD_SIZE),
                this::onHistoryClick
        );


        mainPanel.add(boardPanel);
        mainPanel.add(moveHistory, BorderLayout.EAST);

        setContentPane(mainPanel);
        pack();
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

    private boolean move(Move tryMove, boolean animate) {
        if (locked) return false;
        if (game.isPromotionMove(tryMove)) {
            var promotion = requestPromotion();
            tryMove = new Move(tryMove, promotion);
        }

        QualifiedMove move;
        try {
            move = this.game.move(tryMove);
        } catch (IllegalArgumentException e) {
            return false;
        }

        this.lastMove = move;
        this.moveHistory.addMove(move);
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

        if (animate) {
            animateMove(
                    new Move(move.from(), move.to()),
                    piece,
                    () -> {
                        // wait for animation to finish
                        if (FLIP_BOARD) flip();
                    }
            );
        } else {
            board.get(move.from()).setPiece(null);
            board.get(move.to()).setPiece(piece);
            if (FLIP_BOARD) flip();
        }

        board.get(move.from()).lastMove();
        board.get(move.to()).lastMove();

        // check for game end
        var msg = switch (move.status()) {
            case WhiteWins -> "White Wins";
            case BlackWins -> "Black Wins";
            case Stalemate -> "Stalemate";
            case Draw -> "Draw";
            case InsufficientMaterial -> "Insufficient Material";
            case null, default -> null;
        };
        if (msg != null)
            JOptionPane.showMessageDialog(this, msg);
        return true;
    }

    /**
     * @param move  The move to animate
     * @param piece The piece to animate
     */
    private void animateMove(@NotNull Move move, Piece piece) {
        animateMove(move, piece, null);
    }

    /**
     * @param move     The move to animate
     * @param piece    The piece to animate
     * @param callback A callback to run after the animation is complete
     */
    private void animateMove(@NotNull Move move, Piece piece,
                             Callback callback) {
        final int STEPS = 30;
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
                glassPane.setVisible(false);
                to.setPiece(piece);
                if (callback != null) callback.run();
            }
        }).start();
    }

    private void selectSquare(PieceLabel label, Square square) {
        board.forEach((l, s) -> l.deselect());
        this.selectedSquare = square;
        label.select();
        if (!this.locked) {
            game.getLegalMoves(square).forEach(move -> board.get(move).legal());
        }
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

    public void updateWith(core.Board board, QualifiedMove lastMove) {
        this.board.forEach((label, square) -> {
            label.setPiece(null);
            label.reset();
        });

        board.getPieces().forEach((boardPiece) -> {
            var label = this.board.get(boardPiece.square());
            label.setPiece(boardPiece.piece());
        });

        if (lastMove != null) {
            this.board.get(lastMove.from()).lastMove();
            this.board.get(lastMove.to()).lastMove();
            if (lastMove.status() == GameStatus.Check) {
                var color = lastMove.piece().color() == Color.White ?
                        Color.Black : Color.White;
                var king = game.board().getKing(color);
                this.board.get(king.square()).check();
            }
            if (lastMove.color() != this.lastMove.color() && FLIP_BOARD) {
                flip();
            }
        } else if (this.lastMove.color() == Color.White && FLIP_BOARD) {
            // first move
            flip();
        }

        this.lastMove = lastMove;
    }

    private void flip() {
        var panel = this.boardPanel;
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

    private void onPieceClick(PieceLabel label, Square square, Boolean isDrag) {
        if (this.selectedSquare != null) {
            var succeeded = move(new Move(this.selectedSquare, square), !isDrag);
            if (succeeded) return;
        }

        if (Objects.equals(this.selectedSquare, square)) {
            board.forEach((l, s) -> l.deselect());
            this.selectedSquare = null;
            return;
        }

        selectSquare(label, square);
    }

    private void onUndo(ActionEvent e) {
        if (this.lastMove == null || this.locked) return;
        var move = this.game.undo();


        if (move.isPresent() && move.get().status() == GameStatus.Check)
            Sounds.CHECK.play();
        else playSound(this.lastMove);

        this.moveHistory.removeLastMove();

        animateMove(
                new Move(lastMove.to(), lastMove.from()),
                lastMove.piece(),
                () -> this.updateWith(this.game.board(), move.orElse(null))
        );
    }

    private void onNewGame(ActionEvent e) {
        this.game.undo(1, Color.White);
        this.updateWith(this.game.board(), null);
        this.moveHistory.clear();
        this.locked = false;
    }

    private void onHistoryClick(Integer moveIdx) {
        var board = game.peekBoard(moveIdx);
        this.locked = game.moveCount() != moveIdx;
        this.updateWith(board, game.peekMove(moveIdx));
    }
}
