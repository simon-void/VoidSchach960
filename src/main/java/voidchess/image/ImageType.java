package voidchess.image;

public enum ImageType {
    B_PAWN(true),
    B_KNIGHT(true),
    B_BISHOP(true),
    B_Rook(true),
    B_QUEEN(true),
    B_KING(true),
    W_PAWN(true),
    W_KNIGHT(true),
    W_BISHOP(true),
    W_Rook(true),
    W_QUEEN(true),
    W_KING(true),
    ICON(false);

    final public boolean isFigure;

    ImageType(boolean isFigure) {
        this.isFigure = isFigure;
    }

    public String getFileName() {
        return name().toLowerCase() + ".gif";
    }
}
