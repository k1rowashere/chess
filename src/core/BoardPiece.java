package core;

import core.square.File;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public record BoardPiece(Piece piece, Square square) {

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
