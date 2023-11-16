package core;

import core.square.File;
import core.square.Rank;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Piece(PieceType type, Color color) {
    public int value() {
        return switch (this.type) {
            case Pawn -> 1;
            case Knight, Bishop -> 3;
            case Rook -> 5;
            case Queen -> 7;
            case King -> 200;
        };
    }

    public char unicodeSym() {
        switch (this.color) {
            case White -> {
                return switch (this.type) {
                    case Pawn -> '♙';
                    case Knight -> '♘';
                    case Bishop -> '♗';
                    case Rook -> '♖';
                    case Queen -> '♕';
                    case King -> '♔';
                };
            }
            case Black -> {
                return switch (this.type) {
                    case Pawn -> '♟';
                    case Knight -> '♞';
                    case Bishop -> '♝';
                    case Rook -> '♜';
                    case Queen -> '♛';
                    case King -> '♚';
                };
            }

        }
        return ' ';
    }

    /**
     * @param square          The square the piece is on
     * @param board           The board the piece is on
     * @param enPassantTarget The file of the en passant target square, or null if there is none
     * @param castleRights    The current castle rights
     * @return A list of all possible moves for the piece (not taking into
     * account check and pins/lines of sight).
     */
    ArrayList<Square> getMoves(Square square,
                               @NotNull final Board board,
                               File enPassantTarget,
                               CastleRights castleRights
    ) {
        return switch (this.type) {
            case Pawn -> Moves.pawn(square, this.color, enPassantTarget, board);
            case Knight -> Moves.knight(square);
            case Bishop -> Moves.bishop(square);
            case Rook -> Moves.rook(square);
            case Queen -> Stream.concat(
                            Moves.bishop(square).stream(),
                            Moves.rook(square).stream())
                    .collect(Collectors.toCollection(ArrayList::new));
            case King -> Moves.king(square, this.color, castleRights);
        };
    }

    private static class Moves {
        static ArrayList<Square> king(Square square,
                                      Color color,
                                      CastleRights castleRights
        ) {
            var pairs = new int[][]{{1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1},
                    {-1, 1}, {-1, 0}, {-1, -1}};

            var moves = movesFromPairs(square, pairs);

            var canCastleKingside = switch (color) {
                case White -> castleRights.whiteKingside();
                case Black -> castleRights.blackKingside();
            };
            var canCastleQueenside = switch (color) {
                case White -> castleRights.whiteQueenside();
                case Black -> castleRights.blackQueenside();
            };

            // Castling
            if (!square.equals(new Square(File.E, Rank.One))
                    && !square.equals(new Square(File.E, Rank.Eight))) {
                return moves;
            }
            switch (color) {
                case White -> {
                    if (canCastleKingside)
                        moves.add(new Square(File.G, Rank.One));
                    if (canCastleQueenside)
                        moves.add(new Square(File.C, Rank.One));
                }
                case Black -> {
                    if (canCastleKingside)
                        moves.add(new Square(File.G, Rank.Eight));
                    if (canCastleQueenside)
                        moves.add(new Square(File.C, Rank.Eight));
                }
            }

            return moves;
        }

        static ArrayList<Square> bishop(Square square) {
            var moves = new ArrayList<Square>();

            // Iterate over all possible directions (diagonals)
            // diagonal
            // anti-diagonal
            IntStream.rangeClosed(-7, 7)
                    .filter(i -> i != 0)
                    .forEach(i -> {
                        square.add(i, i).ifPresent(moves::add);
                        square.add(i, -i).ifPresent(moves::add);
                    });

            return moves;
        }

        static ArrayList<Square> rook(Square square) {
            var startRank = square.rank();
            var startFile = square.file();
            var moves = new ArrayList<Square>();

            Arrays.stream(Rank.values())
                    .map(r -> new Square(startFile, r))
                    .filter(s -> !s.equals(square))
                    .collect(Collectors.toCollection(() -> moves));

            Arrays.stream(File.values())
                    .map(f -> new Square(f, startRank))
                    .filter(s -> !s.equals(square))
                    .collect(Collectors.toCollection(() -> moves));

            return moves;
        }

        static ArrayList<Square> knight(Square square) {
            var pairs = new int[][]{{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2,
                    -1}, {-2, 1}, {-1, 2}};

            return movesFromPairs(square, pairs);
        }

        static ArrayList<Square> pawn(
                Square square,
                Color color,
                File enPassantTarget,
                Board board
        ) {
            var moves = new ArrayList<Square>();
            var direction = color == Color.White ? 1 : -1;

            // Single forward
            var inFront = square.add(0, direction);

            if (inFront.isEmpty()) return moves;

            if (board.getPiece(inFront.get()).isEmpty()) {
                inFront.ifPresent(moves::add);

                // Double forward
                boolean onStartRank = square.rank() == Rank.Two && color == Color.White
                        || square.rank() == Rank.Seven && color == Color.Black;

                if (onStartRank)
                    square.add(0, 2 * direction).ifPresent(moves::add);
            }

            var enPassantLeft = false;
            var enPassantRight = false;

            if (enPassantTarget != null
                    && (square.rank() == Rank.Five && color == Color.White
                    || square.rank() == Rank.Four && color == Color.Black)
            ) {
                enPassantLeft = enPassantTarget == square.file().add(-1).orElse(null);
                enPassantRight = enPassantTarget == square.file().add(1).orElse(null);
            }

            // Left and right captures
            Optional<Square> left = square.add(-1, direction);
            Optional<Square> right = square.add(1, direction);

            boolean canCaptureLeft = left
                    .map(s -> board.getPiece(s).isPresent())
                    .orElse(false);
            boolean canCaptureRight = right
                    .map(s -> board.getPiece(s).isPresent())
                    .orElse(false);

            if (canCaptureLeft || enPassantLeft) left.ifPresent(moves::add);
            if (canCaptureRight || enPassantRight) right.ifPresent(moves::add);

            return moves;
        }

        private static ArrayList<Square> movesFromPairs(Square start, int[][] pairs) {
            return Arrays.stream(pairs)
                    .map(pair -> start.add(pair[0], pair[1]))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
