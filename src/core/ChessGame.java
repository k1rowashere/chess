package core;

import core.move.*;
import core.square.File;
import core.square.Rank;
import core.square.Square;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

record CastleRights(boolean whiteKingside, boolean whiteQueenside,
                    boolean blackKingside, boolean blackQueenside) {
    CastleRights() {
        this(true, true, true, true);
    }

    CastleRights disableKingside(Color color) {
        return switch (color) {
            case White ->
                    new CastleRights(false, this.whiteQueenside, this.blackKingside, this.blackQueenside);
            case Black ->
                    new CastleRights(this.whiteKingside, this.whiteQueenside, false, this.blackQueenside);
        };
    }

    CastleRights disableQueenside(Color color) {
        return switch (color) {
            case White ->
                    new CastleRights(this.whiteKingside, false, this.blackKingside, this.blackQueenside);
            case Black ->
                    new CastleRights(this.whiteKingside, this.whiteQueenside, this.blackKingside, false);
        };
    }

    CastleRights disableBoth(Color color) {
        return switch (color) {
            case White ->
                    new CastleRights(false, false, this.blackKingside, this.blackQueenside);
            case Black ->
                    new CastleRights(this.whiteKingside, this.whiteQueenside, false, false);
        };
    }
}

public class ChessGame {
    private Color toMove;
    private CastleRights castleRights;
    private File enPassantTarget;
    private final Board board;
    // TODO: implement moves, threefold repetition,
    //    private ArrayList<Move> moves;
    //    private ArrayList<String> threeFoldRepetitionStates;
    private int fiftyMoveRuleStates = 50;


    public ChessGame() {
        this.board = new Board();
        this.toMove = Color.White;
        this.castleRights = new CastleRights();
        this.enPassantTarget = null;
    }

    public QualifiedMove move(Move move) throws IllegalArgumentException {
        /* List of steps:
         * 1. Validate move
         * 2. Move piece
         * 4. Promotion
         * 5. En passant
         * 6. Castle
         * 7. fifty move rule
         * 8. Check for checkmate, stalemate, draw
         */

        if (!isLegalMove(move))
            throw new IllegalArgumentException("Illegal move");

        Square source = move.from();
        Square target = move.to();

        var piece = this.board.getPiece(source).orElseThrow();
        var capturedPiece = this.board.getPiece(target);

        var retMove = new QualifiedMoveBuilder();

        retMove.piece(piece.piece())
                .source(source)
                .target(target)
                .capture(capturedPiece.map(BoardPiece::type).orElse(null));

        this.board.move(move);

        // Promotion & En passant
        if (piece.type() == PieceType.Pawn) {
            if (target.rank() == Rank.Eight || target.rank() == Rank.One) {
                this.board.setPiece(
                        target,
                        new Piece(move.promotion()
                                .orElseThrow(() -> new IllegalArgumentException("Missing promotion")),
                                piece.color()
                        )
                );

                retMove.promotion(move.promotion().orElse(null));
            }

            // Remove captured pawn
            if (target.file() != source.file() && capturedPiece.isEmpty()) {
                // TODO: use a capture method (to keep track of captured pieces)
                // the fifty move rule is reset anyway because of the pawn move
                this.board.removePiece(new Square(source.file(), target.rank()));
                retMove.enPassant(true)
                        .capture(PieceType.Pawn);
            }

            // En passant target to check for en passant next turn
            switch (target.rank().sub(source.rank())) {
                case 2, -2 -> this.enPassantTarget = target.file();
                default -> this.enPassantTarget = null;
            }
        }

        // Castle
        var isCastleMove = piece.type() == PieceType.King
                && Math.abs(source.file().sub(target.file())) == 2;

        // Castle (move the rook)
        if (isCastleMove) {
            Rank rank = target.rank();
            var rookFrom = switch (target.file()) {
                case C -> new Square(File.A, rank);
                case G -> new Square(File.H, rank);
                default -> throw new RuntimeException("Invalid castle");
            };
            var rookTo = switch (target.file()) {
                case C -> new Square(File.D, rank);
                case G -> new Square(File.F, rank);
                default -> throw new RuntimeException("Invalid castle");
            };
            this.board.move(new Move(rookFrom, rookTo));

            retMove.castle(switch (target.file()) {
                case C -> CastleType.Long;
                case G -> CastleType.Short;
                default -> throw new RuntimeException("Invalid castle");
            });
        }

        // Update Castling Rights
        if (piece.type() == PieceType.King) {
            this.castleRights = this.castleRights.disableBoth(piece.color());
        }
        if (piece.type() == PieceType.Rook) {
            var cr = this.castleRights;
            if (piece.square().equals(new Square(File.A, Rank.One)))
                this.castleRights = cr.disableQueenside(Color.White);
            else if (piece.square().equals(new Square(File.H, Rank.One)))
                this.castleRights = cr.disableKingside(Color.White);
            else if (piece.square().equals(new Square(File.A, Rank.Eight)))
                this.castleRights = cr.disableQueenside(Color.Black);
            else if (piece.square().equals(new Square(File.H, Rank.Eight)))
                this.castleRights = cr.disableKingside(Color.Black);
        }


        // reset fifty move rule
        if (capturedPiece.isPresent() || piece.type() == PieceType.Pawn) {
            this.fiftyMoveRuleStates = 0;
        } else this.fiftyMoveRuleStates++;

        // Update turn
        this.toMove = this.toMove == Color.White ? Color.Black : Color.White;

        // game over conditions
        retMove.status(gameStateCheck());

        return retMove.build();
    }

