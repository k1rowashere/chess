package core.square;

import core.PieceType;

import java.util.Optional;

public record Move(Square from, Square to,
                   boolean capture,
                   boolean check,
                   boolean castle,
                   Optional<PieceType> promotion
) {

    public Move(Square from, Square to) {
        this(from, to, false, false, false, Optional.empty());
    }
}
