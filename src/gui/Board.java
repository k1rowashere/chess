package gui;

import core.square.Rank;
import core.square.Square;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

class Board {
    private final PieceLabel[][] board = new PieceLabel[8][8];

    Board(JPanel panel, Dimension cellDim, BiConsumer<PieceLabel, Square> onSelect) {
        for (var rank : Rank.values()) {
            for (var file : core.square.File.values()) {
                Square square = new Square(file, rank);
                var label = new PieceLabel(null, square, cellDim, onSelect);
                this.set(label, square);
                panel.add(label);
            }
        }
    }

    PieceLabel get(Square square) {
        return board[square.rank().ordinal()][square.file().ordinal()];
    }

    void forEach(BiConsumer<PieceLabel, Square> consumer) {
        for (var rank : Rank.values()) {
            for (var file : core.square.File.values()) {
                consumer.accept(get(new Square(file, rank)), new Square(file,
                        rank));
            }
        }
    }


    public void set(PieceLabel label, Square square) {
        board[square.rank().ordinal()][square.file().ordinal()] = label;
    }
}
