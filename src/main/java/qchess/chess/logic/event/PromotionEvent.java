package qchess.chess.logic.event;

import javafx.event.Event;
import javafx.event.EventType;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;
import qchess.chess.logic.MoveLogic;
import qchess.chess.logic.promotion.PromotionLogic;

public class PromotionEvent extends ChessEvent {
    public static final EventType<PromotionEvent> PROMOTION = new EventType<>(ChessEvent.ANY, "PROMOTION_EVENT");
    ChessPiece promotionPiece;
    Promotable promotableChessPiece;
    MoveLogic moveLogic;

    public PromotionEvent(ChessPiece chessPiece, Promotable promotableChessPiece, MoveLogic moveLogic) {
        super(PROMOTION, chessPiece);
        this.promotableChessPiece = promotableChessPiece;
        this.moveLogic = moveLogic;
    }

    public MoveLogic getMoveLogic() {
        return moveLogic;
    }

    public ChessPiece getPromotionPiece() {
        return promotionPiece;
    }

    public Promotable getPromotableChessPiece() {
        return promotableChessPiece;
    }
}
