package gui;

import core.ChessGame;
import core.Piece;
import core.PieceType;
import core.move.CastleType;
import core.move.Move;
import core.move.QualifiedMove;
import core.square.File;
import core.square.Rank;
import core.square.Square;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Game extends JFrame {
    private static final int GRID_SIZE = 8;
    private static final int CELL_SIZE = 90;
    private static final int TOTAL_SIZE = GRID_SIZE * CELL_SIZE;
    private final Board board;
    private Square selectedSquare = null;

    public Game(ChessGame game) {
        this.setTitle("Image Grid");
        this.setSize(TOTAL_SIZE, TOTAL_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var gridLayout = new GridLayout(GRID_SIZE, GRID_SIZE);
        gridLayout.preferredLayoutSize(this);

        var panel = new JPanel(gridLayout);

        BiConsumer<PieceLabel, Square> onSelect = (label, square) -> move(game, label, square);
        this.board = new Board(onSelect, panel);

        // init board
        game.board().getPieces().forEach((boardPiece) -> {
            var label = board.get(boardPiece.square());
            label.setPiece(boardPiece.piece());
        });

        setContentPane(panel);
        flip();
    }

    private void move(ChessGame game, PieceLabel label, Square square) {
        // reset
        board.forEach((l, s) -> l.setBorder(null));


        if (Objects.equals(this.selectedSquare, square)) {
            this.selectedSquare = null;
            return;
        }

        if (this.selectedSquare != null) {
            try {
                var move = game.move(new Move(this.selectedSquare, square));
                System.out.println(move);
                updateUi(move);
                return;
            } catch (IllegalArgumentException ignored) {
                System.out.println("Invalid move");
            }
        }

        this.selectedSquare = square;
        label.setBorder(BorderFactory.createLineBorder(Color.RED, 3));

        var moves = game.getLegalMoves(square);
        moves.forEach(System.out::println);
        moves.forEach(move ->
                board.get(move).setBorder(BorderFactory.createLineBorder(Color.GREEN, 3))
        );
    }

    private void animateMove(QualifiedMove move) {
        var from = board.get(move.from());
        var to = board.get(move.to());

        JPanel contentPane = (JPanel) getContentPane();
        var glassPane = (JPanel) contentPane.getRootPane().getGlassPane();
        var tempLabel = new JLabel(from.getIcon());
        tempLabel.setSize(from.getSize());
        tempLabel.setLocation(from.getX(), from.getY());
        glassPane.setBounds(0, 0, TOTAL_SIZE, TOTAL_SIZE);
        glassPane.setLayout(null);
        var comp = glassPane.add(tempLabel);
        glassPane.setVisible(true);
        glassPane.setOpaque(false);
        glassPane.repaint();

        var dy = (to.getY() - from.getY()) / 10.0;
        var dx = (to.getX() - from.getX()) / 10.0;

        from.setPiece(null);

        // ease in ease out
        var timer = new Timer(10, e -> {
            var x = (int) (tempLabel.getX() + dx);
            var y = (int) (tempLabel.getY() + dy);
            comp.setLocation(x, y);
            comp.repaint();
            comp.revalidate();
            if (x == to.getX() && y == to.getY()) {
                ((Timer) e.getSource()).stop();
                glassPane.remove(comp);
                glassPane.setVisible(false);
                to.setPiece(move.piece());
//                flip();
            }
        });
        timer.start();
    }

    private void updateUi(QualifiedMove move) {
        // en passant
        if (move.enPassant()) {
            var enPassantSquare = new Square(move.to().file(),
                    move.from().rank());
            board.get(enPassantSquare).setPiece(null);
        }

        // promotion
        if (move.promotion() != null) {
            var promPiece = new Piece(
                    move.promotion(),
                    move.piece().color());
            board.get(move.to()).setPiece(promPiece);
        }

        // castle
        if (move.castle() != CastleType.None) {
            var rookFile =
                    move.castle() == CastleType.Short ?
                            File.H : File.A;
            var rookRank = move.piece().color() == core.Color.White ?
                    Rank._1 : Rank._8;
            var rookSquare = new Square(rookFile, rookRank);
            var newRookFile =
                    move.castle() == CastleType.Short ?
                            File.F : File.D;
            var newRookSquare = new Square(newRookFile, rookRank);
            Piece rook = new Piece(
                    PieceType.Rook,
                    move.piece().color());
            // TODO: animate rook
            board.get(newRookSquare).setPiece(rook);
            board.get(rookSquare).setPiece(null);
        }

        animateMove(move);
    }


    private void flip() {
        var panel = this.getContentPane();
        var components = panel.getComponents();
        panel.removeAll();

        for (int i = GRID_SIZE - 1; i >= 0; i--) {
            for (int j = 0; j < GRID_SIZE; j++) {
                panel.add(components[i * GRID_SIZE + j]);
            }
        }

        panel.revalidate();
        panel.repaint();
    }


    static class PieceLabel extends JLabel {
        private static Image[][] pieceImages;
        private Piece piece;

        public PieceLabel(PieceLabel label) {
            this.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setVerticalAlignment(SwingConstants.CENTER);
            this.setVisible(true);

            this.setPiece(label.getPiece().orElse(null));
        }

        public PieceLabel(Piece piece, Square square, BiConsumer<PieceLabel, Square> onSelect) {
            this.setPiece(piece);
            this.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setVerticalAlignment(SwingConstants.CENTER);
            this.setTransferHandler(new PieceTransferHandler());
            this.setOpaque(true);

            initImages();

            //Create a DragSource and associate it with the component
//            DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
//                    this,
//                    DnDConstants.ACTION_MOVE,
//                    dge -> {
//                        if (getPiece().isEmpty()) {
//                            return;
//                        }
//                        Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//                        //noinspection OptionalGetWithoutIsPresent
//                        Image image = getPieceIcon().get().getImage();
//
//                        dge.startDrag(
//                                cursor,
//                                image,
//                                new Point(10, 10),
//                                new PieceTransferable(getPiece().get()),
//                                null);
////                        this.getTransferHandler().exportAsDrag(
////                                this,
////                                dge.getTriggerEvent(),
////                                TransferHandler.MOVE);
//                    });

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    onSelect.accept(PieceLabel.this, square);
                }
            });
        }

        private static void initImages() {
            if (pieceImages != null) {
                return;
            }

            pieceImages = new Image[2][6];
            for (var c : core.Color.values()) {
                for (var t : core.PieceType.values()) {
                    var path = String.format(
                            "resources/skins/default/%s/%s.png",
                            c.toString().toLowerCase(),
                            t.toString().toLowerCase()
                    );
                    pieceImages[c.ordinal()][t.ordinal()] = new ImageIcon(path).getImage();
                }
            }
        }


        @Contract("_ -> new")
        private static @NotNull ImageIcon getPieceIcon(@NotNull Piece piece) {
            return new ImageIcon(pieceImages[piece.color().ordinal()][piece.type().ordinal()]);
        }

        public Optional<ImageIcon> getPieceIcon() {
            return getPiece().map(PieceLabel::getPieceIcon);
        }

        public void setPiece(Piece piece) {
            this.piece = piece;

            if (piece == null) {
                this.setIcon(null);
                return;
            }

            var icon = getPieceIcon(piece)
                    .getImage()
                    .getScaledInstance(
                            this.getPreferredSize().width,
                            this.getPreferredSize().height,
                            Image.SCALE_SMOOTH);
            this.setIcon(new ImageIcon(icon));
        }

        public Optional<Piece> getPiece() {
            return Optional.ofNullable(piece);
        }
    }

    private record PieceTransferable(Piece piece) implements Transferable {
        static final DataFlavor PIECE_FLAVOR = new DataFlavor(Piece.class, "Piece");

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{PIECE_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(PIECE_FLAVOR);
        }

        @Override
        public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(PIECE_FLAVOR)) {
                return piece;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }

    private static class PieceTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(PieceTransferable.PIECE_FLAVOR);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return ((PieceLabel) c).getPiece()
                    .map(PieceTransferable::new)
                    .orElse(null);
        }

        @Override
        public boolean importData(TransferSupport support) {
            try {
                var transferable = support.getTransferable();
                Piece droppedPiece = (Piece) transferable.getTransferData(PieceTransferable.PIECE_FLAVOR);

                if (support.getComponent() instanceof PieceLabel targetLabel) {
                    targetLabel.setPiece(droppedPiece);
                }

                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                System.out.println("Error importing data");
                return false;
            }
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            System.out.println(action);
            if (action == TransferHandler.MOVE) {
                var sourceLabel = (PieceLabel) source;
                sourceLabel.setPiece(null);
//                sourceLabel.setGhost(true);
            }
        }

        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            super.exportAsDrag(comp, e, action);

            var label = ((PieceLabel) comp);
            var icon = (ImageIcon) label.getIcon();
            BufferedImage image = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            // lower opacity
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g.drawImage(icon.getImage(), 0, 0, null);
            g.dispose();
            label.setIcon(new ImageIcon(image));
        }
    }

}
