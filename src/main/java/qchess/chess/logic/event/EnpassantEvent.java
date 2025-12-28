package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class EnpassantEvent extends ChessEvent {
    public final static EventType<EnpassantEvent> ENPASSANT = new EventType<>(ChessEvent.ANY, "ENPASSANT");

    public EnpassantEvent(ChessPiece instigator) {
        super(ENPASSANT, instigator);
    }
}
