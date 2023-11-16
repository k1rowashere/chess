package core;

import core.square.File;
import core.square.Move;
import core.square.Rank;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

record BoardPiece(Piece piece, Square square) {

    ArrayList<Square> getMoves(
            @NotNull final Board board,
            File enPassantTarget,
            CastleRights castleRights
    ) {
        return this.piece.getMoves(this.square, board, enPassantTarget, castleRights);
    }

    public PieceType type() {
        return this.piece.type();
    }

    public Color color() {
        return this.piece.color();
    }
}

public final class Board {
    private final Piece[][] board;

    Board() {
        var wPawn = new Piece(PieceType.Pawn, Color.White);
        var bPawn = new Piece(PieceType.Pawn, Color.Black);
        var wKnight = new Piece(PieceType.Knight, Color.White);
        var bKnight = new Piece(PieceType.Knight, Color.Black);
        var wBishop = new Piece(PieceType.Bishop, Color.White);
        var bBishop = new Piece(PieceType.Bishop, Color.Black);
        var wRook = new Piece(PieceType.Rook, Color.White);
        var bRook = new Piece(PieceType.Rook, Color.Black);
        var wQueen = new Piece(PieceType.Queen, Color.White);
        var bQueen = new Piece(PieceType.Queen, Color.Black);
        var wKing = new Piece(PieceType.King, Color.White);
        var bKing = new Piece(PieceType.King, Color.Black);


        this.board = new Piece[][]{
                {wRook, wKnight, wBishop, wQueen, wKing, wBishop, wKnight, wRook},
                {wPawn, wPawn, wPawn, wPawn, wPawn, wPawn, wPawn, wPawn},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {bPawn, bPawn, bPawn, bPawn, bPawn, bPawn, bPawn, bPawn},
                {bRook, bKnight, bBishop, bQueen, bKing, bBishop, bKnight, bRook}
        };

    }

    Board(boolean empty) {
        this.board = new Piece[8][8];
    }


    BoardPiece getKing(Color color) {
        return getPieces().stream()
                .filter(piece -> piece.piece().type() == PieceType.King)
                .filter(piece -> piece.color() == color)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No king found"));
    }

    Optional<BoardPiece> getPiece(Square square) {
        int col = square.file().ordinal();
        int row = square.rank().ordinal();

        return Optional.ofNullable(this.board[row][col])
                .map(piece -> new BoardPiece(piece, square));
    }

    ArrayList<BoardPiece> getPieces() {
        var pieces = new ArrayList<BoardPiece>();
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Piece piece = this.board[rank][file];
                if (piece != null) {
                    pieces.add(new BoardPiece(piece, new Square(File.values()[file], Rank.values()[rank])));
                }
            }
        }
        return pieces;
    }

    // unchecked move
    public void move(Move move) {
        int toRow = move.to().rank().ordinal();
        int toCol = move.to().file().ordinal();
        int fromRow = move.from().rank().ordinal();
        int fromCol = move.from().file().ordinal();

        this.board[toRow][toCol] = this.board[fromRow][fromCol];
        this.board[fromRow][fromCol] = null;
    }

    public @NotNull Board copy() {
        var board = new Board(true);
        for (int rank = 0; rank < 8; rank++) {
            System.arraycopy(this.board[rank], 0, board.board[rank], 0, 8);
        }
        return board;
    }

    public void setPiece(@NotNull Square square, Piece boardPiece) {
        int row = square.file().ordinal();
        int col = square.rank().ordinal();

        this.board[row][col] = boardPiece;
    }

    public void removePiece(@NotNull Square square) {
        int row = square.file().ordinal();
        int col = square.rank().ordinal();

        this.board[row][col] = null;
    }

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
