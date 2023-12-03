package gui;

import core.square.Rank;
import core.square.Square;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

class Board {
    private final Game.PieceLabel[][] board = new Game.PieceLabel[8][8];

    Board(BiConsumer<Game.PieceLabel, Square> onSelect, JPanel panel) {
        for (var rank : Rank.values()) {
            for (var file : core.square.File.values()) {
                var label = new Game.PieceLabel(
                        null,
                        new Square(file, rank),
                        onSelect);

                var i = rank.ordinal();
                var j = file.ordinal();
                label.setBackground(new Color((i + j) % 2 == 0 ? 0xb58863 :
                        0xf0d9b5));

                this.set(label, new Square(file, rank));
                panel.add(label);
            }
        }
    }

    Game.PieceLabel get(Square square) {
        return board[square.rank().ordinal()][square.file().ordinal()];
    }

    void forEach(BiConsumer<Game.PieceLabel, Square> consumer) {
        for (var rank : Rank.values()) {
            for (var file : core.square.File.values()) {
                consumer.accept(get(new Square(file, rank)), new Square(file,
                        rank));
            }
        }
    }


    public void set(Game.PieceLabel label, Square square) {
        board[square.rank().ordinal()][square.file().ordinal()] = label;
    }
}
