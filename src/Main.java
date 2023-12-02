import core.ChessBoard;
import core.ChessGame;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("ChessBoard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setContentPane(new ChessBoard(new ChessGame()).getMain());
        frame.setVisible(true);


    }
/*        Scanner scanner = new Scanner(new java.io.File("ChessGame.txt"));
        ChessGame game = new ChessGame();

        boolean gameEnded = false;
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(",");
            if (gameEnded) {
                System.out.println("Game already ended");
                continue;
            }
            gameEnded = gameLoop(line, game);
        }
    }

    private static boolean gameLoop(String[] line, ChessGame game) {
        PieceType promotion = null;
        if (line.length == 3) {
            promotion = switch (line[2]) {
                case "Q" -> PieceType.Queen;
                case "R" -> PieceType.Rook;
                case "B" -> PieceType.Bishop;
                case "K" -> PieceType.Knight;
                default -> null;
            };
        }

        QualifiedMove move;
        try {
            move = game.move(
                    new Move(parseSquare(line[0]),
                            parseSquare(line[1]),
                            promotion)
            );
        } catch (Exception e) {
            System.out.println("Invalid Move");
            return false;
        }

        if (move.castle() != CastleType.None)
            System.out.println("Castle");

        if (move.enPassant())
            System.out.println("Enpassant");

        if (move.capture() != null)
            System.out.println("Captured " + move.capture());

        switch (move.status()) {
            case Check -> {
                switch (move.piece().color()) {
                    case White -> System.out.println("Black in check");
                    case Black -> System.out.println("White in check");
                }
            }
            case WhiteWins -> System.out.println("White Won");
            case BlackWins -> System.out.println("Black Won");
            case Stalemate -> System.out.println("Stalemate");
            case Draw -> System.out.println("Draw");
            case InsufficientMaterial -> System.out.println("Insufficient Material");
        }

        return switch (move.status()) {
            case WhiteWins, BlackWins, Stalemate,
                    Draw, InsufficientMaterial -> true;
            case Check, InProgress -> false;
        };
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
            case '1' -> core.square.Rank._1;
            case '2' -> core.square.Rank._2;
            case '3' -> core.square.Rank._3;
            case '4' -> core.square.Rank._4;
            case '5' -> core.square.Rank._5;
            case '6' -> core.square.Rank._6;
            case '7' -> core.square.Rank._7;
            case '8' -> core.square.Rank._8;
            default -> throw new IllegalStateException("Unexpected value: " + line.charAt(1));
        };
        return new Square(file, Rank);
    }*/
}