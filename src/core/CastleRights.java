package core;

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
