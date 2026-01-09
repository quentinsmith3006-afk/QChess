package qchess.chess.create.interfaces;

import qchess.chess.create.ChessPiece;
import qchess.chess.logic.promotion.PromotionSquares;

/**
 * @author Quentin Smith
 *
 * Allows a chess piece which implements {@code Promotable} have the ability to promote.
 *
 * {@link qchess.chess.create.ChessPiece}
 * {@link qchess.chess.chessmen.Pawn}
 */
public interface Promotable {
    /**
     * Returns a array of all promotion options.
     * E.g. in a normal chess game, the array would contain a Knight, Bishop, Queen and Rook
     * @return a chess piece array of all promotion options.
     */
    public ChessPiece[] getPromotionOptions();

    /**
     * @return squares which black pieces which are promotable can promote on.
     */
    public PromotionSquares getBlackPromotionSquares();

    /**
     * @return squares which white pieces which are promotable can promote on.
     */
    public PromotionSquares getWhitePromotionSquares();

}
