package qchess.chess.logic.event;

import javafx.event.Event;
import javafx.event.EventType;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.interfaces.Promotable;
import qchess.chess.logic.ChessBoard;

public class PromotionEvent extends ChessEvent {
    public static final EventType<PromotionEvent> PROMOTION = new EventType<>(ChessEvent.ANY, "PROMOTION_EVENT");
    ChessPiece promotionPiece;
    Promotable promotableChessPiece;

    public PromotionEvent(EventType<? extends ChessEvent> eventType, ChessPiece chessPiece, Promotable promotableChessPiece) {
        super(PROMOTION, chessPiece);
        this.promotableChessPiece = promotableChessPiece;
    }

    public ChessPiece getPromotionPiece() {
        return promotionPiece;
    }

    public Promotable getPromotableChessPiece() {
        return promotableChessPiece;
    }
}
