package core.move;

import core.PieceType;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Move(Square from, Square to, Optional<PieceType> promotion) {

    public Move(Square from, Square to) {
        this(from, to, Optional.empty());
    }

    public Move(Square from, Square to, PieceType promotion) {
        this(from, to, Optional.ofNullable(promotion));
    }

    public Move(@NotNull Move move, PieceType promotion) {
        this(move.from, move.to, Optional.ofNullable(promotion));
    }
}
