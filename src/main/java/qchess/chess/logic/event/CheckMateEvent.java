package qchess.chess.logic.event;

import javafx.event.EventType;

public class CheckMateEvent extends ChessEvent {
    public static final EventType<CheckMateEvent> CHECK_MATE = new EventType<>(ChessEvent.ANY, "CHECK_MATE");

    public CheckMateEvent() {
        super(CHECK_MATE);
    }
}
