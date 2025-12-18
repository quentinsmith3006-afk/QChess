package qchess.chess.logic.event;

import javafx.event.EventType;

public class CheckEvent extends ChessEvent {
    public static final EventType<CheckEvent> CHECK = new EventType<>(ChessEvent.ANY, "CHECK");

    public CheckEvent() {
        super(CHECK);
    }
}
