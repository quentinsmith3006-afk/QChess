package qchess.chess.logic.promotion;

public record PromotionSquares(int row, int col) {
    public PromotionSquares(int row) {
        this(row, -1);
    }
}
