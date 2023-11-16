import core.ChessGame;
import core.PieceType;
import core.move.CastleType;
import core.move.Move;
import core.move.QualifiedMove;
import core.square.File;
import core.square.Square;

import java.io.FileWriter;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        ChessGame game = new ChessGame();

        Scanner scanner = new Scanner(new java.io.File("ChessGame.txt"));
        FileWriter writer = new FileWriter("Output.txt");
        boolean gameEnded = false;
        while (scanner.hasNextLine()) {
            String[] line;
            line = scanner.nextLine().split(",");
            Square square1 = parseSquare(line[0]);
            Square square2 = parseSquare(line[1]);
            PieceType promotion = null;
            if (gameEnded) {
                writer.write("Game has ended\n");
                continue;
            }
            if (line.length == 3) {
                promotion = switch (line[2]) {
                    case "Queen" -> PieceType.Queen;
                    case "Rook" -> PieceType.Rook;
                    case "Bishop" -> PieceType.Bishop;
                    case "Knight" -> PieceType.Knight;
                    default -> null;
                };
            }
            QualifiedMove move;
            try {
                move = game.move(new Move(square1, square2, Optional.ofNullable(promotion)));
            } catch (Exception e) {
                writer.write("Invalid Move\n");
                continue;
            }
            if ((move.castle() != CastleType.None)) {
                writer.write(move.castle().toString() + "\n");
            }
            if (move.enPassant()) {
                writer.write("En Passant\n");
            }
            if (move.capture() != null) {

                writer.write("Captured " + move.capture().toString() + "\n");
            }
            switch (move.status()) {
                case Check -> {
                    switch (move.piece().color()) {
                        case White -> writer.write("Black in check\n");
                        case Black -> writer.write("White in check\n");
                    }
                }
                case WhiteWins -> writer.write("White Won\n");
                case BlackWins -> writer.write("Black Won\n");
                case Stalemate -> writer.write("Stalemate\n");
                case Draw -> writer.write("Draw\n");
                case InsufficientMaterial -> writer.write("Insufficient Material\n");
            }
            switch (move.status()) {
                case WhiteWins, BlackWins, Stalemate, Draw, InsufficientMaterial -> gameEnded = true;
            }
        }
        writer.close();
    }


    private static Square parseSquare(String line) {
        var file = switch (line.charAt(0)) {
            case 'a' -> File.A;
            case 'b' -> File.B;
            case 'c' -> File.C;
            case 'd' -> File.D;
            case 'e' -> File.E;
            case 'f' -> File.F;
            case 'g' -> File.G;
            case 'h' -> File.H;
            default -> throw new IllegalStateException("Unexpected value: " + line.charAt(0));
        };
        var Rank = switch (line.charAt(1)) {
            case '1' -> core.square.Rank.One;
            case '2' -> core.square.Rank.Two;
            case '3' -> core.square.Rank.Three;
            case '4' -> core.square.Rank.Four;
            case '5' -> core.square.Rank.Five;
            case '6' -> core.square.Rank.Six;
            case '7' -> core.square.Rank.Seven;
            case '8' -> core.square.Rank.Eight;
            default -> throw new IllegalStateException("Unexpected value: " + line.charAt(1));
        };
        return new Square(file, Rank);
    }
}