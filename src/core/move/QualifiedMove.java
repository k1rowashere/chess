package core.move;

import core.Piece;
import core.PieceType;
import core.square.Square;

public record QualifiedMove(
        GameStatus status,
        Piece piece,
        Square from,
        Square to,
        CastleType castle,
        boolean enPassant,
        PieceType capture,
        PieceType promotion) {
    // print the move in algebraic notation
}

