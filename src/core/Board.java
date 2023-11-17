package core;

import core.move.Move;
import core.square.File;
import core.square.Rank;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Board {
    private final Piece[][] board;


    Board() {
        this.board = new Piece[8][8];
    }

    Board(Piece[][] pieces) {
        this.board = pieces;
    }

    static @NotNull Board defaultBoard() {
        var wp = new Piece(PieceType.Pawn, Color.White);
        var wn = new Piece(PieceType.Knight, Color.White);
        var wb = new Piece(PieceType.Bishop, Color.White);
        var wr = new Piece(PieceType.Rook, Color.White);
        var wq = new Piece(PieceType.Queen, Color.White);
        var wk = new Piece(PieceType.King, Color.White);
        var bp = new Piece(PieceType.Pawn, Color.Black);
        var bn = new Piece(PieceType.Knight, Color.Black);
        var bb = new Piece(PieceType.Bishop, Color.Black);
        var br = new Piece(PieceType.Rook, Color.Black);
        var bq = new Piece(PieceType.Queen, Color.Black);
        var bk = new Piece(PieceType.King, Color.Black);
        Piece xx = null;

        var board = new Piece[][]{
                {wr, wn, wb, wq, wk, wb, wn, wr},
                {wp, wp, wp, wp, wp, wp, wp, wp},
                {xx, xx, xx, xx, xx, xx, xx, xx},
                {xx, xx, xx, xx, xx, xx, xx, xx},
                {xx, xx, xx, xx, xx, xx, xx, xx},
                {xx, xx, xx, xx, xx, xx, xx, xx},
                {bp, bp, bp, bp, bp, bp, bp, bp},
                {br, bn, bb, bq, bk, bb, bn, br}
        };

        return new Board(board);
    }

    /**
     * @return A hex hash of the board
     */
    public String toHash() {
        // convert board to bitmap of 64 bits for each piece, 6 pieces per color
        int[] bitmaps = new int[12];
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Piece piece = this.board[rank][file];
                if (piece != null) {
                    int index = piece.color() == Color.White ? 0 : 6;
                    index += switch (piece.type()) {
                        case Pawn -> 0;
                        case Knight -> 1;
                        case Bishop -> 2;
                        case Rook -> 3;
                        case Queen -> 4;
                        case King -> 5;
                    };
                    bitmaps[index] |= 1 << (rank * 8 + file);
                }
            }
        }

        return Arrays.stream(bitmaps)
                .mapToObj(Integer::toHexString)
                .collect(Collectors.joining());
    }

    public BoardPiece getKing(Color color) {
        return getPieces().stream()
                .filter(piece -> piece.piece().type() == PieceType.King)
                .filter(piece -> piece.color() == color)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No king found"));
    }

    public Optional<BoardPiece> getPiece(@NotNull Square square) {
        int row = square.rank().ordinal();
        int col = square.file().ordinal();

        return Optional.ofNullable(this.board[row][col])
                .map(piece -> new BoardPiece(piece, square));
    }

    /**
     * @return all the pieces on the board
     */
    public @NotNull ArrayList<BoardPiece> getPieces() {
        var pieces = new ArrayList<BoardPiece>();
        for (var rank : Rank.values()) {
            for (var file : File.values()) {
                getPiece(new Square(file, rank))
                        .ifPresent(pieces::add);
            }
        }
        return pieces;
    }

    /**
     * @param move Unchecked move to make:
     *             the move doesn't have to be legal
     */
    void move(@NotNull Move move) {
        int toRow = move.to().rank().ordinal();
        int toCol = move.to().file().ordinal();
        int fromRow = move.from().rank().ordinal();
        int fromCol = move.from().file().ordinal();

        this.board[toRow][toCol] = this.board[fromRow][fromCol];
        this.board[fromRow][fromCol] = null;
    }

    public @NotNull Board copy() {
        var board = new Board();
        for (int rank = 0; rank < 8; rank++) {
            System.arraycopy(this.board[rank], 0, board.board[rank], 0, 8);
        }
        return board;
    }

    public void setPiece(@NotNull Square square, Piece boardPiece) {
        int row = square.rank().ordinal();
        int col = square.file().ordinal();

        this.board[row][col] = boardPiece;
    }

    public void removePiece(@NotNull Square square) {
        int row = square.rank().ordinal();
        int col = square.file().ordinal();

        this.board[row][col] = null;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                if (this.board[rank][file] != null) {
                    builder.append(this.board[rank][file].unicodeSym());
                } else {
                    builder.append('□');
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
