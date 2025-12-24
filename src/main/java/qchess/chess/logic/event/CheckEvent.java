package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

public class CheckEvent extends ChessEvent {
    public static final EventType<CheckEvent> CHECK = new EventType<>(ChessEvent.ANY, "CHECK");

    public CheckEvent(ChessPiece instigator) {
        super(CHECK, instigator);
    }
}