    public boolean isLegalMove(@NotNull Move move) {
        return getLegalMoves(move.from()).contains(move.to());
    }

    public ArrayList<Square> getLegalMoves(Square square) {
        return board.getPiece(square)
                .map(this::getLegalMoves)
                .orElse(new ArrayList<>());
    }

    private GameStatus gameStateCheck() {
        Function<Color, Boolean> isInsufficientMaterial = (color) -> {
            var material = this.board.getPieces().stream()
                    .filter(p -> p.color() == color)
                    .map(BoardPiece::type)
                    .toList();

            return switch (material.size()) {
                case 1 -> true;
                case 2 -> material.contains(PieceType.Bishop)
                        || material.contains(PieceType.Knight);
                default -> false;
            };
        };

        var isCheck = isCheck();
        var noLegalMoves = this.board.getPieces()
                .stream()
                .filter(p -> p.color() == this.toMove)
                .map(this::getLegalMoves)
                .mapToInt(ArrayList::size)
                .sum() == 0;

        // TODO: implement threefold repetition
        var threeFoldRepetition = false;
        var draw = (isInsufficientMaterial.apply(Color.Black)
                && isInsufficientMaterial.apply(Color.White))
                || fiftyMoveRuleStates == 50
                || threeFoldRepetition;


        if (noLegalMoves) {
            return isCheck ? switch (this.toMove) {
                case White -> GameStatus.BlackWins;
                case Black -> GameStatus.WhiteWins;
            } : GameStatus.Draw;
        } else if (draw) {
            return GameStatus.Draw;
        } else if (isCheck) {
            return GameStatus.Check;
        } else {
            return GameStatus.InProgress;
        }

    }

    /**
     * @param piece
     * @return
     */
    private ArrayList<Square> getLegalMoves(@NotNull BoardPiece piece) {
        if (piece.color() != this.toMove) return new ArrayList<>();

        return piece
                .getMoves(this.board, this.enPassantTarget, getCastleRights())
                .stream()
                .filter(target -> board.getPiece(target)
                        .map(p -> piece.color() != p.color())
                        .orElse(true))
                .filter(target -> lineOfSight(piece, target))
                .filter(target -> !isCheck(new Move(piece.square(), target)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private @NotNull CastleRights getCastleRights() {
        var king = this.board.getKing(toMove);
        var square = king.square();

        var canCastle = switch (king.color()) {
            case White -> square.equals(new Square(File.E, Rank.One));
            case Black -> square.equals(new Square(File.E, Rank.Eight));
        } && !isCheck();

        var kingSideCheck = isCheck(new Move(square, square.uncheckedAdd(1, 0)));
        var queenSideCheck = isCheck(new Move(square, square.uncheckedAdd(-1, 0)))
                || isCheck(new Move(square, square.uncheckedAdd(-2, 0)));

        return canCastle ? new CastleRights(
                !kingSideCheck && this.castleRights.whiteKingside(),
                !queenSideCheck && this.castleRights.whiteQueenside(),
                !kingSideCheck && this.castleRights.blackKingside(),
                !queenSideCheck && this.castleRights.blackQueenside()
        ) : new CastleRights(false, false, false, false);
    }

    public boolean isCheck() {
        return isCheck(null);
    }

    /**
     * @param simulateMove Optional Move to apply before checking.
     *                     (The move is not validated)
     * @return If the king is in check after applying simulateMove.
     * @throws java.util.NoSuchElementException If there is no king on the
     *                                          board.
     */
    private boolean isCheck(@Nullable Move simulateMove) {
        var board = this.board.copy();


        if (simulateMove != null) {
            board.move(simulateMove);
        }

        var king = board.getKing(this.toMove);
        return board.getPieces()
                .stream()
                .filter(piece -> piece.color() != this.toMove)
                .filter(p -> lineOfSight(p, king.square()))
                .flatMap(p -> p.getMoves(board, this.enPassantTarget, this.castleRights).stream())
                .anyMatch(s -> s.equals(king.square()));
    }


    /**
     * @param piece  the piece to check
     * @param target the target square to check
     * @return if there are no pieces between piece and target
     * @implNote this method assumes that piece and target are on the same
     * rank, file, or diagonal.
     * <p>
     * It does not check if piece can actually move to the target, but that
     * it can potentially "see" it.
     * use in conjunction with {@link BoardPiece#getMoves}
     * to check if a piece can move to a square
     */
    private boolean lineOfSight(@NotNull BoardPiece piece, Square target) {
        var source = piece.square();

        // pieces can't move to their own square
        if (source == target) return false;

        // knights can jump over pieces
        if (piece.type() == PieceType.Knight) return true;

        var fileDir = Integer.signum(target.file().sub(source.file()));
        var rankDir = Integer.signum(target.rank().sub(source.rank()));

        while (true) {
            var tryAdd = source.add(fileDir, rankDir);
            if (tryAdd.isEmpty()) return false;
            source = tryAdd.get();

            if (source.equals(target)) return true;

            if (this.board.getPiece(source).isPresent())
                return false;
        }
    }
}
