package core.square;


import java.util.Optional;

public enum Rank {
    One, Two, Three, Four, Five, Six, Seven, Eight;


    public Optional<Rank> add(int i) {
        var rank = this.ordinal() + i;
        if (rank < 0 || rank > 7) return Optional.empty();

        return Optional.of(Rank.values()[rank]);
    }

    public int sub(Rank other) {
        return this.ordinal() - other.ordinal();
    }

    public String toString() {
        return Integer.toString(this.ordinal() + 1);
    }
}