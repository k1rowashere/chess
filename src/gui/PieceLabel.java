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
    private static final DataFlavor DATA_FLAVOR = new DataFlavor(Piece.class, "PieceLabel");
    private static final Color DARK = new Color(0xb58863);
    private static final Color LIGHT = new Color(0xf0d9b5);
    private static final Color SELECTED = new Color(0x00DD00);
    private static final Color LEGAL = new Color(0x9C9C9C);
    private static final Color CHECK = new Color(0xFF0000);
    private static final Color LAST_MOVE = new Color(0xDDDD00);
    private static Image[][] pieceImages;

    private final Color initBg;
    private Piece piece;
    private boolean selected = false;
    private boolean inCheck = false;
    private boolean legal = false;
    private boolean lastMove = false;


    public PieceLabel(Piece piece, Square square, Dimension dim,
                      BiConsumer<PieceLabel, Square> onSelect,
                      BiConsumer<PieceLabel, Square> onDrag) {
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
                return support.isDataFlavorSupported(DATA_FLAVOR);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                onSelect.accept(PieceLabel.this, square);
                return true;
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.accept(PieceLabel.this, square);
            }
        });

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                this,
                DnDConstants.ACTION_COPY_OR_MOVE,
                dge -> {
                    if (this.piece == null) return;
                    onDrag.accept(PieceLabel.this, square);
                    this.ghost();
                    // workaround for the ghost image not being drawn on linux
                    // using a cursor instead of drag image
                    Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                            getImage(),
                            new Point(this.getWidth() / 2, this.getHeight() / 2),
                            null
                    );
                    Transferable transferable = new Transferable() {
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
                            return PieceLabel.this.piece;
                        }
                    };
                    dge.startDrag(cursor, transferable);
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

    public void select() {
        this.selected = true;
        this.update();
    }

    public void legal() {
        this.legal = true;
        this.update();
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

    public void deselect() {
        this.selected = false;
        this.legal = false;
        this.setPiece(this.piece);
        this.update();
    }

    public void check() {
        this.inCheck = true;
        this.update();
    }

    public void reset() {
        this.inCheck = false;
        this.legal = false;
        this.lastMove = false;
        this.selected = false;
        this.update();
    }

    public void lastMove() {
        this.lastMove = true;
        this.update();
    }

    @NotNull
    private Image getImage() {
        return pieceImages[piece.color().ordinal()][piece.type().ordinal()];
    }

    private Color mix(Color a, Color b) {
        return new Color(
                (a.getRed() + b.getRed()) / 2,
                (a.getGreen() + b.getGreen()) / 2,
                (a.getBlue() + b.getBlue()) / 2
        );
    }

    private void update() {
        var bgColor = new Color(initBg.getRGB());
        if (this.selected) bgColor = mix(bgColor, SELECTED);
        if (this.legal) bgColor = mix(bgColor, LEGAL);
        if (this.inCheck) bgColor = mix(bgColor, CHECK);
        if (this.lastMove) bgColor = mix(bgColor, LAST_MOVE);
        this.setBackground(bgColor);
    }
}
