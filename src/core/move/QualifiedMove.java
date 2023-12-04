package core.move;

import core.Color;
import core.Piece;
import core.PieceType;
import core.square.Square;

import java.util.Objects;

public record QualifiedMove(
        GameStatus status,
        Piece piece,
        Square from,
        Square to,
        CastleType castle,
        boolean enPassant,
        PieceType capture,
        PieceType promotion,
        boolean disambiguateRank,
        boolean disambiguateFile
) {
    public String partialAlgebraicNotation() {
        StringBuilder res = new StringBuilder();

        String piece;
        if (this.piece().type() == PieceType.Pawn) {
            piece = "";
        } else {
            piece = new Piece(this.piece().type(), Color.Black).unicodeSym() + "";
        }

        var postfix = switch (this.status()) {
            case WhiteWins, BlackWins -> "#";
            case Check -> "+";
            default -> "";
        };


        switch (this.castle()) {
            case Short -> res.append("O-O");
            case Long -> res.append("O-O-O");
            default -> {
                res.append(piece);
                if (this.disambiguateFile()
                        || this.capture() != null && this.piece().type() == PieceType.Pawn
                ) {
                    res.append(this.from().file());
                }
                if (this.disambiguateRank()) {
                    res.append(this.from().rank());
                }
                res.append(this.capture() != null ? "x" : "");
                res.append(this.to());
                if (this.promotion() != null)
                    res.append(new Piece(this.promotion(), Color.Black).unicodeSym());
            }
        }
        res.append(postfix);

        return res.toString();
    }

//    public static String fullAlgebraicNotation(QualifiedMove white,
//                                               QualifiedMove black,
//                                               int moveNumber) {
//        var res = new StringBuilder();
//        res.append(moveNumber).append(". ");
//        res.append(white.partialAlgebraicNotation()).append(" ");
//        res.append(black.partialAlgebraicNotation()).append(" ");
//        return res.toString();
//    }

}

