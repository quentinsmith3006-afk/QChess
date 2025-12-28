package qchess.chess.create.interfaces;

import qchess.chess.create.ChessPiece;
import qchess.chess.logic.promotion.PromotionSquares;

public interface Promotable {
    public ChessPiece[] getPromotionOptions();
    public PromotionSquares getBlackPromotionSquares();
    public PromotionSquares getWhitePromotionSquares();

}
