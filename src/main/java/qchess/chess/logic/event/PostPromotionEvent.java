package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class PostPromotionEvent extends ChessEvent {
    public static final EventType<PostPromotionEvent> POSTPROMOTION = new EventType<>(ChessEvent.ANY, "POSTPROMOTION");

    public PostPromotionEvent(ChessPiece chessPiece) {
        super(POSTPROMOTION, chessPiece);
    }
}
