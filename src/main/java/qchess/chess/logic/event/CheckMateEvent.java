package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class CheckMateEvent extends ChessEvent {
    public static final EventType<CheckMateEvent> CHECK_MATE = new EventType<>(ChessEvent.ANY, "CHECK_MATE");

    public CheckMateEvent(ChessPiece instigator) {
        super(CHECK_MATE, instigator);
    }
}
