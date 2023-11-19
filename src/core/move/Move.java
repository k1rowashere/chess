package core.move;

import core.PieceType;
import core.square.Square;

import java.util.Optional;

public record Move(Square from, Square to, Optional<PieceType> promotion) {

    public Move(Square from, Square to) {
        this(from, to, Optional.empty());
    }

    public Move(Square from, Square to, PieceType promotion) {
        this(from, to, Optional.ofNullable(promotion));
    }
}
