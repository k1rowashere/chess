package gui;

import core.Piece;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.Optional;

import static core.Color.White;
import static core.PieceType.Pawn;

public class Game extends JFrame {

    private static final int GRID_SIZE = 8;
    private final PieceLabel[][] grid;

    public Game() {
        setTitle("Image Grid");
        setSize(720, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridLayout gridLayout = new GridLayout(GRID_SIZE, GRID_SIZE);
        JPanel panel = new JPanel(gridLayout);
        grid = new PieceLabel[GRID_SIZE][GRID_SIZE];

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                var label = new PieceLabel(null);

                label.setBackground(new Color((i + j) % 2 == 0 ? 0xb58863 : 0xf0d9b5));

                grid[i][j] = label;
                panel.add(label);
            }
        }


        grid[0][0].setPiece(new Piece(Pawn, White));

        setContentPane(panel);
    }

    private static class PieceLabel extends JLabel {
        private Piece piece;
        private static Image[][] pieceImages;

        public PieceLabel(Piece piece) {
            setPiece(piece);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setTransferHandler(new PieceTransferHandler());
            setOpaque(true);

            if (pieceImages == null) {
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


            //Create a DragSource and associate it with the component
            DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                    this,
                    DnDConstants.ACTION_MOVE,
                    dge -> {
                        if (getPiece().isEmpty()) {
                            return;
                        }
                        Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                        Image image = getPieceIcon()
                                .getImage()
                                .getScaledInstance(50, 50, Image.SCALE_SMOOTH);

                        dge.startDrag(
                                cursor,
                                image,
                                new Point(10, 10),
                                new PieceTransferable(getPiece().get()),
                                null);
                        this.getTransferHandler().exportAsDrag(
                                this,
                                dge.getTriggerEvent(),
                                TransferHandler.MOVE);
                    });
        }


        @Contract("_ -> new")
        private static @NotNull ImageIcon getPieceIcon(@NotNull Piece piece) {
            return new ImageIcon(pieceImages[piece.color().ordinal()][piece.type().ordinal()]);
        }

        public @Nullable ImageIcon getPieceIcon() {
            return piece == null ? null : getPieceIcon(piece);
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
            setIcon(piece == null ? null : getPieceIcon(piece));
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
            if (action != TransferHandler.MOVE) {
                try {
                    ((PieceLabel) source)
                            .setPiece((Piece) data.getTransferData(PieceTransferable.PIECE_FLAVOR));
                } catch (UnsupportedFlavorException | IOException ignored) {
                }
            }

        }

        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            super.exportAsDrag(comp, e, action);
            ((PieceLabel) comp).setPiece(null);
        }
    }

}
