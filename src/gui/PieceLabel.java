package gui;

import core.Piece;
import core.square.Square;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

class PieceLabel extends JLabel {
    private static final Color DARK = new Color(0xb58863);
    private static final Color LIGHT = new Color(0xf0d9b5);
    private static final Color SELECTED = new Color(0x00DD00);
    private static final Color LEGAL = new Color(0x9C9C9C);
    private static final Color CHECK = new Color(0xFF0000);
    private static Image[][] pieceImages;
    private final Color initBg;
    private Piece piece;

    public PieceLabel(Piece piece, Square square, Dimension dim,
                      BiConsumer<PieceLabel,
                              Square> onSelect) {
        initImages(dim.width, dim.height);

        this.initBg =
                (square.rank().ordinal() + square.file().ordinal()) % 2 == 0 ?
                        DARK : LIGHT;
        this.setBackground(initBg);

        this.setPiece(piece);
        this.setPreferredSize(dim);
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setVerticalAlignment(SwingConstants.CENTER);
        this.setOpaque(true);
        this.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(PieceTransferable.DATA_FLAVOR);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                onSelect.accept(PieceLabel.this, square);
                return true;
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                onSelect.accept(PieceLabel.this, square);
            }
        });

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                this,
                DnDConstants.ACTION_COPY_OR_MOVE,
                dge -> {
                    var label = PieceLabel.this;
                    var icon = (ImageIcon) label.getIcon();
                    if (icon == null) {
                        return;
                    }
                    this.ghost();
                    this.select();
                    dge.startDrag(
                            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
                            getImage(),
                            new Point(0, 0),
                            new PieceTransferable(piece),
                            null
                    );
                }
        );
    }

    private static void initImages(int width, int height) {
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
                pieceImages[c.ordinal()][t.ordinal()] =
                        new ImageIcon(path).getImage()
                                .getScaledInstance(width, height, Image.SCALE_SMOOTH);
            }
        }
    }


    public void setPiece(Piece piece) {
        this.piece = piece;
        if (piece == null) {
            this.setIcon(null);
            return;
        }

        this.setIcon(new ImageIcon(getImage()));
    }

    public void reset() {
        this.setBackground(initBg);
        this.setPiece(piece);
    }

    public void select() {
        // mix the color with initBg
        var r = (initBg.getRed() + SELECTED.getRed()) / 2;
        var g = (initBg.getGreen() + SELECTED.getGreen()) / 2;
        var b = (initBg.getBlue() + SELECTED.getBlue()) / 2;
        this.setBackground(new Color(r, g, b));
    }

    public void ghost() {
        if (this.piece == null) {
            return;
        }

        var img = new BufferedImage(this.getPreferredSize().width,
                this.getPreferredSize().height,
                BufferedImage.TYPE_INT_ARGB);
        var g = (Graphics2D) img.getGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        // wait for the image to be drawn
        //noinspection StatementWithEmptyBody
        while (!g.drawImage(getImage(), 0, 0, null)) ;

        this.setIcon(new ImageIcon(img));
    }

    @NotNull
    private Image getImage() {
        return pieceImages[piece.color().ordinal()][piece.type().ordinal()];
    }

    public void legal() {
        var r = (initBg.getRed() + LEGAL.getRed()) / 2;
        var g = (initBg.getGreen() + LEGAL.getGreen()) / 2;
        var b = (initBg.getBlue() + LEGAL.getBlue()) / 2;
        this.setBackground(new Color(r, g, b));
    }

    public void check() {
        this.setBackground(CHECK);
    }

    private record PieceTransferable(Piece piece) implements Transferable {

        public static final DataFlavor DATA_FLAVOR =
                new DataFlavor(Piece.class, "PieceLabel");

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DATA_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DATA_FLAVOR);
        }

        @Override
        public @NotNull Object getTransferData(DataFlavor flavor) {
            return piece;
        }
    }
}
