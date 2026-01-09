package qchess.chess.logic.promotion;

/**
 * @author Quentin Smith
 *
 * This contains the location specification for promotion conditions.
 * Specifing -1 for row and 3 for col will make any promotable piece with this promotions square
 * eligible for promotion if they are on the 3rd column.
 *
 * If you specify both, then on that specific coordinate, chess piece can promote if it meets
 * all other conditions.
 *
 * Apply a -1 if you do not want to specify a row or column.
 * @param row
 * @param col
 */
public record PromotionSquares(int row, int col) {
    public PromotionSquares(int row) {
        this(row, -1);
    }
}
