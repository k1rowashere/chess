//package core;
//
//import core.move.Move;
//import core.square.Square;
//
//import static core.square.File.*;
//import static core.square.Rank.*;
//
//class ChessGameTest {
//    @test
//    void test() {
//        /*
//        1 d4 d6
//        2 Nc3 Nf6
//        3 e4 g6
//        4 Bg5 Bg7
//        5 Bxf6 Bxf6
//        6 h4 O-O
//        7 h5 c6
//        8 hxg6 hxg6
//        9 Qf3 e5
//        10 Qg3 exd4
//        11 Qh2 Re8
//        12 Qh7+ Kf8
//        13 Nce2 d3
//        14 O-O-O dxe2
//        15 Bxe2 Qb6
//        16 c3 Qxf2
//        17 Bc4 Qxg2
//         */
//        var d4 = new Move(new Square(D, _2), new Square(D, _4));
//        var d6 = new Move(new Square(D, _7), new Square(D, _6));
//        var Nc3 = new Move(new Square(B, _1), new Square(C, _3));
//        var Nf6 = new Move(new Square(G, _8), new Square(F, _6));
//        var e4 = new Move(new Square(E, _2), new Square(E, _4));
//        var g6 = new Move(new Square(G, _7), new Square(G, _6));
//        var Bg5 = new Move(new Square(C, _1), new Square(G, _5));
//        var Bg7 = new Move(new Square(F, _8), new Square(G, _7));
//        var Bxf6 = new Move(new Square(G, _5), new Square(F, _6));
//        var Bxf6_2 = new Move(new Square(G, _7), new Square(F, _6));
//        var h4 = new Move(new Square(H, _2), new Square(H, _4));
//        var O_O = new Move(new Square(E, _8), new Square(G, _8));
//        var h5 = new Move(new Square(H, _4), new Square(H, _5));
//        var c6 = new Move(new Square(C, _7), new Square(C, _6));
//        var hxg6 = new Move(new Square(H, _5), new Square(G, _6));
//        var hxg6_2 = new Move(new Square(H, _7), new Square(G, _6));
//        var Qf3 = new Move(new Square(D, _1), new Square(F, _3));
//        var e5 = new Move(new Square(E, _7), new Square(E, _5));
//        var Qg3 = new Move(new Square(F, _3), new Square(G, _3));
//        var exd4 = new Move(new Square(E, _5), new Square(D, _4));
//        var Qh2 = new Move(new Square(G, _3), new Square(H, _2));
//        var Re8 = new Move(new Square(F, _8), new Square(E, _8));
//        var Qh7_ = new Move(new Square(H, _2), new Square(H, _7));
//        var Kf8 = new Move(new Square(G, _8), new Square(F, _8));
//        var Nce2 = new Move(new Square(C, _3), new Square(E, _2));
//        var d3 = new Move(new Square(D, _4), new Square(D, _3));
//        var O_O_O = new Move(new Square(E, _1), new Square(C, _1));
//        var dxe2 = new Move(new Square(D, _3), new Square(E, _2));
//        var Bxe2 = new Move(new Square(F, _1), new Square(E, _2));
//        var Qb6 = new Move(new Square(D, _8), new Square(B, _6));
//        var c3 = new Move(new Square(C, _2), new Square(C, _3));
//        var Qxf2 = new Move(new Square(B, _6), new Square(F, _2));
//        var Bc4 = new Move(new Square(E, _2), new Square(C, _4));
//        var Qxg2 = new Move(new Square(F, _2), new Square(G, _2));
//        var Qxf7_ = new Move(new Square(H, _7), new Square(F, _7));
//
//        var game = new ChessGame();
//        Move[] moves = {
//                d4, d6,
//                Nc3, Nf6,
//                e4, g6,
//                Bg5, Bg7,
//                Bxf6, Bxf6_2,
//                h4, O_O,
//                h5, c6,
//                hxg6, hxg6_2,
//                Qf3, e5,
//                Qg3, exd4,
//                Qh2, Re8,
//                Qh7_, Kf8,
//                Nce2, d3,
//                O_O_O, dxe2,
//                Bxe2, Qb6,
//                c3, Qxf2,
//                Bc4, Qxg2,
//                Qxf7_
//        };
//
//        for (Move move : moves) {
//            System.out.print(move.from() + " -> " + move.to() + " ");
//            System.out.println(game.getLegalMoves(move.from()));
//            System.out.println(game.move(move));
//            System.out.println(game.board());
//        }
//    }
//
//
//}