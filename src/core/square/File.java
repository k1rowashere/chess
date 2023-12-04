package core.square;

import java.util.Optional;

public enum File {
    A, B, C, D, E, F, G, H;

    public Optional<File> add(int i) {
        var file = this.ordinal() + i;
        if (file < 0 || file > 7) {
            return Optional.empty();
        }
        return Optional.of(File.values()[file]);
    }

    public int sub(File other) {
        return this.ordinal() - other.ordinal();
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
