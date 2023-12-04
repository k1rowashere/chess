package core.move;

import core.Piece;
import core.PieceType;
import core.square.Square;

public class QualifiedMoveBuilder {
    private GameStatus status;
    private Piece piece;
    private Square from;
    private Square to;
    private CastleType castle;
    private boolean enPassant;
    private PieceType capture;
    private PieceType promotion;
    private boolean disambiguateRank;
    private boolean disambiguateFile;

    public QualifiedMoveBuilder() {
        this.status = GameStatus.InProgress;
        this.piece = null;
        this.from = null;
        this.to = null;
        this.castle = CastleType.None;
        this.enPassant = false;
        this.capture = null;
        this.promotion = null;
        this.disambiguateRank = false;
        this.disambiguateFile = false;
    }

    public QualifiedMoveBuilder status(GameStatus status) {
        this.status = status;
        return this;
    }

    public QualifiedMoveBuilder piece(Piece piece) {
        this.piece = piece;
        return this;
    }

    public QualifiedMoveBuilder source(Square from) {
        this.from = from;
        return this;
    }

    public QualifiedMoveBuilder target(Square to) {
        this.to = to;
        return this;
    }

    public QualifiedMoveBuilder castle(CastleType castle) {
        this.castle = castle;
        return this;
    }

    public QualifiedMoveBuilder enPassant(boolean enPassant) {
        this.enPassant = enPassant;
        return this;
    }

    public QualifiedMoveBuilder capture(PieceType capture) {
        this.capture = capture;
        return this;
    }

    public QualifiedMoveBuilder promotion(PieceType promotion) {
        this.promotion = promotion;
        return this;
    }

    public QualifiedMoveBuilder disambiguateRank() {
        this.disambiguateRank = true;
        return this;
    }


    public QualifiedMoveBuilder disambiguateFile() {
        this.disambiguateFile = true;
        return this;
    }

    public QualifiedMove build() {
        return new QualifiedMove(status, piece, from, to, castle, enPassant,
                capture, promotion, disambiguateRank, disambiguateFile);
    }
}
