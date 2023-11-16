package core.square;

import java.util.Optional;

public record Square(File file, Rank rank) {
    public String toString() {
        return file.toString() + rank.toString();
    }

    public Square uncheckedAdd(int file, int rank) {
        return this.add(file, rank).orElseThrow();
    }

    public Optional<Square> add(int file, int rank) {
        file += this.file.ordinal();
        rank += this.rank.ordinal();

        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            return Optional.empty();
        }

        return Optional.of(new Square(File.values()[file], Rank.values()[rank]));
    }
}

